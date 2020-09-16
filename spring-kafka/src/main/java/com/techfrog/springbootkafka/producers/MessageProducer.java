package com.techfrog.springbootkafka.producers;


import com.techfrog.springbootkafka.events.Greeting;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Component
public class MessageProducer {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value(value = "${kafka.topic.message}")
    private String messageTopic;

    @Autowired
    private KafkaTemplate<String, Greeting> greetingTemplate;

    @Value(value = "${kafka.topic.greeting}")
    private String greetingTopic;


    public void sendMessage(String message) {
        ListenableFuture<SendResult<String, String>> feature =
                kafkaTemplate.send(messageTopic, message);

        feature.addCallback(new ListenableFutureCallback<SendResult<String, String>>() {
            @Override
            public void onFailure(Throwable t) {
                System.out.println("Unable to send message=["
                        + message + "] to " + messageTopic + " due to : " + t.getMessage());
            }

            @Override
            public void onSuccess(SendResult<String, String> result) {
                System.out.println("Sent message=[" + message +
                        "] with offset=[" + result.getRecordMetadata().offset() + "] to " + messageTopic);
            }
        });
    }

    public void sendGreeting(Greeting greeting) {
        greetingTemplate.send(greetingTopic, greeting);
    }
}
