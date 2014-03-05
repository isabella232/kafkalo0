//package com.test.simple;
//
//import kafka.api.FetchRequest;
//import kafka.api.FetchRequestBuilder;
//import kafka.api.PartitionOffsetRequestInfo;
//import kafka.common.ErrorMapping;
//import kafka.common.TopicAndPartition;
//import kafka.javaapi.*;
//import kafka.javaapi.consumer.SimpleConsumer;
//import kafka.message.MessageAndOffset;
//
//import java.nio.ByteBuffer;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import com.google.common.collect.*;
//
///**
// * Copied from: https://cwiki.apache.org/confluence/display/KAFKA/0.8.0+SimpleConsumer+Example
// */
//public class SimpleExample {
//    public static void main(String args[]) {
//        SimpleExample example = new SimpleExample();
//        long maxReads = 1000;
//        String topic = "mytopc";
//        int partition = 3;
//        List<String> seeds = Lists.newArrayList("localhost:9092", "localhost:9093");
//        try {
//            example.run(maxReads, topic, partition, seeds);
//        } catch (Exception e) {
//            System.out.println("Oops:" + e);
//            e.printStackTrace();
//        }
//    }
//
//    private List<String> m_replicaBrokers = new ArrayList<String>();
//
//    public SimpleExample() {
//        m_replicaBrokers = new ArrayList<String>();
//    }
//
//    public void run(long a_maxReads, String a_topic, int a_partition, List<String> a_seedBrokers) throws Exception {
//        // find the meta data about the topic and partition we are interested in
//        //
//        PartitionMetadata metadata = findLeader(a_seedBrokers, a_topic, a_partition);
//        if (metadata == null) {
//            System.out.println("Can't find metadata for Topic and Partition. Exiting");
//            return;
//        }
//        if (metadata.leader() == null) {
//            System.out.println("Can't find Leader for Topic and Partition. Exiting");
//            return;
//        }
//        String leadBroker = metadata.leader().host();
//        int leadBrokerPort = metadata.leader().port();
//        String clientName = "Client_" + a_topic + "_" + a_partition;
//
//        SimpleConsumer consumer = new SimpleConsumer(leadBroker, leadBrokerPort, 100000, 64 * 1024, clientName);
//        long readOffset = getLastOffset(consumer, a_topic, a_partition, kafka.api.OffsetRequest.EarliestTime(), clientName);
//
//        int numErrors = 0;
//        while (a_maxReads > 0) {
//            if (consumer == null) {
//                consumer = new SimpleConsumer(leadBroker, leadBrokerPort, 100000, 64 * 1024, clientName);
//            }
//            FetchRequest req = new FetchRequestBuilder().clientId(clientName).addFetch(a_topic, a_partition, readOffset, 100000).build();
//            FetchResponse fetchResponse = consumer.fetch(req);
//
//            if (fetchResponse.hasError()) {
//                numErrors++;
//                // Something went wrong!
//                short code = fetchResponse.errorCode(a_topic, a_partition);
//                System.out.println("Error fetching data from the Broker:" + leadBroker + " Reason: " + code);
//                if (numErrors > 5)
//                    break;
//                if (code == ErrorMapping.OffsetOutOfRangeCode()) {
//                    // We asked for an invalid offset. For simple case ask for the last element to reset
//                    readOffset = getLastOffset(consumer, a_topic, a_partition, kafka.api.OffsetRequest.LatestTime(), clientName);
//                    continue;
//                }
//                consumer.close();
//                consumer = null;
//                String[] host = findNewLeader(leadBroker, leadBrokerPort, a_topic, a_partition).split(":");
//                leadBroker = host[0];
//                leadBrokerPort = Integer.parseInt(host[1]);
//                continue;
//            }
//            numErrors = 0;
//
//            long numRead = 0;
//            for (MessageAndOffset messageAndOffset : fetchResponse.messageSet(a_topic, a_partition)) {
//                long currentOffset = messageAndOffset.offset();
//                if (currentOffset < readOffset) {
//                    System.out.println("Found an old offset: " + currentOffset + " Expecting: " + readOffset);
//                    continue;
//                }
//                readOffset = messageAndOffset.nextOffset();
//                ByteBuffer payload = messageAndOffset.message().payload();
//
//                byte[] bytes = new byte[payload.limit()];
//                payload.get(bytes);
//                System.out.println(String.valueOf(messageAndOffset.offset()) + ": " + new String(bytes, "UTF-8"));
//                numRead++;
//                a_maxReads--;
//            }
//
//            if (numRead == 0) {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ie) {
//                }
//            }
//        }
//        if (consumer != null)
//            consumer.close();
//    }
//
//    public static long getLastOffset(SimpleConsumer consumer, String topic, int partition, long whichTime, String clientName) {
//        TopicAndPartition topicAndPartition = new TopicAndPartition(topic, partition);
//        Map<TopicAndPartition, PartitionOffsetRequestInfo> requestInfo = new HashMap<TopicAndPartition, PartitionOffsetRequestInfo>();
//        requestInfo.put(topicAndPartition, new PartitionOffsetRequestInfo(whichTime, 1));
//        kafka.javaapi.OffsetRequest request = new kafka.javaapi.OffsetRequest(requestInfo, kafka.api.OffsetRequest.CurrentVersion(), clientName);
//        OffsetResponse response = consumer.getOffsetsBefore(request);
//
//        if (response.hasError()) {
//            System.out.println("Error fetching data Offset Data the Broker. Reason: " + response.errorCode(topic, partition));
//            return 0;
//        }
//        long[] offsets = response.offsets(topic, partition);
//        return offsets[0];
//    }
//
//    private String findNewLeader(String a_oldLeader, int a_oldLeaderPort, String a_topic, int a_partition) throws Exception {
//        for (int i = 0; i < 3; i++) {
//            boolean goToSleep = false;
//            PartitionMetadata metadata = findLeader(m_replicaBrokers, a_topic, a_partition);
//            if (metadata == null) {
//                goToSleep = true;
//            } else if (metadata.leader() == null) {
//                goToSleep = true;
//            } else if (a_oldLeaderPort == metadata.leader().port() && a_oldLeader.equalsIgnoreCase(metadata.leader().host()) && i == 0) {
//                // first time through if the leader hasn't changed give ZooKeeper a second to recover
//                // second time, assume the broker did recover before failover, or it was a non-Broker issue
//                //
//                goToSleep = true;
//            } else {
//                return metadata.leader().host()+":"+metadata.leader().port();
//            }
//            if (goToSleep) {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException ie) {
//                }
//            }
//        }
//        System.out.println("Unable to find new leader after Broker failure. Exiting");
//        throw new Exception("Unable to find new leader after Broker failure. Exiting");
//    }
//
//    private PartitionMetadata findLeader(List<String> a_seedBrokers, String a_topic, int a_partition) {
//        PartitionMetadata returnMetaData = null;
//        for (String seed : a_seedBrokers) {
//            SimpleConsumer consumer = null;
//            try {
//                String[] host = seed.split(":");
//                consumer = new SimpleConsumer(host[0], Integer.parseInt(host[1]), 100000, 64 * 1024, "leaderLookup");
//                List<String> topics = new ArrayList<String>();
//                topics.add(a_topic);
//                TopicMetadataRequest req = new TopicMetadataRequest(topics);
//                kafka.javaapi.TopicMetadataResponse resp = consumer.send(req);
//
//                List<TopicMetadata> metaData = resp.topicsMetadata();
//                for (TopicMetadata item : metaData) {
//                    for (PartitionMetadata part : item.partitionsMetadata()) {
//                        if (part.partitionId() == a_partition) {
//                            returnMetaData = part;
//                            break;
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                System.out.println("Error communicating with Broker [" + seed + "] to find Leader for [" + a_topic + ", " + a_partition + "] Reason: " + e);
//            } finally {
//                if (consumer != null)
//                    consumer.close();
//            }
//        }
//        if (returnMetaData != null) {
//            m_replicaBrokers.clear();
//            for (kafka.cluster.Broker replica : returnMetaData.replicas()) {
//                m_replicaBrokers.add(replica.host());
//            }
//        }
//        return returnMetaData;
//    }
//}