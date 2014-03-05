package com.liquidm.kafkalo0;

import org.apache.curator.framework.*;
import org.apache.curator.framework.api.*;
import org.apache.curator.retry.*;

import com.google.common.eventbus.*;
import com.liquidm.kafkalo0.events.*;

/**
 * This is just a test app. Yeah, you wish it was a JUnit, but get real!
 */
public class Main {

    public static void main(String[] args) throws Exception {
        
        CuratorFramework curatorClient = CuratorFrameworkFactory.newClient("localhost:2181", new ExponentialBackoffRetry(1000, 3));
        curatorClient.start();
        curatorClient.getUnhandledErrorListenable().addListener(new UnhandledErrorListener() {
            @Override
            public void unhandledError(String message, Throwable e) {
                e.printStackTrace();
            }
        });
        EventBus eventBus = new EventBus();
        eventBus.register(new Object() {
            @Subscribe
            public void handleAdds(GainLeadershipEvent event) {
                System.out.println("----------> "+ event);
            }
            @Subscribe
            public void handleAdds(LostLeadershipEvent event) {
                System.out.println("----------> "+ event);
            }
        });
        KafkaZookeeperClient client = new KafkaZookeeperClient(curatorClient, eventBus, "mytopic");
        client.start();
        Thread.sleep(Long.MAX_VALUE);
    
    
    }

}
