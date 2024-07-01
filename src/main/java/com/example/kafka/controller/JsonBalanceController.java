package com.example.kafka.controller;

import com.example.kafka.entity.Account;
import com.example.kafka.entity.Operation;
import com.example.kafka.entity.Person;
import com.example.kafka.service.JsonBalanceService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedList;

@RestController
@RequestMapping("api/kafka/")
@AllArgsConstructor
public class JsonBalanceController {

    private final JsonBalanceService service;
    private final KafkaTemplate<Long, Operation> kafkaTemplate;


    @PostMapping("retry")
    public ResponseEntity<String> retry(@RequestBody Operation operation) {
        kafkaTemplate.send("dlg-failed", operation.getAccountId(), operation);
        return ResponseEntity.ok("JSON sent to the retry topic");
    }


    @PostMapping("new/acc")
    public Account newAccount(@RequestBody Account account) {
        return service.saveNewAccount(account);
    }

    @PostMapping("new/person")
    public Person newPerson(@RequestBody Person person) {
        return service.saveNewPerson(person);
    }

    @PostMapping("change-balance")
    public ResponseEntity<String> sendMessage(@RequestBody LinkedList<Operation> input) {
        service.sendPayments(input);
        return ResponseEntity.ok("JSON input sent to the topic");
    }

    @PostMapping("confirm")
    public ResponseEntity<String> confirmPayment(@RequestBody LinkedList<Integer> ids) {
        service.sendConfirmation(ids);
        return ResponseEntity.ok("Confirmation sent to the topic");
    }

}
