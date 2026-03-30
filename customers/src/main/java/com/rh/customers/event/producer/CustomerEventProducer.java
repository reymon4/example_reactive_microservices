package com.rh.customers.event.producer;


import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CustomerEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;


    public CustomerEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendCustomerCreatedEvent(String topic, CustomerEvent event) {

        kafkaTemplate.send(topic, event.getIdentificationNumber(), event);
        log.info("Event sent to Kafka topic={} id={}", topic, event.getIdentificationNumber());
    }
}