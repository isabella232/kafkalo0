package com.liquidm.kafkalo0.json;

/**
 * @see https://cwiki.apache.org/confluence/display/KAFKA/Kafka+data+structures+in+Zookeeper
 */
public class KafkaBrokerInfo {
    
    private String host;
    private int port;
    private int jmx_port;
    private long timestamp;
    private int version;
    private int id;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getJmx_port() {
        return jmx_port;
    }

    public void setJmx_port(int jmx_port) {
        this.jmx_port = jmx_port;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }
    
    @Override
    public String toString() {
        return "KafkaBrokerInfo [host=" + host + ", port=" + port + ", jmx_port=" + jmx_port + ", timestamp=" + timestamp + ", version=" + version + "]";
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public int getId() {
        return id;
    }

    public void updateFrom(KafkaBrokerInfo other) {
        setTimestamp(other.getTimestamp());
        setVersion(other.getVersion());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + id;
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
        KafkaBrokerInfo other = (KafkaBrokerInfo) obj;
        if (id != other.id)
            return false;
        return true;
    }
    
}