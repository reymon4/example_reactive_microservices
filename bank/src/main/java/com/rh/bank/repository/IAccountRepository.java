package com.rh.bank.repository;

import com.rh.bank.repository.model.AccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IAccountRepository extends JpaRepository<AccountEntity, Long> {

    @Query("SELECT a FROM AccountEntity a WHERE a.number = ?1")
    AccountEntity findByAccountNumber(@Param("accountNumber") String accountNumber);


    @Query("SELECT a FROM AccountEntity a WHERE a.state = ?1 AND a.personIdentificationNumber=?2")
    Page<AccountEntity> findAllByState(@Param("state") Boolean state, Pageable pageable, @Param("personIdentificationNumber") String personIdentificationNumber);

}
