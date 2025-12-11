package com.example.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.transaction.model.Transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequestDto {
    private String sourceAccountNumber;
    private String destinationAccountNumber;
    private BigDecimal amount;
    private Transaction.TransactionType transactionType;
    private Transaction.TransactionCurrency currency;
    private String description;
    private LocalDateTime createdAt;
}