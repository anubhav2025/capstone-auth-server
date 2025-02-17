package com.capstone.authServer.service;

import com.capstone.authServer.dto.event.ScanRequestEvent;
import com.capstone.authServer.enums.ToolTypes;
import com.capstone.authServer.dto.event.payload.ScanRequestEventPayload;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ScanEventProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public ScanEventProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishScanEvent(ToolTypes tool, String tenantId) {
        String topic = "jfc_auth";
        ScanRequestEventPayload payload = new ScanRequestEventPayload(tool, tenantId);
        ScanRequestEvent event = new ScanRequestEvent(payload);
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, jsonPayload);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
