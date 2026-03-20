package com.rh.customers.service;

import com.rh.customers.repository.ICustomerRepository;
import com.rh.customers.repository.IPersonRepository;
import com.rh.customers.repository.model.CustomerEntity;
import com.rh.customers.service.dto.CustomerDTO;
import com.rh.customers.service.dto.PersonDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CustomerServiceImpl implements ICustomerService{

    private final ICustomerRepository customerRepository;
    private final IPersonRepository personRepository;

    public CustomerServiceImpl(ICustomerRepository customerRepository, IPersonRepository personRepository) {
        this.customerRepository = customerRepository;
        this.personRepository = personRepository;
    }

    @Override
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        return null;
    }

    @Override
    public CustomerDTO getCustomerByIdentificationNumber(String customerIdentificationNumber) {
        return null;
    }

    @Override
    public List<CustomerDTO> getAllCustomers(Boolean state) {
        return List.of();
    }

    @Override
    public void updateCustomer(CustomerDTO customer) {

    }

    @Override
    public void deleteCustomer(String identificationNumber) {

    }

    private CustomerDTO mapToDTO(CustomerEntity customer) {
        PersonDTO personDTO = new PersonDTO();
        personDTO.setName(customer.person().name());
        personDTO.setIdentificationNumber(customer.person().identificationNumber());
        personDTO.setGender(customer.person().gender());
        personDTO.setAddress(customer.person().address());
        personDTO.setPhone(customer.person().phone());
        personDTO.setId(customer.person().id());
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setId(customer.id());
        customerDTO.setState(customer.state());
        customerDTO.setState(customer.state());
        customerDTO.setPerson(personDTO);
        return customerDTO;
    }
}
