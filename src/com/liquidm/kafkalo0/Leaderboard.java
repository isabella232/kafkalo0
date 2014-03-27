package com.liquidm.kafkalo0;

import java.util.*;
import java.util.Map.*;

import com.google.common.collect.*;
import com.liquidm.kafkalo0.json.*;

public class Leaderboard {

    private Multimap<KafkaBroker, KafkaPartitionInfo> map = HashMultimap.create();

    public KafkaBroker getBrokerFor(KafkaPartitionInfo partitionInfo) {
        for (Entry<KafkaBroker, Collection<KafkaPartitionInfo>> entry : map.asMap().entrySet()) {
            if (entry.getValue().contains(partitionInfo)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public void remove(KafkaPartitionInfo partitionInfo) {
        for (Entry<KafkaBroker, Collection<KafkaPartitionInfo>> entry : map.asMap().entrySet()) {
            if (entry.getValue().remove(partitionInfo)) {
                return;
            }
        }
    }

    public void put(KafkaBroker broker, KafkaPartitionInfo partitionInfo) {
        map.put(broker, partitionInfo);
    }

    public Collection<KafkaPartitionInfo> getPartitionsFor(KafkaBroker broker) {
        return map.get(broker);
    }
}