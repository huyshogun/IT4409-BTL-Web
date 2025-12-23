package com.example.transaction.service;

import org.springframework.stereotype.Service;
import com.example.transaction.model.Transaction;
import com.example.transaction.model.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor // <--- THÊM DÒNG NÀY LÀ HẾT LỖI
public class TransactionEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    public void publishTransactionEvent(Transaction transaction, BigDecimal newSourceBalance, BigDecimal newDestinationBalance) {
        String sourceAccountNumber = transaction.getSourceAccountNumber();
        String destinationAccountNumber = Optional.ofNullable(transaction)
                .map(Transaction::getDestinationAccountNumber)
                .orElse(null);

        TransactionEvent event_source = TransactionEvent.builder()
                .transactionReference(transaction.getTransactionReference())
                .transactionAccountNumber(sourceAccountNumber)
                .status(transaction.getTransactionStatus().toString())
                .type(transaction.getTransactionType().toString())
                .amount("-" + transaction.getAmount().toString())
                .balance(newSourceBalance)
                .createdAt(transaction.getCreatedAt())
                .description(transaction.getDescription())
                .build();
        kafkaTemplate.send("transaction-topic", event_source);

        if (destinationAccountNumber == null) {
            return; // Nếu không có tài khoản đích, chỉ gửi sự kiện tài khoản nguồn
        }
        TransactionEvent event_destination = TransactionEvent.builder()
                .transactionReference(transaction.getTransactionReference())
                .transactionAccountNumber(destinationAccountNumber)
                .status(transaction.getTransactionStatus().toString())
                .type(transaction.getTransactionType().toString())
                .amount("+" + transaction.getAmount().toString())
                .balance(newDestinationBalance)
                .createdAt(transaction.getCreatedAt())
                .description(transaction.getDescription())
                .build();
        
        kafkaTemplate.send("transaction-topic", event_destination);
      
    }
}