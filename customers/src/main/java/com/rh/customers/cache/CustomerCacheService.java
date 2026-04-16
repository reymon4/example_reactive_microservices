package com.rh.customers.cache;

import com.rh.customers.service.dto.GetCustomerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class CustomerCacheService {

    private final ReactiveRedisTemplate<String, GetCustomerDTO> redisTemplate;

    private static final String PREFIX = "customer:by-identification-number:";

    public CustomerCacheService(ReactiveRedisTemplate<String, GetCustomerDTO> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<GetCustomerDTO> get(String identificationNumber) {
        return redisTemplate.opsForValue()
                .get(PREFIX + identificationNumber);
    }

    //TTL
    public Mono<Boolean> save(GetCustomerDTO customer) {
        return redisTemplate.opsForValue()
                .set(PREFIX + customer.getIdentificationNumber(), customer, Duration.ofMinutes(10));
    }

    public Mono<Boolean> delete(String identificationNumber) {
        return redisTemplate.delete(PREFIX + identificationNumber).map(count -> count > 0);
    }
}