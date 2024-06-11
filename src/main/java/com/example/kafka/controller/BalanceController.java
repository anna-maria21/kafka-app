package com.example.kafka.controller;


//import com.example.kafka.kafka.ChangeBalanceProducer;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//
//@Controller
//@RequestMapping("api/kafka")
//public class BalanceController {
//    private final ChangeBalanceProducer changeBalanceProducer;
//
//    public BalanceController(ChangeBalanceProducer changeBalanceProducer) {
//        this.changeBalanceProducer = changeBalanceProducer;
//    }
//
//    @GetMapping("/change-balance")
//    public ResponseEntity<String> sendMessage(@RequestParam("message") String message) {
//        changeBalanceProducer.sendMessage(message);
//        return ResponseEntity.ok("Message sent successfully");
//    }
//}
