package com.example.kafka.service;

import com.example.kafka.dto.AccountDto;
import com.example.kafka.entity.Account;
import com.example.kafka.entity.Operation;
import com.example.kafka.entity.Person;
import com.example.kafka.kafka.ConfirmProducer;
import com.example.kafka.kafka.JsonChangeBalanceProducer;
import com.example.kafka.mapper.Mapper;
import com.example.kafka.repository.AccountRepo;
import com.example.kafka.repository.OperationRepo;
import com.example.kafka.repository.PersonRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
@AllArgsConstructor
@Slf4j
public class JsonBalanceService {

    private final PersonRepo personRepo;
    private final AccountRepo accountRepo;
    private final OperationRepo operationRepo;
    private final Mapper mapper;
    private final JsonChangeBalanceProducer changeBalanceProducer;
    private final ConfirmProducer confirmProducer;
    private final DataSourceTransactionManagerAutoConfiguration dataSourceTransactionManagerAutoConfiguration;

    public Account saveNewAccount(AccountDto accountDto) {
        return accountRepo.save(mapper.toAccount(accountDto));
    }

    public Person saveNewPerson(Person person) {
        return personRepo.save(person);
    }

    public void sendPayments(LinkedList<Operation> operations) {
        changeBalanceProducer.send(operations);
    }

    public void sendConfirmation(Integer id) {
        confirmProducer.send(Long.valueOf(id));
    }
}
