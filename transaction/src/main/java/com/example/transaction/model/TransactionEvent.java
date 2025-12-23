package com.example.transaction.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TransactionEvent {
    private String transactionReference;
    private String transactionAccountNumber;
    private String status;
    private String type;
    private String amount;
    private BigDecimal balance;
    private LocalDateTime createdAt;
    private String description;
}
