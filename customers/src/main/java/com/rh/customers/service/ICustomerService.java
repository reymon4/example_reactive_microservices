package com.rh.customers.service;

import com.rh.customers.service.dto.CustomerDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface ICustomerService  {

    Mono<CustomerDTO> createCustomer(CustomerDTO customerDTO);

    Mono<CustomerDTO> getCustomerByIdentificationNumber(String customerIdentificationNumber);

    Flux<CustomerDTO> getAllCustomers(Boolean state);

    void updateCustomer(CustomerDTO customer);

    void deleteCustomer(String identificationNumber);



}
