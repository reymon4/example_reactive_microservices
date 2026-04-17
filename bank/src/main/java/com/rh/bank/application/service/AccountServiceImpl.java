package com.rh.bank.application.service;


import com.rh.bank.application.clients.CustomersRestClient;
import com.rh.bank.application.exception.db.DuplicateResourceException;
import com.rh.bank.application.exception.db.IllegalArgumentsException;
import com.rh.bank.application.exception.domain.NotFoundException;
import com.rh.bank.infrastructure.cache.AccountCacheService;
import com.rh.bank.infrastructure.event.producer.AccountEvent;
import com.rh.bank.infrastructure.event.producer.AccountEventProducer;
import com.rh.bank.infrastructure.repository.IAccountRepository;
import com.rh.bank.application.service.dto.CreateAccountDTO;
import com.rh.bank.application.service.dto.GetAccountDTO;
import com.rh.bank.infrastructure.repository.model.AccountEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AccountServiceImpl implements IAccountService {

    private final IAccountRepository accountRepository;
    private final AccountCacheService accountCacheService;
    private final AccountEventProducer accountEventProducer;
    private final CustomersRestClient customersRestClient;

    public AccountServiceImpl(IAccountRepository accountRepository, AccountCacheService accountCacheService, AccountEventProducer accountEventProducer, CustomersRestClient customersRestClient) {
        this.accountRepository = accountRepository;
        this.accountCacheService = accountCacheService;
        this.accountEventProducer = accountEventProducer;
        this.customersRestClient = customersRestClient;
    }


    @Override
    public Mono<GetAccountDTO> createAccount(CreateAccountDTO createAccountDTO) {

        String identificationNumber = createAccountDTO.getPersonIdentificationNumber();

        return customersRestClient.getCustomerByIdentificationNumber(identificationNumber).map(response -> response.getBody()).flatMap(body -> {
            if (body == null || !Boolean.TRUE.equals(body.success())) {
                return Mono.error(new IllegalStateException("Customer not found or inactive: " + identificationNumber));
            }

            AccountEntity accountEntity = convertToEntity(createAccountDTO);

            return Mono.fromCallable(() -> accountRepository.save(accountEntity)).subscribeOn(Schedulers.boundedElastic()).map(saved -> convertToDTO(saved, body.data().getCustomerName())).flatMap(accountDto -> accountCacheService.save(accountDto).then(accountEventProducer.sendAccountEvent(new AccountEvent(accountDto.getNumber(), "ACCOUNT_CREATED", LocalDateTime.now(), identificationNumber), accountDto.getNumber())).thenReturn(accountDto));
        }).onErrorMap(DataIntegrityViolationException.class, ex -> {

            String message = ex.getMostSpecificCause() != null ? ex.getMostSpecificCause().getMessage() : ex.getMessage();

            if (message != null && message.contains("unique")) {
                log.warn("Duplicate customer identificationNumber={}", identificationNumber);
                return new DuplicateResourceException("Accounts", identificationNumber);
            }

            if (message != null && message.contains("null value")) {
                log.error("Null constraint violation identificationNumber={}", identificationNumber, ex);
                return new IllegalArgumentsException(identificationNumber, message);
            }

            log.error("Data integrity violation identificationNumber={}", identificationNumber, ex);
            return ex;
        });
    }


    @Override
    public Mono<GetAccountDTO> getAccountByNumber(String accountNumber) {
        return accountCacheService.get(accountNumber)
                .switchIfEmpty(Mono.fromCallable(() -> accountRepository.findByAccountNumber(accountNumber))
                        .subscribeOn(Schedulers.boundedElastic())
                        .switchIfEmpty(Mono.defer(() -> {
            log.info("Account not found with number={}", accountNumber);
            return Mono.error(new NotFoundException(accountNumber));
        }))
                        .flatMap(accountEntity -> customersRestClient
                                .getCustomerByIdentificationNumber(accountEntity.getPersonIdentificationNumber())
                                .map(response -> response.getBody().data().getCustomerName()).map(body -> convertToDTO(accountEntity, body)))
                        .flatMap(accountDto ->
                                accountCacheService.save(accountDto)
                                        .thenReturn(accountDto)));
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

    private AccountEntity convertToEntity(CreateAccountDTO createAccountDTO) {
        AccountEntity accountEntity = new AccountEntity();
        accountEntity.setState(createAccountDTO.getState());
        accountEntity.setPersonIdentificationNumber(createAccountDTO.getPersonIdentificationNumber());
        accountEntity.setType(createAccountDTO.getType());
        accountEntity.setNumber(createAccountDTO.getNumber());
        accountEntity.setBalance(createAccountDTO.getInitialBalance());
        return accountEntity;
    }

    private GetAccountDTO convertToDTO(AccountEntity accountEntity, String customerName) {
        GetAccountDTO getAccountDTO = new GetAccountDTO();
        getAccountDTO.setState(accountEntity.getState());
        getAccountDTO.setType(accountEntity.getType());
        getAccountDTO.setNumber(accountEntity.getNumber());
        getAccountDTO.setBalance(accountEntity.getBalance());
        getAccountDTO.setCustomerName(customerName);
        return getAccountDTO;
    }

    //Just update state and type
    private AccountEntity updateEntity(AccountEntity accountEntity, CreateAccountDTO createAccountDTO) {
        accountEntity.setState(createAccountDTO.getState() != null ? createAccountDTO.getState() : accountEntity.getState());
        accountEntity.setType(createAccountDTO.getType() != null ? createAccountDTO.getType() : accountEntity.getType());
        return accountEntity;


    }
}
