package com.rh.customers.service;

import com.rh.customers.application.exception.db.DuplicateResourceException;
import com.rh.customers.application.exception.domain.NotFoundException;
import com.rh.customers.application.service.CustomerServiceImpl;
import com.rh.customers.application.service.dto.CreateCustomerDTO;
import com.rh.customers.application.service.dto.CreatePersonDTO;
import com.rh.customers.application.service.dto.GetCustomerDTO;
import com.rh.customers.infrastructure.cache.CustomerCacheService;
import com.rh.customers.infrastructure.event.producer.CustomerEventProducer;
import com.rh.customers.infrastructure.repository.ICustomerRepository;
import com.rh.customers.infrastructure.repository.model.CustomerEntity;
import com.rh.customers.infrastructure.repository.model.PersonEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    @Mock
    private ICustomerRepository customerRepository;

    @Mock
    private CustomerCacheService customerCacheService;

    @Mock
    private CustomerEventProducer customerEventProducer;

    private CustomerServiceImpl customerService;

    @BeforeEach
    void setUp() {
        customerService = new CustomerServiceImpl(customerRepository, customerCacheService, customerEventProducer);
    }

    @Test
    void createCustomer_success() {
        CreateCustomerDTO request = buildCreateCustomerDTO("12345");
        CustomerEntity savedEntity = buildCustomerEntity("12345", "Alice");

        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(savedEntity);
        when(customerCacheService.save(any(GetCustomerDTO.class))).thenReturn(Mono.just(true));
        when(customerEventProducer.sendCustomerEvent(eq("customer-created"), any(), eq("12345"))).thenReturn(Mono.empty());

        StepVerifier.create(customerService.createCustomer(request))
                .assertNext(customer -> {
                    assertEquals("12345", customer.getIdentificationNumber());
                    assertEquals("Alice", customer.getCustomerName());
                    assertEquals(true, customer.getState());
                })
                .verifyComplete();

        verify(customerRepository).save(any(CustomerEntity.class));
        verify(customerCacheService).save(any(GetCustomerDTO.class));
        verify(customerEventProducer).sendCustomerEvent(eq("customer-created"), any(), eq("12345"));
    }

    @Test
    void createCustomer_failure_duplicateIdentification() {
        CreateCustomerDTO request = buildCreateCustomerDTO("12345");
        DataIntegrityViolationException duplicate = new DataIntegrityViolationException(
                "constraint",
                new RuntimeException("unique constraint violation")
        );

        when(customerRepository.save(any(CustomerEntity.class))).thenThrow(duplicate);

        StepVerifier.create(customerService.createCustomer(request))
                .expectError(DuplicateResourceException.class)
                .verify();

        verify(customerCacheService, never()).save(any(GetCustomerDTO.class));
        verify(customerEventProducer, never()).sendCustomerEvent(anyString(), any(), anyString());
    }

    @Test
    void getCustomerByIdentificationNumber_success_fromCache() {
        GetCustomerDTO cached = buildGetCustomerDTO("67890", "Bob");
        when(customerCacheService.get("67890")).thenReturn(Mono.just(cached));

        StepVerifier.create(customerService.getCustomerByIdentificationNumber("67890"))
                .assertNext(customer -> {
                    assertEquals("67890", customer.getIdentificationNumber());
                    assertEquals("Bob", customer.getCustomerName());
                })
                .verifyComplete();

        verify(customerCacheService).get("67890");
        verify(customerRepository, never()).findByIdentificationNumber(anyString());
    }

    @Test
    void getCustomerByIdentificationNumber_failure_notFound() {
        when(customerCacheService.get("99999")).thenReturn(Mono.empty());
        when(customerRepository.findByIdentificationNumber("99999")).thenReturn(null);

        StepVerifier.create(customerService.getCustomerByIdentificationNumber("99999"))
                .expectError(NotFoundException.class)
                .verify();
    }

    @Test
    void getAllCustomers_success() {
        CustomerEntity entity = buildCustomerEntity("11111", "Carol");
        Page<CustomerEntity> page = new PageImpl<>(List.of(entity));

        when(customerRepository.findAllByState(eq(true), any())).thenReturn(page);

        StepVerifier.create(customerService.getAllCustomers(true, 1, 10, "id"))
                .assertNext(resultPage -> {
                    assertEquals(1, resultPage.getTotalElements());
                    assertEquals("Carol", resultPage.getContent().getFirst().getCustomerName());
                })
                .verifyComplete();

        verify(customerRepository).findAllByState(eq(true), any());
    }

    @Test
    void getAllCustomers_failure_databaseError() {
        when(customerRepository.findAllByState(eq(true), any()))
                .thenThrow(new DataAccessResourceFailureException("db unavailable"));

        StepVerifier.create(customerService.getAllCustomers(true, 1, 10, "id"))
                .expectErrorMatches(ex -> ex instanceof org.springframework.dao.DataAccessException
                        && ex.getMessage().contains("Error accessing the database"))
                .verify();
    }

    @Test
    void updateCustomer_success() {
        CreateCustomerDTO request = buildCreateCustomerDTO("12345");
        request.getPerson().setName("Alice Updated");
        CustomerEntity existing = buildCustomerEntity("12345", "Alice");
        CustomerEntity updated = buildCustomerEntity("12345", "Alice Updated");

        when(customerRepository.findByIdentificationNumber("12345")).thenReturn(existing);
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(updated);
        when(customerCacheService.delete("12345")).thenReturn(Mono.just(true));

        StepVerifier.create(customerService.updateCustomer(request, "12345"))
                .assertNext(customer -> assertEquals("Alice Updated", customer.getCustomerName()))
                .verifyComplete();

        verify(customerRepository).findByIdentificationNumber("12345");
        verify(customerRepository).save(any(CustomerEntity.class));
        verify(customerCacheService).delete("12345");
    }

    @Test
    void updateCustomer_failure_notFound() {
        CreateCustomerDTO request = buildCreateCustomerDTO("404");
        when(customerRepository.findByIdentificationNumber("404")).thenReturn(null);

        StepVerifier.create(customerService.updateCustomer(request, "404"))
                .expectError(NotFoundException.class)
                .verify();

        verify(customerRepository, never()).save(any(CustomerEntity.class));
        verify(customerCacheService, never()).delete(anyString());
    }

    @Test
    void deleteCustomer_success() {
        CustomerEntity existing = buildCustomerEntity("77777", "Dave");

        when(customerRepository.findByIdentificationNumber("77777")).thenReturn(existing);
        when(customerCacheService.delete("77777")).thenReturn(Mono.just(true));
        when(customerEventProducer.sendCustomerEvent(eq("customer-deleted"), any(), eq("77777"))).thenReturn(Mono.empty());

        StepVerifier.create(customerService.deleteCustomer("77777"))
                .verifyComplete();

        verify(customerRepository).findByIdentificationNumber("77777");
        verify(customerRepository).delete(existing);
        verify(customerCacheService).delete("77777");
        verify(customerEventProducer).sendCustomerEvent(eq("customer-deleted"), any(), eq("77777"));
    }

    @Test
    void deleteCustomer_failure_notFound() {
        when(customerRepository.findByIdentificationNumber("00000")).thenReturn(null);
        // Service composes these Monos eagerly, so mocks must return a Mono even in error paths.
        when(customerCacheService.delete("00000")).thenReturn(Mono.just(false));
        when(customerEventProducer.sendCustomerEvent(eq("customer-deleted"), any(), eq("00000"))).thenReturn(Mono.empty());

        StepVerifier.create(customerService.deleteCustomer("00000"))
                .expectError(NotFoundException.class)
                .verify();

        verify(customerRepository).findByIdentificationNumber("00000");
        verify(customerRepository, never()).delete(any());

    }

    private static CreateCustomerDTO buildCreateCustomerDTO(String identificationNumber) {
        CreatePersonDTO person = new CreatePersonDTO();
        person.setId(10L);
        person.setName("Alice");
        person.setIdentificationNumber(identificationNumber);
        person.setGender("F");
        person.setAddress("Street 1");
        person.setPhone("0999999999");

        CreateCustomerDTO customer = new CreateCustomerDTO();
        customer.setId(1L);
        customer.setPassword("secret");
        customer.setState(true);
        customer.setPerson(person);
        return customer;
    }

    private static CustomerEntity buildCustomerEntity(String identificationNumber, String name) {
        PersonEntity person = new PersonEntity();
        person.setId(10L);
        person.setName(name);
        person.setIdentificationNumber(identificationNumber);
        person.setGender("F");
        person.setAddress("Street 1");
        person.setPhone("0999999999");

        CustomerEntity customer = new CustomerEntity();
        customer.setId(1L);
        customer.setPassword("secret");
        customer.setState(true);
        customer.setPerson(person);
        return customer;
    }

    private static GetCustomerDTO buildGetCustomerDTO(String identificationNumber, String customerName) {
        GetCustomerDTO dto = new GetCustomerDTO();
        dto.setId(1L);
        dto.setIdentificationNumber(identificationNumber);
        dto.setCustomerName(customerName);
        dto.setState(true);
        dto.setAddress("Street 1");
        dto.setPhone("0999999999");
        return dto;
    }
}
