package com.rh.customers.repository;

import com.rh.customers.repository.model.PersonEntity;
import org.springframework.data.repository.CrudRepository;

public interface IPersonRepository extends CrudRepository<PersonEntity, Long> {
}
