package com.example.transaction.account;

import java.math.BigDecimal;

public record AccountResponse(
    Long id,
    String accountNumber,
    String userId,
    BigDecimal balance,
    String currency,
    Boolean isActive
) {
}
