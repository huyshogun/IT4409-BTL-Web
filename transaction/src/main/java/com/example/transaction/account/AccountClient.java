package com.example.transaction.account;
import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
@FeignClient(name = "account-service", url = "${application.config.account-url}")
public interface AccountClient {
    @GetMapping("/{accountNumber}")
    Optional<AccountResponse> findAccountByAccountNumber(@PathVariable String accountNumber);

    @PutMapping("/{accountNumber}/balance")
    void updateAccountBalance(@PathVariable String accountNumber, BigDecimal newBalance);
}
