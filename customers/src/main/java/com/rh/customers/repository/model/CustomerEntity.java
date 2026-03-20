package com.rh.customers.repository.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Table(name="customer")
public class CustomerEntity {

    @Id
    private Long id;
    private Boolean state;
    private String password;

    @Column("person_id")
    private Long personId;


}
