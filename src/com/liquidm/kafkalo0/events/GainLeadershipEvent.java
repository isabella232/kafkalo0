package com.liquidm.kafkalo0.events;

import com.liquidm.kafkalo0.*;
import com.liquidm.kafkalo0.json.*;

public class GainLeadershipEvent {

    private KafkaPartitionInfo partitionInfo;
    private KafkaBroker newLeader;

    public GainLeadershipEvent(KafkaPartitionInfo partitionInfo, KafkaBroker newLeader) {
        this.partitionInfo = partitionInfo;
        this.newLeader = newLeader;
    }

    public KafkaPartitionInfo getPartitionInfo() {
        return partitionInfo;
    }

    public KafkaBroker getNewLeader() {
        return newLeader;
    }

    @Override
    public String toString() {
        return "GainLeadershipEvent [partitionInfo=" + partitionInfo + ", newLeader=" + newLeader + "]";
    }

}
