package com.example.account.dto;
import java.math.BigDecimal;

import com.example.account.model.Account;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountRequestDto {
    @NotNull
    private Long userId;

    @NotNull
    private Account.AccountType accountType;

    @NotNull
    private String currency;

    @Positive
    private BigDecimal initialDeposit;
}
