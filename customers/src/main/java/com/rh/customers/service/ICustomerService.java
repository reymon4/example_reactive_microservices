package com.rh.customers.service;

import com.rh.customers.service.dto.CreateCustomerDTO;
import com.rh.customers.service.dto.GetCustomerDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ICustomerService  {

    Mono<GetCustomerDTO> createCustomer(CreateCustomerDTO createCustomerDTO);

    Mono<GetCustomerDTO> getCustomerByIdentificationNumber(String customerIdentificationNumber);

    Flux<GetCustomerDTO> getAllCustomers(Boolean state);

    Mono<GetCustomerDTO> updateCustomer(CreateCustomerDTO customer);

    Mono<Void> deleteCustomer(String identificationNumber);



}
