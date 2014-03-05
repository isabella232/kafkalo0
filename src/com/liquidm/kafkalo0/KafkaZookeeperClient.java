package com.liquidm.kafkalo0;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import org.apache.curator.framework.*;
import org.apache.curator.framework.recipes.cache.*;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;
import com.google.common.collect.*;
import com.google.common.eventbus.*;
import com.liquidm.kafkalo0.events.*;
import com.liquidm.kafkalo0.json.*;

public class KafkaZookeeperClient  {

    public final static int DEFAULT_KAFKA_SO_TIMEOUT = 100000;
    public final static int DEFAULT_KAFKA_BUFFER_SIZE = 64 * 1024;

    private CuratorFramework curatorClient;
    private PathChildrenCache idsPathCache;
    private NodeCache topicPathCache;
    private ObjectMapper jsonMapper = new ObjectMapper();
    private EventBus eventBus;
    private String topic;
    private ConcurrentMap<Integer, KafkaBroker> brokers = new ConcurrentHashMap<Integer, KafkaBroker>();
    private KafkaTopicInfo currentTopicInfo = new KafkaTopicInfo();
    private Map<String, NodeCache> partitionPathCaches = new HashMap<String, NodeCache>();
    private BiMap<KafkaPartitionInfo, KafkaBroker> leaders = HashBiMap.create();
    
    // in the case zookeeper notifies us about parition change before the broker addition
    private Map<Integer, KafkaPartitionInfo> pendingLeaders = new HashMap<Integer, KafkaPartitionInfo>();

    
    public KafkaZookeeperClient(CuratorFramework curatorClient, EventBus eventBus, String topic) {
        this.curatorClient = curatorClient;
        this.eventBus = eventBus;
        this.topic = topic;
        
        // so, upon startup, we'll treat it as if there was no partition
        currentTopicInfo.setPartitions(new HashMap<String, List<Integer>>());
    }
    
    private KafkaBroker addAndResolveBroker(KafkaBrokerInfo info) {
        KafkaBroker broker = new KafkaBroker(info);
        KafkaBroker old = brokers.putIfAbsent(info.getId(), broker);
        if (old != null) {
            broker = old;
        }
        broker.updateFrom(info);
        return broker;
    }
    
    public void start() throws Exception {
        idsPathCache = new PathChildrenCache(curatorClient, "/brokers/ids", true);
        idsPathCache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                case CHILD_ADDED: {
                    KafkaBroker broker = addAndResolveBroker(readBrokerInfo(event));
                    eventBus.post(new BrokerAddedEvent(broker));
                    KafkaPartitionInfo partitionInfo = pendingLeaders.remove(broker.getInfo().getId());
                    if (partitionInfo != null) {
                        gainLeadership(partitionInfo, broker);
                    }
                    break;
                }
                case CHILD_REMOVED: {
                    KafkaBroker broker = addAndResolveBroker(readBrokerInfo(event));
                    eventBus.post(new BrokerRemovedEvent(broker));
                    KafkaPartitionInfo partitionInfo = leaders.inverse().get(broker);
                    if (partitionInfo != null) {
                        loseLeadership(partitionInfo, broker);
                        pendingLeaders.put(partitionInfo.getLeader(), partitionInfo);
                    }
                    break;
                }
                case CHILD_UPDATED: {
                    eventBus.post(new BrokerUpdatedEvent(addAndResolveBroker(readBrokerInfo(event))));
                    break;
                }
                case CONNECTION_LOST:
                    break;
                case CONNECTION_RECONNECTED:
                    break;
                case CONNECTION_SUSPENDED:
                    break;
                case INITIALIZED:
                    break;
                default:
                    break;

                }
            }

        });
        idsPathCache.start();

        topicPathCache = new NodeCache(curatorClient, "/brokers/topics/" + topic);
        topicPathCache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                KafkaTopicInfo newTopicInfo = jsonMapper.readValue(topicPathCache.getCurrentData().getData(), KafkaTopicInfo.class);
                if (newTopicInfo.equals(currentTopicInfo)) {
                    return;
                }
                topicChanged(newTopicInfo);
            }
        });
        topicPathCache.start();

    }
    
    private KafkaBrokerInfo readBrokerInfo(PathChildrenCacheEvent event) throws IOException, JsonParseException, JsonMappingException {
        KafkaBrokerInfo brokerInfo = jsonMapper.readValue(event.getData().getData(), KafkaBrokerInfo.class);
        String path = event.getData().getPath();
        brokerInfo.setId(Integer.parseInt(path.substring(path.lastIndexOf('/') + 1)));
        return brokerInfo;
    }

    private void topicChanged(KafkaTopicInfo newTopicInfo) throws IOException, Exception {
        
        Set<String> removals = new HashSet<String>(currentTopicInfo.getPartitions().keySet());
        Set<String> inserts = new HashSet<String>(newTopicInfo.getPartitions().keySet());

        removals.removeAll(newTopicInfo.getPartitions().keySet());
        inserts.removeAll(currentTopicInfo.getPartitions().keySet());
        
        currentTopicInfo = newTopicInfo;

        for (String partitionId : removals) {
            NodeCache nodeCache = partitionPathCaches.remove(partitionId);
            nodeCache.close();
        }
        
        for (final String partitionId : inserts) {
            final NodeCache nodeCache = new NodeCache(curatorClient, "/brokers/topics/" + topic + "/partitions/"+partitionId+"/state");
            partitionPathCaches.put(partitionId, nodeCache);
            nodeCache.getListenable().addListener(new NodeCacheListener() {
                @Override
                public void nodeChanged() throws Exception {
                    KafkaPartitionInfo partitionInfo = jsonMapper.readValue(nodeCache.getCurrentData().getData(), KafkaPartitionInfo.class);
                    partitionInfo.setId(partitionId);
                    partitionChanged(partitionInfo);
                }

            });

            nodeCache.start();
        }
    }
    
    private void partitionChanged(KafkaPartitionInfo partitionInfo) {
        KafkaBroker leader = leaders.get(partitionInfo);
        if (leader != null) {
            if (leader.getInfo().getId() != partitionInfo.getLeader()) {
                loseLeadership(partitionInfo, leader);
            }
        } else if(partitionInfo.getLeader() != -1) {
            leader = brokers.get(partitionInfo.getLeader());
            if (leader != null) {
                gainLeadership(partitionInfo, leader);
            } else {
                pendingLeaders.put(partitionInfo.getLeader(), partitionInfo);
            }
        }
    }

    private void loseLeadership(KafkaPartitionInfo partitionInfo, KafkaBroker broker) {
        leaders.remove(partitionInfo);
        eventBus.post(new LostLeadershipEvent(partitionInfo, broker));
    }

    private void gainLeadership(KafkaPartitionInfo partitionInfo, KafkaBroker broker) {
        leaders.put(partitionInfo, broker);
        eventBus.post(new GainLeadershipEvent(partitionInfo, broker));
    }
}
