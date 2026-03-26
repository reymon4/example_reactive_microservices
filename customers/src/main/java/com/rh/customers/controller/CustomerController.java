package com.rh.customers.controller;


import com.rh.customers.core.GenericResponse;
import com.rh.customers.service.ICustomerService;
import com.rh.customers.service.dto.CreateCustomerDTO;
import com.rh.customers.service.dto.GetCustomerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@RestController
@RequestMapping("/customers")
@CrossOrigin
@Slf4j
public class CustomerController {
    private final ICustomerService customerService;

    public CustomerController(ICustomerService customerService) {
        this.customerService = customerService;
    }

//    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
//    public Mono<GenericResponse<Mono<GetCustomerDTO>>> createCustomer(@RequestBody CreateCustomerDTO customerDTO, ServerHttpRequest request) {
//        return customerService.createCustomer(customerDTO)
//                .map(createdCustomer ->
//                        new GenericResponse<Mono<GetCustomerDTO>>(true, 201, "Customer created successfully!",request.getPath().value(), createdCustomer))
//                .subscribeOn(Schedulers.boundedElastic());
//    }

    @GetMapping(value = "/{identificationNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<GenericResponse<GetCustomerDTO>>> getCustomerByIdentificationNumber(@PathVariable String identificationNumber, ServerHttpRequest request) {
        log.info("Received request to get customer with identification number: {}", identificationNumber);
        return customerService.getCustomerByIdentificationNumber(identificationNumber)
                .map(customer -> new GenericResponse<>(true, 200, "Customer retrieved successfully!", request.getPath().value(), customer))
                .map(ResponseEntity::ok);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<GenericResponse> getAllCustomers(@RequestParam(required = false) Boolean state, ServerHttpRequest request) {
        return customerService.getAllCustomers(state)
                .collectList()
                .doOnNext(customers -> log.info("Customers retrieved: {}", customers))
                .map(customers ->
                        new GenericResponse(true, 200, "Customers retrieved successfully!",request.getPath().value(), customers))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
