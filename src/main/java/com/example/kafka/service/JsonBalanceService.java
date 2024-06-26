package com.example.kafka.service;

import com.example.kafka.entity.Account;
import com.example.kafka.entity.Operation;
import com.example.kafka.entity.Person;
import com.example.kafka.exception.AlreadyConfirmedOperationException;
import com.example.kafka.exception.NoSuchAccountException;
import com.example.kafka.exception.NoSuchOperationException;
import com.example.kafka.exception.NoSuchPersonException;
import com.example.kafka.kafka.ConfirmProducer;
import com.example.kafka.kafka.JsonChangeBalanceProducer;
import com.example.kafka.repository.jpa.AccountRepo;
import com.example.kafka.repository.jpa.OperationRepo;
import com.example.kafka.repository.jpa.PersonRepo;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedList;

@Service
@AllArgsConstructor
@Slf4j
public class JsonBalanceService {

    private final PersonRepo personRepo;
    private final AccountRepo accountRepo;
    private final OperationRepo operationRepo;
    private final JsonChangeBalanceProducer changeBalanceProducer;
    private final ConfirmProducer confirmProducer;


    public Account saveNewAccount(Account account) {
        personRepo.findById(account.getPersonId()).orElseThrow(() -> new NoSuchPersonException(account.getPersonId()));
        return accountRepo.save(account);
    }

    public Person saveNewPerson(Person person) {
        return personRepo.save(person);
    }

    public void sendPayments(LinkedList<Operation> operations) {
        operations.forEach(o -> accountRepo.findById(o.getAccountId()).orElseThrow(() -> new NoSuchAccountException(o.getAccountId())));
        changeBalanceProducer.send(operations);
    }

    public void sendConfirmation(LinkedList<Integer> ids) {
        long amountAlreadyConfirmedOperations = ids.stream()
                .filter(id -> operationRepo.findById(Long.valueOf(id)).orElseThrow(() -> new NoSuchOperationException(Long.valueOf(id))).getIsConfirmed())
                .count();
        if (amountAlreadyConfirmedOperations > 0) {
            throw new AlreadyConfirmedOperationException();
        }
        confirmProducer.send(ids);
    }
}
