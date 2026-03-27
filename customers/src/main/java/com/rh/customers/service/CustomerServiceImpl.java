package com.rh.customers.service;
import com.rh.customers.exception.domain.NotFoundException;
import com.rh.customers.repository.ICustomerRepository;
import com.rh.customers.repository.model.CustomerEntity;
import com.rh.customers.repository.model.PersonEntity;
import com.rh.customers.service.dto.CreateCustomerDTO;
import com.rh.customers.service.dto.GetCustomerDTO;
import com.rh.customers.service.dto.PersonDTO;
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

    public CustomerServiceImpl(ICustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }


    @Override
    public Mono<GetCustomerDTO> createCustomer(CreateCustomerDTO createCustomerDTO) {
        String identificationNumber = createCustomerDTO.getPerson().getIdentificationNumber();
        return Mono.fromCallable(() ->
                        customerRepository.save(convertToEntity(createCustomerDTO))
                )
                .subscribeOn(Schedulers.boundedElastic())
                .map(this::convertToDTO)
                .onErrorMap(DataIntegrityViolationException.class, ex -> {
                    log.warn("Constraint violation while creating customer identificationNumber={}", identificationNumber, ex);
                    return new DataIntegrityViolationException("Data integrity violation for customer with identification number: " + identificationNumber, ex) {
                    };
                })
                .onErrorMap(DataAccessException.class, ex -> {
                    log.error("Database error while creating customer identificationNumber={}", identificationNumber, ex);
                    return new DataAccessException("Error accessing the database", ex) {
                    };
                });
    }

    @Override
    public Mono<GetCustomerDTO> getCustomerByIdentificationNumber(
            String customerIdentificationNumber) {
        return Mono.fromCallable(() ->
                        customerRepository.findByIdentificationNumber(customerIdentificationNumber)
                )
                .subscribeOn(Schedulers.boundedElastic())
                // Reactive operation 
                .flatMap(Mono::justOrEmpty)
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Customer not found with identificationNumber={}", customerIdentificationNumber);
                    return Mono.error(new NotFoundException(customerIdentificationNumber));
                }))
                //No reactive operation
                .map(this::convertToDTO)
                .onErrorMap(DataAccessException.class, ex -> {
                    log.error("Database error while retrieving customer identificationNumber={}", customerIdentificationNumber, ex);
                    return new DataAccessException("Error accessing the database", ex) {
                    };
                });
    }


    @Override
    public Mono<Page<GetCustomerDTO>> getAllCustomers(Boolean state, Integer page, Integer size, String sorting) {
        var safepage = page != null && page > 0 ? page : 1;
        var safesize = size != null && size > 0 ? size : 10;
        var sortingField = sorting != null && !sorting.isEmpty() ? sorting : "id";
        Pageable pageable = PageRequest.of(safepage-1, safesize, Sort.by(sortingField).descending());
        return Mono.fromCallable(() ->
                        customerRepository.findAllByState(state, pageable)
                )
                .subscribeOn(Schedulers.boundedElastic())
                .map(customerPage -> customerPage.map(this::convertToDTO))
                .onErrorMap(DataAccessException.class, ex -> {
                    log.error("Database error while retrieving customers page={}, size={}, state={}",
                            page, size, state, ex);
                    return new DataAccessException("Error accessing the database", ex) {};
                });
    }

    @Override
    public Mono<GetCustomerDTO> updateCustomer(CreateCustomerDTO customer, String identificationNumber) {
        return Mono.fromCallable(() -> this.customerRepository.findByIdentificationNumber(identificationNumber))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Customer not found for update identificationNumber={}", identificationNumber);
                    return Mono.error(new NotFoundException(identificationNumber));
                }))
                .map(existingCustomer -> {
                    updateEntity(existingCustomer, customer);
                    return existingCustomer;
                })
                .flatMap(c ->
                        Mono.fromCallable(() -> this.convertToDTO(this.customerRepository.save(c)))
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .onErrorMap(DataIntegrityViolationException.class, ex -> {
                    log.warn("Constraint violation while updating customer identificationNumber={}", customer.getPerson().getIdentificationNumber(), ex);
                    return new DataIntegrityViolationException("Data integrity violation for customer with identification number: " + customer.getPerson().getIdentificationNumber(), ex) {
                    };
                })

                .onErrorMap(DataAccessException.class, ex -> {
                    log.error("Database error while updating customer identificationNumber={}", customer.getPerson().getIdentificationNumber(), ex);
                    return new DataAccessException("Error accessing the database", ex) {};
                });
    }

    @Override
    public Mono<Void> deleteCustomer(String identificationNumber) {
        return Mono.fromCallable(() -> this.customerRepository.findByIdentificationNumber(identificationNumber))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(Mono::justOrEmpty)
                .switchIfEmpty(Mono.defer(() -> {
                    log.warn("Customer not found for delete identificationNumber={}", identificationNumber);
                    return Mono.error(new NotFoundException(identificationNumber));
                }))
                .flatMap(customer -> Mono.fromRunnable(() -> this.customerRepository.delete(customer))
                        .subscribeOn(Schedulers.boundedElastic())
                )
                .onErrorMap(DataAccessException.class, ex -> {
                    log.error("Database error while updating customer identificationNumber={}", identificationNumber, ex);
                    return new DataAccessException("Error accessing the database", ex) {};
                })
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
        PersonDTO personDTO = new PersonDTO();
        personDTO.setId(customerEntity.getPerson().getId());
        personDTO.setName(customerEntity.getPerson().getName());
        personDTO.setIdentificationNumber(customerEntity.getPerson().getIdentificationNumber());
        personDTO.setName(customerEntity.getPerson().getName());
        personDTO.setGender(customerEntity.getPerson().getGender());
        personDTO.setAddress(customerEntity.getPerson().getAddress());
        personDTO.setPhone(customerEntity.getPerson().getPhone());
        getCustomerDTO.setPerson(personDTO);
        return getCustomerDTO;
    }

    //NO update identification number, only update other fields
   private CustomerEntity updateEntity(CustomerEntity existingEntity, CreateCustomerDTO updateDTO) {
        existingEntity.setState(updateDTO.getState()!=null?updateDTO.getState():existingEntity.getState());
        existingEntity.setPassword(updateDTO.getPassword()!=null?updateDTO.getPassword():existingEntity.getPassword());
        existingEntity.setState(updateDTO.getState()!=null?updateDTO.getState():existingEntity.getState());
        existingEntity.getPerson().setName(updateDTO.getPerson().getName()!=null?updateDTO.getPerson().getName():existingEntity.getPerson().getName());
        existingEntity.getPerson().setGender(updateDTO.getPerson().getGender()!=null?updateDTO.getPerson().getGender():existingEntity.getPerson().getGender());
        existingEntity.getPerson().setAddress(updateDTO.getPerson().getAddress()!=null?updateDTO.getPerson().getAddress():existingEntity.getPerson().getAddress());
        existingEntity.getPerson().setPhone(updateDTO.getPerson().getPhone()!=null?updateDTO.getPerson().getPhone():existingEntity.getPerson().getPhone());
        return existingEntity;
    }


}
