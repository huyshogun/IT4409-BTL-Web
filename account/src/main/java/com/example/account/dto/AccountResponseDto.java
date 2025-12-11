package com.example.account.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.example.account.model.Account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponseDto {
    private Long id;
    private String accountNumber;
    private Long userId;
    private Account.AccountType accountType;
    private BigDecimal balance;
    private String currency;
    private LocalDateTime createdAt;
    private Boolean isActive;
    
}
