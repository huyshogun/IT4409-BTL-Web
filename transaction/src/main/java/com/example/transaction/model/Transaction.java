package com.example.transaction.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@Entity
@Table(name = "transactions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String transactionReference;

    @Column(nullable=false)
    private String sourceAccountNumber;

    @Column(nullable=false)
    private String destinationAccountNumber;

    @Column(nullable=false)
    private BigDecimal amount;

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    @Column(nullable=false)
    @Enumerated(EnumType.STRING)
    private TransactionCurrency currency;

    @Column(nullable=false)
    private String description;

    @Column(nullable=false)
    private LocalDateTime createdAt;

    @Column(nullable=false)
    private LocalDateTime updatedAt;

    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER
    }

    public enum TransactionStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REVERSED
    }

    public enum TransactionCurrency {
        USD,
        EUR,
        GBP,
        JPY,
        AUD,
        CAD,
        CHF,
        CNY,
        SEK,
        NZD,
        VND
    }
    
}
