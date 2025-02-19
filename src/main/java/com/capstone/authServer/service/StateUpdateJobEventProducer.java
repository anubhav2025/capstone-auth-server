package com.capstone.authServer.service;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.capstone.authServer.dto.event.StateUpdateJobEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class StateUpdateJobEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public StateUpdateJobEventProducer(KafkaTemplate<String, String> kafkaTemplate,
                                       ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void produce(StateUpdateJobEvent StateUpdateJobEvent) {
        try {
            String json = objectMapper.writeValueAsString(StateUpdateJobEvent);
            kafkaTemplate.send("job_ingestion_topic",
                               StateUpdateJobEvent.getEventId(),
                               json);
            System.out.println("1. StateUpdateJobEvent produced at AuthServer, id: " + StateUpdateJobEvent.getEventId());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}