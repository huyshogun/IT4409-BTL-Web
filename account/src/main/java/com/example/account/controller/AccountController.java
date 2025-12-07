package com.example.account.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.dto.AccountRequest;
import com.example.account.service.AccountService;

import jakarta.validation.Valid;
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    // Define your endpoints here
    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<String> createAccount(@Valid @RequestBody AccountRequest accountRequest) {
        
        // Placeholder implementation
        return ResponseEntity.ok("Account created successfully");
    }


    





    
}
