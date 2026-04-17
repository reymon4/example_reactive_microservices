package com.rh.bank.infrastructure.event.producer;


import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class AccountEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "account-events";

    public AccountEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<Void> sendAccountEvent(AccountEvent event, String account) {
        return Mono.fromRunnable(() -> {
            kafkaTemplate.send(TOPIC, account, event);
        }).subscribeOn(Schedulers.boundedElastic())
          .then();
    }
}