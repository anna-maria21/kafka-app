package com.example.kafka.controller;

import com.example.kafka.dto.Account;
import com.example.kafka.dto.KafkaInput;
import com.example.kafka.dto.Person;
import com.example.kafka.kafka.JsonChangeBalanceProducer;
import com.example.kafka.repository.AccountRepo;
import com.example.kafka.repository.PersonRepo;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/kafka")
@AllArgsConstructor
public class JsonBalanceController {

    private final JsonChangeBalanceProducer changeBalanceProducer;
    private final AccountRepo accountRepo;
    private final PersonRepo personRepo;


    @PostMapping("/new/acc")
    public Account newAccount(@RequestBody Account account) {
        return accountRepo.save(account);
    }

    @PostMapping("/new/person")
    public Person newPerson(@RequestBody Person person) {
        return personRepo.save(person);
    }

    @PostMapping("/change-balance")
    public ResponseEntity<String> sendMessage(@RequestBody KafkaInput input) {
        changeBalanceProducer.send(input);
        return ResponseEntity.ok("JSON input sent to the topic");
    }

}
