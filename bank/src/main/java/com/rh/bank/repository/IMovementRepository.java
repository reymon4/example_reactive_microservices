package com.rh.bank.repository;

import com.rh.bank.repository.model.MovementEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface IMovementRepository extends JpaRepository<MovementEntity, Long> {

    @Query("SELECT m FROM MovementEntity m WHERE m.keyMovement = ?1")
    MovementEntity findByKeyMovement(@Param("keyMovement") String keyMovement);


    @Query("SELECT m FROM MovementEntity m WHERE m.account.number = :number AND m.date BETWEEN :initialDate AND :finalDate AND m.state = :state")
    Page<MovementEntity> findAllToReport(@Param("number") String accountNumber,
                                        @Param("initialDate")LocalDateTime initialDate,
                                        @Param("finalDate") LocalDateTime finalDate,
                                        @Param("state") Boolean state,
                                        Pageable pageable);

}
