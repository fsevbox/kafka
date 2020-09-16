package com.techfrog.springbootkafka.producers;

import com.techfrog.springbootkafka.configs.KafkaProducerConfig;
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
@EmbeddedKafka(partitions = 1, topics = {"${kafka.topic.message}"})
public class MessageProducerTest {

    @Autowired
    private EmbeddedKafkaBroker kafkaBroker;

    @Autowired
    private MessageProducer producer;

    @Value("${kafka.topic.message}")
    private String messageTopic;

    private KafkaMessageListenerContainer<String, String> listener;
    private BlockingQueue<ConsumerRecord<String, String>> records;

    @Test
    public void sendMessage() throws InterruptedException {
        producer.sendMessage("hello");

        ConsumerRecord<String, String> response = records.poll(10, TimeUnit.SECONDS);
        assertThat(response.value(), equalTo("hello"));
    }

    @BeforeEach
    public void setUp() {
        records = new LinkedBlockingQueue<>();

        ContainerProperties containerProperties = new ContainerProperties(messageTopic);
        Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps(
                "test", "false", kafkaBroker);

        DefaultKafkaConsumerFactory<String, String> consumer = new DefaultKafkaConsumerFactory<>(consumerProperties);

        listener = new KafkaMessageListenerContainer<>(consumer, containerProperties);
        listener.setupMessageListener((MessageListener<String, String>) record -> {
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