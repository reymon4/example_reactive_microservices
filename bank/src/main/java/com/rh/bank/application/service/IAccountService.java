package com.rh.bank.application.service;

import com.rh.bank.application.service.dto.CreateAccountDTO;
import com.rh.bank.application.service.dto.GetAccountDTO;
import org.springframework.data.domain.Page;
import reactor.core.publisher.Mono;

public interface IAccountService {

    Mono<GetAccountDTO> createAccount(CreateAccountDTO createAccountDTO);

    Mono<GetAccountDTO> getAccountByNumber(String accountNumber);

    Mono<Page<GetAccountDTO>> getAllAccountsByCustomer(Boolean state,
                                                       String personIdentificationNumber,
                                                       Integer page, Integer size, String sorting);

    Mono<GetAccountDTO> updateAccount(CreateAccountDTO createAccountDTO, String accountNumber);

    Mono<Void> deleteAccount(String accountNumber);
}
