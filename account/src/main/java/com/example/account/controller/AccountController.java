package com.example.account.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.account.dto.AccountRequestDto;
import com.example.account.dto.AccountResponseDto;
import com.example.account.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {
    // Define your endpoints here
    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponseDto> createAccount(@Valid @RequestBody AccountRequestDto accountRequest) {
        // Placeholder implementation
        return new ResponseEntity<>(accountService.createAccount(accountRequest), org.springframework.http.HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<AccountResponseDto>> getAllAccounts() {
        // Placeholder implementation
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponseDto> getAccountByAccountNumber(@PathVariable String accountNumber) {
        // Placeholder implementation
        return ResponseEntity.ok(accountService.getAccountByAccountNumber(accountNumber));
    }

    @PutMapping("/{accountNumber}")
    public ResponseEntity<AccountResponseDto> updateAccount(@PathVariable String accountNumber, @Valid @RequestBody AccountRequestDto accountRequest) {
        // Placeholder implementation
        return ResponseEntity.ok(accountService.updateAccount(accountNumber, accountRequest));
    }
    @DeleteMapping("/{accountNumber}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String accountNumber) {
        // Placeholder implementation
        accountService.deleteAccount(accountNumber);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{accountNumber}/balance")
    public ResponseEntity<AccountResponseDto> updateAccountBalance(@PathVariable String accountNumber, @RequestBody BigDecimal newBalance) {
        // Placeholder implementation
        return ResponseEntity.ok(accountService.updateAccountBalance(accountNumber, newBalance));
    }
}
