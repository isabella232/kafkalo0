package com.liquidm.kafkalo0.json;

import java.util.*;

/**
 * @see https://cwiki.apache.org/confluence/display/KAFKA/Kafka+data+structures+in+Zookeeper
 */
public class KafkaTopicInfo {

    private Map<String, List<Integer>> partitions;
    private int version;

    public Map<String, List<Integer>> getPartitions() {
        return partitions;
    }

    public void setPartitions(Map<String, List<Integer>> partitions) {
        this.partitions = partitions;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((partitions == null) ? 0 : partitions.hashCode());
        result = prime * result + version;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KafkaTopicInfo other = (KafkaTopicInfo) obj;
        if (partitions == null) {
            if (other.partitions != null)
                return false;
        } else if (!partitions.equals(other.partitions))
            return false;
        if (version != other.version)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "KafkaTopicInfo [partitions=" + partitions + ", version=" + version + "]";
    }

}
