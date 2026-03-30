package com.rh.bank.service;

import com.rh.bank.service.dto.CreateAccountDTO;
import com.rh.bank.service.dto.CreateMovementDTO;
import com.rh.bank.service.dto.GetAccountDTO;
import com.rh.bank.service.dto.GetMovementDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.repository.query.Param;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

public interface IMovementService {

    Mono<GetMovementDTO> createMovement(CreateMovementDTO createMovementDTO);

    Mono<GetMovementDTO> getMovementByKey(String keyMovement);

    /// Report by Customer (Account statement)
    Mono<Page<GetMovementDTO>> getMovementsByCustomerAndState(String accountNumber,
                                                      LocalDateTime initialDate, LocalDateTime finalDate, Boolean state,
                                                      Integer page, Integer size, String sorting);

    Mono<GetMovementDTO> updateMovement(CreateMovementDTO movementDTO, String keyMovement);

    Mono<Void> deleteMovement(String keyMovement);
}
