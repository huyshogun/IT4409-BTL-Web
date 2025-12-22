package com.example.transaction.account;
import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
@FeignClient(name = "ACCOUNT-SERVICE", path = "/api/v1/account")
public interface AccountClient {
    @GetMapping("/internalApi/account/{accountNumber}")
    Optional<AccountResponse> findAccountByAccountNumber(@PathVariable String accountNumber);

    @GetMapping("/internalApi/user/{userId}")
    Optional<AccountResponse> findAccountByUserId(@PathVariable String userId);

    @PutMapping("/internalApi/account/{accountNumber}/balance")
    void updateAccountBalance(@PathVariable String accountNumber, BigDecimal newBalance);

}
