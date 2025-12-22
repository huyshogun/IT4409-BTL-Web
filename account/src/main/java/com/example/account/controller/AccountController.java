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

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/account")
public class AccountController {
    // Define your endpoints here
    @Autowired
    private AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponseDto> createAccount(@Valid @RequestBody AccountRequestDto accountRequest) {
        // Placeholder implementation
        return new ResponseEntity<>(accountService.createAccount(accountRequest), org.springframework.http.HttpStatus.CREATED);
    }

    @GetMapping("/admin/getAll")
    public ResponseEntity<List<AccountResponseDto>> getAllAccounts() {
        // Placeholder implementation
        return ResponseEntity.ok(accountService.getAllAccounts());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AccountResponseDto> getAccountByUserId(@PathVariable UUID userId) {
        // Placeholder implementation
        return ResponseEntity.ok(accountService.getAccountByUserId(userId));
    }

    @PutMapping("/{userId}")
    public ResponseEntity<AccountResponseDto> updateAccount(@PathVariable UUID userId, @Valid @RequestBody AccountRequestDto accountRequest) {
        // Placeholder implementation
        return ResponseEntity.ok(accountService.updateAccount(userId, accountRequest));
    }
    /* 
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable UUID userId   ) {
        // Placeholder implementation
        accountService.deleteAccount(userId);
        return ResponseEntity.noContent().build();
    }
    */

    @PutMapping("/{userId}/balance")
    public ResponseEntity<AccountResponseDto> updateAccountBalance(@PathVariable UUID userId, @RequestBody BigDecimal newBalance) {
        // Placeholder implementation
        return ResponseEntity.ok(accountService.updateAccountBalance(userId, newBalance));
    }
}
