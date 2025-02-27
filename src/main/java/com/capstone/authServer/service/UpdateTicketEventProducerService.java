package com.capstone.authServer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.capstone.authServer.dto.event.UpdateTicketEvent;
import com.capstone.authServer.dto.event.payload.UpdateTicketEventPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class UpdateTicketEventProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public UpdateTicketEventProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void produce(String ticketId, String tenantId) {
        String topic = "job_ingestion_topic";
        UpdateTicketEventPayload payload = new UpdateTicketEventPayload(tenantId, ticketId);
        UpdateTicketEvent event = new UpdateTicketEvent(payload);
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, jsonPayload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
