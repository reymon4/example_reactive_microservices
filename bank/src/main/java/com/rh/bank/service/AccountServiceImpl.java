package com.rh.bank.service;


import com.rh.bank.repository.IAccountRepository;
import com.rh.bank.service.dto.CreateAccountDTO;
import com.rh.bank.service.dto.GetAccountDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@Slf4j
public class AccountServiceImpl implements IAccountService {

    private final IAccountRepository accountRepository;

    public AccountServiceImpl(IAccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }


    @Override
    public Mono<GetAccountDTO> createAccount(CreateAccountDTO createAccountDTO) {
        String identificationNumber = createAccountDTO.getPersonIdentificationNumber();

    }

    @Override
    public Mono<GetAccountDTO> getAccountByNumber(String accountNumber) {
        return null;
    }

    @Override
    public Mono<Page<GetAccountDTO>> getAllAccountsByCustomer(Boolean state, String personIdentificationNumber, Integer page, Integer size, String sorting) {
        return null;
    }

    @Override
    public Mono<GetAccountDTO> updateAccount(CreateAccountDTO createAccountDTO, String accountNumber) {
        return null;
    }

    @Override
    public Mono<Void> deleteAccount(String accountNumber) {
        return null;
    }
}
