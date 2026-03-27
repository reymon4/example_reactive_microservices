package com.rh.customers.controller;


import com.rh.customers.core.GenericResponse;
import com.rh.customers.service.ICustomerService;
import com.rh.customers.service.dto.CreateCustomerDTO;
import com.rh.customers.service.dto.GetCustomerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;



@RestController
@RequestMapping("/customers")
@CrossOrigin
@Slf4j
public class CustomerController {
    private final ICustomerService customerService;

    public CustomerController(ICustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<GenericResponse<GetCustomerDTO>>> createCustomer(@RequestBody CreateCustomerDTO customerDTO, ServerHttpRequest request) {
        log.info("Received request to create customer with identification number: {}", customerDTO.getPerson().getIdentificationNumber());
        return customerService.createCustomer(customerDTO)
                .map(customer -> new GenericResponse<>
                        (true, HttpStatus.CREATED.value(), "Customer created successfully!", request.getPath().value(), customer))
                .map(response -> ResponseEntity.created(request.getURI()).body(response));
    }

    @GetMapping(value = "/{identificationNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<GenericResponse<GetCustomerDTO>>> getCustomerByIdentificationNumber(@PathVariable String identificationNumber, ServerHttpRequest request) {
        log.info("Received request to get customer with identification number: {}", identificationNumber);
        return customerService.getCustomerByIdentificationNumber(identificationNumber)
                .map(customer -> new GenericResponse<>
                        (true, 200, "Customer retrieved successfully!", request.getPath().value(), customer))
                .map(ResponseEntity::ok);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<GenericResponse<Page<GetCustomerDTO>>>> getAllCustomers(@RequestParam Boolean state,
                                                                      @RequestParam Integer page, @RequestParam Integer Size, @RequestParam (required = false) String sorting, ServerHttpRequest request) {
        log.debug("Received request to get all customers with state: {}, page: {}, size: {}, sorting: {}", state, page, Size, sorting);
        return customerService.getAllCustomers(state, page, Size, sorting)
                .map(customers -> new GenericResponse<>
                        (true, 200, "Customers retrieved successfully!", request.getPath().value(), customers))
                .map(ResponseEntity::ok);
    }

    @PutMapping(value = "/{identificationNumber}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<GenericResponse<GetCustomerDTO>>> updateCustomer(@RequestBody CreateCustomerDTO customerDTO, @PathVariable String identificationNumber, ServerHttpRequest request) {
        log.info("Received request to update customer with identification number: {}", identificationNumber);
        return customerService.updateCustomer(customerDTO, identificationNumber)
                .map(customer -> new GenericResponse<>
                        (true, 200, "Customer updated successfully!", request.getPath().value(), customer))
                .map(ResponseEntity::ok);
    }

    @DeleteMapping(value = "/{identificationNumber}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<ResponseEntity<GenericResponse<Void>>> deleteCustomer(@PathVariable String identificationNumber, ServerHttpRequest request) {
        log.info("Received request to delete customer with identification number: {}", identificationNumber);
        return customerService.deleteCustomer(identificationNumber)
                .thenReturn(ResponseEntity.ok(
                        new GenericResponse<>(
                                true,
                                200,
                                "Customer deleted successfully!",
                                request.getPath().value(),
                                null
                        )
                ));
    }
}
