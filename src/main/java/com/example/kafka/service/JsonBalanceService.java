package com.example.kafka.service;

import com.example.kafka.dto.AccountDto;
import com.example.kafka.entity.Account;
import com.example.kafka.entity.Person;
import com.example.kafka.mapper.Mapper;
import com.example.kafka.repository.AccountRepo;
import com.example.kafka.repository.PersonRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class JsonBalanceService {

    private final PersonRepo personRepo;
    private final AccountRepo accountRepo;
    private final Mapper mapper;

    public Account saveNewAccount(AccountDto accountDto) {
        return accountRepo.save(mapper.toAccount(accountDto));
    }

    public Person saveNewPerson(Person person) {
        return personRepo.save(person);
    }
}
