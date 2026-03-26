package com.rh.customers.repository;

import com.rh.customers.repository.model.CustomerEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.List;


@Repository
public interface ICustomerRepository extends JpaRepository<CustomerEntity, Long> {


    @Query("SELECT c FROM CustomerEntity c WHERE c.person.identificationNumber = ?1")
    CustomerEntity findByIdentificationNumber(@Param("identificationNumber") String identificationNumber);


    @Query("SELECT c FROM CustomerEntity c WHERE c.state = ?1")
    List<CustomerEntity> findAllByState(@Param("state") Boolean state);

}
