package com.capstone.authServer.config;

import com.capstone.authServer.dto.ScanEventDTO;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {

    /**
     * Create a topic named "scan-topic" with default 3 partitions, 1 replica.
     * Adjust as needed for your environment.
     */
    @Bean
    public NewTopic scanTopic() {
        return TopicBuilder.name("scan-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    /**
     * Producer configs for sending ScanEventDTO messages
     */
    @Bean
    public ProducerFactory<String, ScanEventDTO> producerFactory() {
        Map<String, Object> props = new HashMap<>();
        // Typical producer settings
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        // Use the Spring Kafka JsonSerializer
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // We can disable type headers if we want a decoupled approach:
        props.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    public KafkaTemplate<String, ScanEventDTO> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}