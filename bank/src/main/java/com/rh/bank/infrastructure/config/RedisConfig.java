package com.rh.bank.infrastructure.config;


import com.rh.customers.application.service.dto.GetCustomerDTO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, GetCustomerDTO> redisTemplate(
            ReactiveRedisConnectionFactory factory) {
        JacksonJsonRedisSerializer<GetCustomerDTO> valueSerializer = new JacksonJsonRedisSerializer<>(GetCustomerDTO.class);

        RedisSerializationContext<String, GetCustomerDTO> context =
                RedisSerializationContext
                        .<String, GetCustomerDTO>newSerializationContext(new StringRedisSerializer())
                        .value(valueSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(factory, context);
    }
}
