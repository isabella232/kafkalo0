package com.liquidm.kafkalo0.events;

import com.liquidm.kafkalo0.*;


public class BrokerAddedEvent {

    private final KafkaBroker broker;

    public BrokerAddedEvent(KafkaBroker broker) {
        this.broker = broker;
    }
    
    public KafkaBroker getBroker() {
        return broker;
    }

    @Override
    public String toString() {
        return "BrokerAddedEvent [broker=" + broker + "]";
    }

}
