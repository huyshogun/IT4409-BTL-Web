package com.example.transaction.controller;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.transaction.dto.TransactionRequestDto;
import com.example.transaction.dto.TransactionResponseDto;
import com.example.transaction.service.TransactionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/api/v1/transaction")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @PostMapping("/{userId}")
    public ResponseEntity<TransactionResponseDto> createTransaction(@PathVariable UUID userId, @Valid @RequestBody TransactionRequestDto requestDto) {
        // Implement the logic to create a transaction
        return new ResponseEntity<>(transactionService.createTransaction(userId, requestDto), HttpStatus.CREATED);
    }

    @GetMapping("/reference/{reference-id}")
    public ResponseEntity<TransactionResponseDto> getTransactionByReferenceId(@PathVariable("reference-id") String referenceId) {
        TransactionResponseDto responseDto = transactionService.getTransactionByReferenceId(referenceId);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{userId}/reference/source-account/{accountNumber}")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsBySourceAccountNumber(
            @PathVariable("userId") UUID userId, @PathVariable("accountNumber") String accountNumber) {
        List<TransactionResponseDto> responseDtos = transactionService.getTransactionsBySourceAccountNumber(accountNumber);
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("/{userId}/reference/destination-account/{accountNumber}")
    public ResponseEntity<List<TransactionResponseDto>> getTransactionsByDestinationAccountNumber(
            @PathVariable("userId") UUID userId, @PathVariable("accountNumber") String accountNumber) {
        List<TransactionResponseDto> responseDtos = transactionService.getTransactionsByDestinationAccountNumber(accountNumber);
        return ResponseEntity.ok(responseDtos);
    }

    @GetMapping("reference/{reference-id}/status")
    public ResponseEntity<String> getTransactionStatus(@PathVariable("reference-id") String referenceId) {
        String status = transactionService.getTransactionStatus(referenceId);
        return ResponseEntity.ok(status);
    }

    @GetMapping("/{userId}/reference/{accountNumber}")
    public ResponseEntity<Page<TransactionResponseDto>> getTransactionsByAccountId(
            @PathVariable("userId") UUID userId,
            @PathVariable("accountNumber") String accountNumber,
            Pageable pageable) {
        Page<TransactionResponseDto> responseDtos = transactionService.getTransactionsBySourceAccountNumberOrDestinationAccountNumber(accountNumber, pageable);
        return ResponseEntity.ok(responseDtos);
    }


}
