package com.rh.customers.repository;

import com.rh.customers.repository.model.CustomerEntity;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


public interface ICustomerRepository extends ReactiveCrudRepository<CustomerEntity, Long> {


    @Query("SELECT c FROM CustomerEntity c WHERE c.person.identificationNumber = ?1")
    Mono<CustomerEntity> findByIdentificationNumber(@Param("identificationNumber") String identificationNumber);

    @Query("SELECT c FROM CustomerEntity c WHERE c.state = ?1")
    Flux<CustomerEntity> findAllByState(@Param("state") Boolean state);

}
