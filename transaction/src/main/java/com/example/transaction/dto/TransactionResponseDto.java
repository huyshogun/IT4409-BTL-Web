package com.example.transaction.dto;

import java.math.BigDecimal;

import com.example.transaction.model.Transaction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDto {
    
    @NotNull
    private String sourceAccountNumber;

    @NotNull
    private String destinationAccountNumber;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotNull
    private Transaction.TransactionType transactionType;

    @NotNull
    private Transaction.TransactionCurrency transactionCurrency;

    private String description;
}
