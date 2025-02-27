package com.capstone.authServer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.capstone.authServer.dto.event.CreateTicketEvent;
import com.capstone.authServer.dto.event.UpdateTicketEvent;
import com.capstone.authServer.dto.event.payload.CreateTicketEventPayload;
import com.capstone.authServer.dto.event.payload.UpdateTicketEventPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class CreateTicketEventProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public CreateTicketEventProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void produce(String tenantId, String findingId, String summary, String description) {
        String topic = "job_ingestion_topic";
        CreateTicketEventPayload payload = new CreateTicketEventPayload(tenantId, findingId, summary, description);
        CreateTicketEvent event = new CreateTicketEvent(payload);
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, jsonPayload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
