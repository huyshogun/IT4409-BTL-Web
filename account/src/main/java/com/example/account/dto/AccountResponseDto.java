package com.example.account.dto;

import java.math.BigDecimal;
import java.util.UUID;
import java.time.LocalDateTime;

import com.example.account.model.Account;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountResponseDto implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String accountNumber;
    private UUID userId;
    private Account.AccountType accountType;
    private BigDecimal balance;
    private String currency;
    private LocalDateTime createdAt;
    private Boolean isActive;
    
}
