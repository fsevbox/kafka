package com.techfrog.springbootkafka.consumers;

import com.techfrog.springbootkafka.events.Greeting;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

@Component
public class MessageConsumer {

    private CountDownLatch messageLatch = new CountDownLatch(3);
    private CountDownLatch greetingLatch = new CountDownLatch(1);

    @KafkaListener(topics = "${kafka.topic.message}",
            containerFactory = "kafkaListenerContainerFactory",
            groupId = "group1")
    public void listenTopic(@Payload String message, @Header(KafkaHeaders.RECEIVED_PARTITION_ID) int partition) {
        System.out.println("Received message in topic1/group1: " + message + " from partition: " + partition);
        messageLatch.countDown();
    }

    @KafkaListener(topics = "${kafka.topic.message}",
            containerFactory = "filterKafkaListenerContainerFactory",
            groupId = "group2")
    public void listenTopicWithFiltering(String message) {
        System.out.println("Received filtered message in topic1/group2: " + message);
        messageLatch.countDown();
    }

    @KafkaListener(topics = "${kafka.topic.greeting}",
            containerFactory = "greetingKafkaListenerContainerFactory",
            groupId = "group1")
    public void listenForGreetings(Greeting message) {
        System.out.println("Received message in topicw/group1: " + message);
        greetingLatch.countDown();
    }

    public CountDownLatch getMessageLatch() {
        return messageLatch;
    }

    public CountDownLatch getGreetingLatch() {
        return greetingLatch;
    }
}
