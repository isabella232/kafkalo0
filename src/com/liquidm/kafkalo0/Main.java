package com.liquidm.kafkalo0;

import org.apache.curator.framework.*;
import org.apache.curator.framework.api.*;
import org.apache.curator.retry.*;
import org.slf4j.*;

import com.google.common.eventbus.*;
import com.liquidm.kafkalo0.events.*;

/**
 * This is just a test app. Yeah, you wish it was a JUnit, but get real!
 */
public class Main {
    
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        
        String zookeeper = args.length > 0 ? args[0] : "localhost:2181";
        String topic = args.length > 1 ? args[1] : "mytopic";
        
        log.info("Zookeeper connection string: " + zookeeper);
        log.info("Topic: " + topic);
        
        CuratorFramework curatorClient = CuratorFrameworkFactory.newClient(zookeeper, new ExponentialBackoffRetry(1000, 3));
        curatorClient.start();
        curatorClient.getUnhandledErrorListenable().addListener(new UnhandledErrorListener() {
            @Override
            public void unhandledError(String message, Throwable e) {
                log.warn(message, e);
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
        KafkaZookeeperClient client = new KafkaZookeeperClient(curatorClient, eventBus, topic);
        client.start();
        Thread.sleep(Long.MAX_VALUE);
    
    
    }

}
