package com.liquidm.kafkalo0.events;

import com.liquidm.kafkalo0.*;

public class BrokerUpdatedEvent {

    private final KafkaBroker broker;

    public BrokerUpdatedEvent(KafkaBroker broker) {
        this.broker = broker;
    }
    
    public KafkaBroker getBroker() {
        return broker;
    }

    @Override
    public String toString() {
        return "BrokerUpdatedEvent [broker=" + broker + "]";
    }

}
