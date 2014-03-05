package com.liquidm.kafkalo0.json;

import java.util.*;

/**
 * @see https://cwiki.apache.org/confluence/display/KAFKA/Kafka+data+structures+in+Zookeeper
 */
public class KafkaPartitionInfo {

    private String id;
    private int controller_epoch;
    private List<Integer> isr;
    private int leader;
    private int leader_epoch;
    private int version;

    public int getController_epoch() {
        return controller_epoch;
    }

    public void setController_epoch(int controller_epoch) {
        this.controller_epoch = controller_epoch;
    }

    public List<Integer> getIsr() {
        return isr;
    }

    public void setIsr(List<Integer> isr) {
        this.isr = isr;
    }

    public int getLeader() {
        return leader;
    }

    public void setLeader(int leader) {
        this.leader = leader;
    }

    public int getLeader_epoch() {
        return leader_epoch;
    }

    public void setLeader_epoch(int leader_epoch) {
        this.leader_epoch = leader_epoch;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return "KafkaPartitionInfo [id=" + id + ", controller_epoch=" + controller_epoch + ", isr=" + isr + ", leader=" + leader + ", leader_epoch="
                + leader_epoch + ", version=" + version + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        KafkaPartitionInfo other = (KafkaPartitionInfo) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
    
}
