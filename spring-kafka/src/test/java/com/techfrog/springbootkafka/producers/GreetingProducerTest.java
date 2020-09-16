package com.techfrog.springbootkafka.producers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.techfrog.springbootkafka.configs.KafkaProducerConfig;
import com.techfrog.springbootkafka.events.Greeting;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@SpringJUnitConfig
@DirtiesContext
@SpringBootTest(classes = {MessageProducer.class, KafkaProducerConfig.class})
@EmbeddedKafka(partitions = 1, topics = {"${kafka.topic.greeting}"})
public class GreetingProducerTest {

    @Autowired
    private EmbeddedKafkaBroker kafkaBroker;

    @Autowired
    private MessageProducer producer;

    @Value("${kafka.topic.greeting}")
    private String greetingTopic;

    private KafkaMessageListenerContainer<String, Greeting> listener;
    private BlockingQueue<ConsumerRecord<String, Greeting>> records;

    @Test
    public void sendGreeting() throws InterruptedException, JsonProcessingException {
        Greeting greeting = new Greeting("Greetings!");
        producer.sendGreeting(greeting);

        ObjectMapper mapper = new ObjectMapper();
        String value = mapper.writeValueAsString(greeting);

        ConsumerRecord<String, Greeting> response = records.poll(10, TimeUnit.SECONDS);
        assertThat(response.value(), equalTo(value));
    }

    @BeforeEach
    public void setUp() {
        records = new LinkedBlockingQueue<>();

        ContainerProperties containerProperties = new ContainerProperties(greetingTopic);
        Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps(
                "test", "false", kafkaBroker);

        DefaultKafkaConsumerFactory<String, Greeting> consumer = new DefaultKafkaConsumerFactory<>(consumerProperties);

        listener = new KafkaMessageListenerContainer<>(consumer, containerProperties);
        listener.setupMessageListener((MessageListener<String, Greeting>) record -> {
            records.add(record);
        });
        listener.start();

        ContainerTestUtils.waitForAssignment(listener, kafkaBroker.getPartitionsPerTopic());
    }

    @AfterEach
    public void tearDown() {
        listener.stop();
    }

}