package com.rh.customers.service;

import com.rh.customers.exception.domain.DuplicateResourceException;
import com.rh.customers.exception.domain.NotFoundException;
import com.rh.customers.repository.ICustomerRepository;
import com.rh.customers.repository.model.CustomerEntity;
import com.rh.customers.repository.model.PersonEntity;
import com.rh.customers.service.dto.CreateCustomerDTO;
import com.rh.customers.service.dto.GetCustomerDTO;
import com.rh.customers.service.dto.PersonDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;


@Service
@Slf4j
public class CustomerServiceImpl implements ICustomerService {

    private final ICustomerRepository customerRepository;

    public CustomerServiceImpl(ICustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }


    @Override
    public Mono<GetCustomerDTO> createCustomer(CreateCustomerDTO createCustomerDTO) {
        return Mono.fromCallable(() -> this.customerRepository.findByIdentificationNumber(createCustomerDTO.getPerson().getIdentificationNumber()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(customerEntity -> {
                    if (customerEntity != null) {
                        return Mono.error(new DuplicateResourceException(createCustomerDTO.getPerson().getIdentificationNumber()));
                    }
                    return Mono.fromCallable(() -> {
                        CustomerEntity aux = convertToEntity(createCustomerDTO);
                        this.customerRepository.save(aux);
                        return convertToDTO(aux);
                    }).subscribeOn(Schedulers.boundedElastic());
                });
    }

    @Override
    public Mono<GetCustomerDTO> getCustomerByIdentificationNumber(
            String customerIdentificationNumber) {
        return Mono.fromCallable(() ->
                        customerRepository.findByIdentificationNumber(customerIdentificationNumber)
                )
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty)
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Customer not found with identificationNumber={}", customerIdentificationNumber);
                    return Mono.error(new NotFoundException(customerIdentificationNumber));
                }))
                .map(this::convertToDTO)
                .onErrorMap(DataAccessException.class, ex -> {
                    log.error("Database error while retrieving customer identificationNumber={}", customerIdentificationNumber, ex);
                    return new DataAccessException("Error accessing the database", ex) {};
                });
    }


    @Override
    public Flux<GetCustomerDTO> getAllCustomers(Boolean state) {
        return Mono.fromCallable(() -> this.customerRepository.findAllByState(state))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(this::convertToDTO); // Simulate a delay to show the reactive behavior
    }

    @Override
    public Mono<GetCustomerDTO> updateCustomer(CreateCustomerDTO customer) {
        return Mono.fromCallable(() -> this.customerRepository.findByIdentificationNumber(customer.getPerson().getIdentificationNumber()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(customerEntity -> {
                    if (customerEntity != null) {
                        return Mono.fromCallable(() -> {
                            customer.setId(customerEntity.getId());
                            customer.getPerson().setId(customerEntity.getPerson().getId());
                            CustomerEntity aux = convertToEntity(customer);
                            this.customerRepository.saveAndFlush(aux);
                            return convertToDTO(aux);
                        }).subscribeOn(Schedulers.boundedElastic());

                    }
                    return Mono.error(new NotFoundException(customer.getPerson().getIdentificationNumber()));
                });
    }

    @Override
    public Mono<Void> deleteCustomer(String identificationNumber) {
        return Mono.fromCallable(() -> this.customerRepository.findByIdentificationNumber(identificationNumber))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(customerEntity -> {
                    if (customerEntity != null) {
                        return Mono.fromRunnable(() -> this.customerRepository.delete(customerEntity))
                                .subscribeOn(Schedulers.boundedElastic()).then();
                    }
                    return Mono.error(new NotFoundException(identificationNumber));
                });
    }

    private CustomerEntity convertToEntity(CreateCustomerDTO createCustomerDTO) {
        CustomerEntity cu = new CustomerEntity();
        cu.setId(createCustomerDTO.getId());
        cu.setState(createCustomerDTO.getState());
        cu.setPassword(createCustomerDTO.getPassword());
        PersonEntity pe = new PersonEntity();
        pe.setId(createCustomerDTO.getPerson().getId());
        pe.setAddress(createCustomerDTO.getPerson().getAddress());
        pe.setName(createCustomerDTO.getPerson().getName());
        pe.setGender(createCustomerDTO.getPerson().getGender());
        pe.setIdentificationNumber(createCustomerDTO.getPerson().getIdentificationNumber());
        pe.setPhone(createCustomerDTO.getPerson().getPhone());
        return cu;
    }

    private GetCustomerDTO convertToDTO(CustomerEntity customerEntity) {
        GetCustomerDTO getCustomerDTO = new GetCustomerDTO();
        getCustomerDTO.setId(customerEntity.getId());
        PersonDTO personDTO = new PersonDTO();
        personDTO.setName(customerEntity.getPerson().getName());
        personDTO.setIdentificationNumber(customerEntity.getPerson().getIdentificationNumber());
        personDTO.setName(customerEntity.getPerson().getName());
        personDTO.setGender(customerEntity.getPerson().getGender());
        personDTO.setAddress(customerEntity.getPerson().getAddress());
        personDTO.setPhone(customerEntity.getPerson().getPhone());
        getCustomerDTO.setPerson(personDTO);
        return getCustomerDTO;
    }

    private GetCustomerDTO convertCreateToDTO(CreateCustomerDTO createCustomerDTO) {
        GetCustomerDTO getCustomerDTO = new GetCustomerDTO();
        getCustomerDTO.setId(createCustomerDTO.getId());
        PersonDTO personDTO = new PersonDTO();
        personDTO.setName(createCustomerDTO.getPerson().getName());
        personDTO.setIdentificationNumber(createCustomerDTO.getPerson().getIdentificationNumber());
        personDTO.setName(createCustomerDTO.getPerson().getName());
        personDTO.setGender(createCustomerDTO.getPerson().getGender());
        personDTO.setAddress(createCustomerDTO.getPerson().getAddress());
        personDTO.setPhone(createCustomerDTO.getPerson().getPhone());
        getCustomerDTO.setPerson(personDTO);
        return getCustomerDTO;
    }


}
