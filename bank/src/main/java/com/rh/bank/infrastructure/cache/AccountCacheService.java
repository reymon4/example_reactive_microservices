package com.rh.bank.infrastructure.cache;

import com.rh.bank.application.service.dto.GetAccountDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Service
@Slf4j
public class AccountCacheService {

    private final ReactiveRedisTemplate<String, GetAccountDTO> redisTemplate;

    private static final String PREFIX = "account:by-number:";

    public AccountCacheService(ReactiveRedisTemplate<String, GetAccountDTO> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public Mono<GetAccountDTO> get(String accountNumber) {
        return redisTemplate.opsForValue()
                .get(PREFIX + accountNumber);
    }

    //TTL
    public Mono<Boolean> save(GetAccountDTO account) {
        return redisTemplate.opsForValue()
                .set(PREFIX + account.getNumber(), account, Duration.ofMinutes(10));
    }

    public Mono<Boolean> delete(String accountNumber) {
        return redisTemplate.delete(PREFIX + accountNumber).map(count -> count > 0);
    }
}