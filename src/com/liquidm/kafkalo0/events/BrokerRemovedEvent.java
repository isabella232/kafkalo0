package com.liquidm.kafkalo0.events;

import com.liquidm.kafkalo0.*;


public class BrokerRemovedEvent {

    private final KafkaBroker broker;

    public BrokerRemovedEvent(KafkaBroker brokerInfo) {
        this.broker = brokerInfo;
    }

    public KafkaBroker getBroker() {
        return broker;
    }

    @Override
    public String toString() {
        return "BrokerRemovedEvent [broker=" + broker + "]";
    }

}
