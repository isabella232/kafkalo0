package com.liquidm.kafkalo0;

import java.net.*;

import com.liquidm.kafkalo0.json.*;

public class KafkaBroker {

    private KafkaBrokerInfo info;
    private InetAddress inetAddress;

    public KafkaBroker(KafkaBrokerInfo info) {
        this.info = info;
    }
    
    public boolean isLocal() {
        try {
            InetAddress inet = getInetAddress();
            return inet.isLoopbackAddress() || inet.isAnyLocalAddress() || NetworkInterface.getByInetAddress(inet) != null;
        } catch (UnknownHostException | SocketException e) {
            throw new RuntimeException(e);
        }
    }

    private InetAddress getInetAddress() throws UnknownHostException {
        if (inetAddress == null) {
            inetAddress = InetAddress.getByName(info.getHost());
        }
        return inetAddress;
    }

    public boolean isSameHost(String otherHost) {
        try {
            return getInetAddress().equals(InetAddress.getByName(otherHost));
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        }
    }

//    public boolean isSameBroker(Broker leader) {
//        return info.getPort() == leader.port() && isSameHost(leader.host());
//    }

    public void updateFrom(KafkaBrokerInfo info) {
        this.info.updateFrom(info);
    }

    public KafkaBrokerInfo getInfo() {
        return info;
    }

    @Override
    public String toString() {
        return "KafkaBroker [inetAddress=" + inetAddress + ", info=" + info + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((info == null) ? 0 : info.hashCode());
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
        KafkaBroker other = (KafkaBroker) obj;
        if (info == null) {
            if (other.info != null)
                return false;
        } else if (!info.equals(other.info))
            return false;
        return true;
    }

}
