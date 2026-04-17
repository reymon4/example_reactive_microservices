package com.rh.customers.application.service;

import com.rh.customers.application.service.dto.CreateCustomerDTO;
import com.rh.customers.application.service.dto.GetCustomerDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Mono;

public interface ICustomerService  {

    Mono<GetCustomerDTO> createCustomer(CreateCustomerDTO createCustomerDTO);

    Mono<GetCustomerDTO> getCustomerByIdentificationNumber(String customerIdentificationNumber);

    Mono<Page<GetCustomerDTO>> getAllCustomers(Boolean state, Integer page, Integer size, String sorting);

    Mono<GetCustomerDTO> updateCustomer(CreateCustomerDTO customer, String identificationNumber);

    Mono<Void> deleteCustomer(String identificationNumber);



}
