package com.capstone.authServer.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.capstone.authServer.dto.ScanEventDTO;

/**
 * Produces (publishes) ScanEventDTO messages to the "scan-topic".
 */
@Service
public class ScanEventProducerService {

    private final KafkaTemplate<String, ScanEventDTO> kafkaTemplate;

    @Autowired
    public ScanEventProducerService(KafkaTemplate<String, ScanEventDTO> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishScanEvent(ScanEventDTO dto) {
        String topic = "scan-topic";
        kafkaTemplate.send(topic, dto);
    }
}
