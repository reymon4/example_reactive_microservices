package com.rh.customers.service;

import com.rh.customers.cache.CustomerCacheService;
import com.rh.customers.event.producer.CustomerEvent;
import com.rh.customers.event.producer.CustomerEventProducer;
import com.rh.customers.exception.db.DuplicateResourceException;
import com.rh.customers.exception.db.IllegalArgumentsException;
import com.rh.customers.exception.domain.NotFoundException;
import com.rh.customers.repository.ICustomerRepository;
import com.rh.customers.repository.model.CustomerEntity;
import com.rh.customers.repository.model.PersonEntity;
import com.rh.customers.service.dto.CreateCustomerDTO;
import com.rh.customers.service.dto.GetCustomerDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


@Service
@Slf4j
public class CustomerServiceImpl implements ICustomerService {

    private final ICustomerRepository customerRepository;
    private final CustomerCacheService customerCacheService;
    private final CustomerEventProducer customerEventProducer;

    public CustomerServiceImpl(ICustomerRepository customerRepository, CustomerCacheService customerCacheService, CustomerEventProducer customerEventProducer) {
        this.customerCacheService = customerCacheService;
        this.customerRepository = customerRepository;
        this.customerEventProducer = customerEventProducer;
    }


    @Override
    public Mono<GetCustomerDTO> createCustomer(CreateCustomerDTO createCustomerDTO) {
        String identificationNumber = createCustomerDTO.getPerson().getIdentificationNumber();
        return Mono.fromCallable(() -> customerRepository.save(convertToEntity(createCustomerDTO))).subscribeOn(Schedulers.boundedElastic())
                .map(this::convertToDTO)
                .flatMap(customer -> customerCacheService.save(customer)
                        .onErrorResume(ex -> {
            log.warn("Cache failed creating customer with identificationNumber={}", identificationNumber, ex);
            return Mono.empty();
        }).then(customerEventProducer.sendCustomerEvent("customer-created", new CustomerEvent(customer.getIdentificationNumber(), customer.getCustomerName(), customer.getState()), customer.getIdentificationNumber())).onErrorResume(ex -> {
            log.warn("Event publish failed for customer with identificationNumber={}", identificationNumber, ex);
            return Mono.empty();
        }).thenReturn(customer)).onErrorMap(DataIntegrityViolationException.class, ex -> {
            String message = ex.getMostSpecificCause().getMessage();

            if (message != null && message.contains("unique")) {
                log.warn("Duplicate customer identificationNumber={}", identificationNumber);
                return new DuplicateResourceException("Customers", identificationNumber);
            }

            if (message != null && message.contains("null value")) {
                log.error("Null constraint violation identificationNumber={}", identificationNumber, ex);
                return new IllegalArgumentsException(identificationNumber, message);
            }

            log.error("Data integrity violation identificationNumber={}", identificationNumber, ex);
            return ex;
        });
    }

    @Override
    public Mono<GetCustomerDTO> getCustomerByIdentificationNumber(String customerIdentificationNumber) {
        return customerCacheService.get(customerIdentificationNumber)
                .switchIfEmpty(
                        Mono.fromCallable(() ->
                                        customerRepository.findByIdentificationNumber(customerIdentificationNumber))
                                .subscribeOn(Schedulers.boundedElastic())
                                .switchIfEmpty(Mono.defer(() -> {
                                    log.info("Customer not found with identificationNumber={}", customerIdentificationNumber);
                                    return Mono.error(new NotFoundException(customerIdentificationNumber));
                                }))
                                //No reactive operation
                                .map(this::convertToDTO)
                                .flatMap(customer ->
                                        customerCacheService.save(customer)
                                                .onErrorResume(ex -> {
                                                    log.warn("Cache failed for customer with identificationNumber={}", customerIdentificationNumber, ex);
                                                    return Mono.empty();
                                                }).thenReturn(customer)));
    }


    @Override
    public Mono<Page<GetCustomerDTO>> getAllCustomers(Boolean state, Integer page, Integer size, String sorting) {
        var safepage = page != null && page > 0 ? page : 1;
        var safesize = size != null && size > 0 ? size : 10;
        var sortingField = sorting != null && !sorting.isEmpty() ? sorting : "id";
        Pageable pageable = PageRequest.of(safepage - 1, safesize, Sort.by(sortingField).descending());
        return Mono.fromCallable(() -> customerRepository.findAllByState(state, pageable)).subscribeOn(Schedulers.boundedElastic()).map(customerPage -> customerPage.map(this::convertToDTO)).onErrorMap(DataAccessException.class, ex -> {
            log.error("Database error while retrieving customers page={}, size={}, state={}", page, size, state, ex);
            return new DataAccessException("Error accessing the database", ex) {
            };
        });
    }

    @Override
    public Mono<GetCustomerDTO> updateCustomer(CreateCustomerDTO customer, String identificationNumber) {
        return Mono.fromCallable(() -> this.customerRepository.findByIdentificationNumber(identificationNumber)).subscribeOn(Schedulers.boundedElastic()).flatMap(Mono::justOrEmpty).switchIfEmpty(Mono.defer(() -> {
                    log.warn("Customer not found for update identificationNumber={}", identificationNumber);
                    return Mono.error(new NotFoundException(identificationNumber));
                })).map(existingCustomer -> {
                    updateEntity(existingCustomer, customer);
                    return existingCustomer;
                })

                .flatMap(c -> Mono.fromCallable(() ->
                                this.customerRepository.save(c))
                        .subscribeOn(Schedulers.boundedElastic()))
                .map(this::convertToDTO)
                .flatMap(updatedCustomer
                        -> customerCacheService.delete(identificationNumber)
                        //.doOnSuccess(d -> log.debug("Cache invalidated id={}", identificationNumber))
                        .onErrorResume(ex -> {
                            log.warn("Cache delete failed id={}", identificationNumber, ex);
                            return Mono.empty();
                        }).thenReturn(updatedCustomer))

                .onErrorMap(DataIntegrityViolationException.class, ex -> {
                    String message = ex.getMostSpecificCause().getMessage();

                    if (message != null && message.contains("unique")) {
                        log.warn("Duplicate customer identificationNumber={}", identificationNumber);
                        return new DuplicateResourceException("Customers", identificationNumber);
                    }

                    if (message != null && message.contains("null value")) {
                        log.error("Null constraint violation identificationNumber={}", identificationNumber, ex);
                        return new IllegalArgumentsException(identificationNumber, message);
                    }

                    log.error("Data integrity violation identificationNumber={}", identificationNumber, ex);
                    return ex;
                });
    }

    @Override
    public Mono<Void> deleteCustomer(String identificationNumber) {
        return Mono.fromCallable(() -> this.customerRepository.findByIdentificationNumber(identificationNumber))
                .subscribeOn(Schedulers.boundedElastic())
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Customer not found for delete identificationNumber={}", identificationNumber);
                    return Mono.error(new NotFoundException(identificationNumber));
                })).flatMap(customer ->
                        Mono.fromRunnable(() -> this.customerRepository.delete(customer))
                                .subscribeOn(Schedulers.boundedElastic()))
                .then(customerCacheService.delete(identificationNumber)
                        //.doOnSuccess(d -> log.debug("Cache deleted id={}", identificationNumber))
                        .onErrorResume(ex -> {
                            log.warn("Cache failed id={}", identificationNumber, ex);
                            return Mono.empty();

                        }).then(customerEventProducer.sendCustomerEvent("customer-deleted", new CustomerEvent(null, null, null), identificationNumber)
                                .onErrorResume(ex -> {
                            log.warn("Event publish failed for deleted customer with identificationNumber={}", identificationNumber, ex);
                            return Mono.empty();
                        }))
                )
                .then();
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
        cu.setPerson(pe);
        return cu;
    }

    private GetCustomerDTO convertToDTO(CustomerEntity customerEntity) {
        GetCustomerDTO getCustomerDTO = new GetCustomerDTO();
        getCustomerDTO.setId(customerEntity.getId());
        getCustomerDTO.setAddress(customerEntity.getPerson().getAddress());
        getCustomerDTO.setState(customerEntity.getState());
        getCustomerDTO.setCustomerName(customerEntity.getPerson().getName());
        getCustomerDTO.setPhone(customerEntity.getPerson().getPhone());
        getCustomerDTO.setIdentificationNumber(customerEntity.getPerson().getIdentificationNumber());
        return getCustomerDTO;
    }

    //NO update identification number, only update other fields
    private CustomerEntity updateEntity(CustomerEntity existingEntity, CreateCustomerDTO updateDTO) {
        existingEntity.setState(updateDTO.getState() != null ? updateDTO.getState() : existingEntity.getState());
        existingEntity.setPassword(updateDTO.getPassword() != null ? updateDTO.getPassword() : existingEntity.getPassword());
        existingEntity.setState(updateDTO.getState() != null ? updateDTO.getState() : existingEntity.getState());
        existingEntity.getPerson().setName(updateDTO.getPerson().getName() != null ? updateDTO.getPerson().getName() : existingEntity.getPerson().getName());
        existingEntity.getPerson().setGender(updateDTO.getPerson().getGender() != null ? updateDTO.getPerson().getGender() : existingEntity.getPerson().getGender());
        existingEntity.getPerson().setAddress(updateDTO.getPerson().getAddress() != null ? updateDTO.getPerson().getAddress() : existingEntity.getPerson().getAddress());
        existingEntity.getPerson().setPhone(updateDTO.getPerson().getPhone() != null ? updateDTO.getPerson().getPhone() : existingEntity.getPerson().getPhone());
        return existingEntity;
    }


}
