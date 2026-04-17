package com.rh.bank.application.clients;

import com.rh.bank.application.clients.dto.GetCustomerDTO;
import com.rh.bank.domain.GenericResponse;
import com.rh.bank.infrastructure.config.WebClientConfig;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class CustomersRestClient {

    private final WebClientConfig webClientConfig;
    private final ReactiveCircuitBreaker circuitBreaker;

    public CustomersRestClient(
            WebClientConfig webClientConfig,
            ReactiveCircuitBreakerFactory<?, ?> circuitBreakerFactory
    ) {
        this.webClientConfig = webClientConfig;
        this.circuitBreaker = circuitBreakerFactory.create("customers-service");
    }

    public Mono<ResponseEntity<GenericResponse<GetCustomerDTO>>>
    getCustomerByIdentificationNumber(String identificationNumber) {

        return webClientConfig.customersWebClient()
                .get()
                .uri(uriBuilder -> uriBuilder.pathSegment(identificationNumber).build())
                .retrieve()
                .toEntity(new ParameterizedTypeReference<GenericResponse<GetCustomerDTO>>() {})
                .timeout(Duration.ofSeconds(4))
                .transform(mono ->
                        circuitBreaker.run(mono, this::fallback)
                );
    }

    private Mono<ResponseEntity<GenericResponse<GetCustomerDTO>>> fallback(Throwable ex) {

        GetCustomerDTO getCustomerDTO = new GetCustomerDTO();
        getCustomerDTO.setCustomerName("No available");
        getCustomerDTO.setIdentificationNumber("No available");
        getCustomerDTO.setPhone("No available");
        getCustomerDTO.setAddress("No available");
        getCustomerDTO.setState(false);

        GenericResponse<GetCustomerDTO> response = new GenericResponse<>(
                false,
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "Fallback - Customer service unavailable",
                null,
                getCustomerDTO
        );

        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response));
    }
}