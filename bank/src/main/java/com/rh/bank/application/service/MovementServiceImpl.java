package com.rh.bank.application.service;

import com.rh.bank.infrastructure.repository.IAccountRepository;
import com.rh.bank.application.service.dto.CreateMovementDTO;
import com.rh.bank.application.service.dto.GetMovementDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
@Slf4j
public class MovementServiceImpl implements IMovementService {

    private final IAccountRepository accountRepository;

    public MovementServiceImpl(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public Mono<GetMovementDTO> createMovement(CreateMovementDTO createMovementDTO) {
        return null;
    }

    @Override
    public Mono<GetMovementDTO> getMovementByKey(String keyMovement) {
        return null;
    }

    @Override
    Mono<Page<GetMovementDTO>> getMovementsByCustomerAndState(String accountNumber, LocalDateTime initialDate, LocalDateTime finalDate, Boolean state, Integer page, Integer size, String sorting) {
        return null;
    }

    @Override
    public Mono<GetMovementDTO> updateMovement(CreateMovementDTO movementDTO, String keyMovement) {
        return null;
    }

    @Override
    public Mono<Void> deleteMovement(String keyMovement) {
        return null;
    }
}
