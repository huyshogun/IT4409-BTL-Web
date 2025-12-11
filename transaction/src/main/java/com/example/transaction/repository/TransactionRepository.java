package com.example.transaction.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.transaction.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    Optional<Transaction> findByTransactionReference(String transactionReference);
    List<Transaction> findBySourceAccountNumber(String sourceAccountNumber);
    List<Transaction> findByDestinationAccountNumber(String destinationAccountNumber);
    Page<Transaction> findBySourceAccountNumberOrDestinationAccountNumber(String sourceAccountNumber, String destinationAccountNumber, Pageable pageable);
    List<Transaction> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
