package com.example.kafka.controller;


import com.example.kafka.kafka.ChangeBalanceProducer;
import org.apache.coyote.Response;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/kafka")
public class BalanceController {
    private ChangeBalanceProducer changeBalanceProducer;

    public BalanceController(ChangeBalanceProducer changeBalanceProducer) {
        this.changeBalanceProducer = changeBalanceProducer;
    }

    @GetMapping("/change-balance")
    public ResponseEntity<String> sendMessage(@RequestParam("message") String message) {
        changeBalanceProducer.sendMessage(message);
        return ResponseEntity.ok("Message sent successfully");
    }
}
