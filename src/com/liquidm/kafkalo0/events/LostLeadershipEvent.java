package com.liquidm.kafkalo0.events;

import com.liquidm.kafkalo0.*;
import com.liquidm.kafkalo0.json.*;

public class LostLeadershipEvent {

    private KafkaPartitionInfo partitionInfo;
    private KafkaBroker oldLeader;

    public LostLeadershipEvent(KafkaPartitionInfo partitionInfo, KafkaBroker oldLeader) {
        this.partitionInfo = partitionInfo;
        this.oldLeader = oldLeader;
    }
    
    public KafkaPartitionInfo getPartitionInfo() {
        return partitionInfo;
    }
    
    public KafkaBroker getOldLeader() {
        return oldLeader;
    }

    @Override
    public String toString() {
        return "LostLeadershipEvent [partitionInfo=" + partitionInfo + ", oldLeader=" + oldLeader + "]";
    }

}
