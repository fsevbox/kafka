package com.techfrog.springbootkafka;

import com.techfrog.springbootkafka.consumers.MessageConsumer;
import com.techfrog.springbootkafka.events.Greeting;
import com.techfrog.springbootkafka.producers.MessageProducer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class SpringBootKafkaApplication {

    public static void main(String[] args) throws InterruptedException {
        ConfigurableApplicationContext context = SpringApplication.run(SpringBootKafkaApplication.class, args);

        MessageProducer producer = context.getBean(MessageProducer.class);
        MessageConsumer consumer = context.getBean(MessageConsumer.class);

        producer.sendMessage("Hello");
        producer.sendMessage("Hello World");
        consumer.getMessageLatch().await();

        producer.sendGreeting(new Greeting("Greetings"));
        consumer.getGreetingLatch().await();

        context.close();
    }
}
