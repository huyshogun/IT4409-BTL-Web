package com.example.account.respository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.account.model.Account;

@Repository
public interface AccountRespository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserId(Long userId);
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByUserIdAndIsActiveTrue(Long userId);

    boolean existsByAccountNumber(String accountNumber);
    boolean existsByUserId(Long userId);

}
