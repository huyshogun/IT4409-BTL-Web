package com.example.account.respository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.account.model.Account;

@Repository
public interface AccountRespository extends JpaRepository<Account, String> {
    Optional<Account> findByUserId(UUID userId);
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByUserIdAndIsActiveTrue(UUID userId);

    boolean existsByAccountNumber(String accountNumber);
    boolean existsByUserId(UUID userId);

}
