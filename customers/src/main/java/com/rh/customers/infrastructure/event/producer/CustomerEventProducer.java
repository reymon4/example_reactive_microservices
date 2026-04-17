package com.rh.customers.infrastructure.event.producer;


import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class CustomerEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "customer-events";


    public CustomerEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<Void> sendCustomerEvent(CustomerEvent event, String identificationNumber) {
        return Mono.fromRunnable(() -> {
            kafkaTemplate.send(TOPIC, identificationNumber, event);
        }).subscribeOn(Schedulers.boundedElastic())
          .then();
    }
}