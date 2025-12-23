package com.example.notification.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEvent {
    private String transactionReference;
    private String transactionAccountNumber; // Map với AccountNumber trong Entity
    private String status;
    private String type;
    private String amount; // Input là String
    private LocalDateTime createdAt;
    private String description;
    
    // Bổ sung để khớp với Entity (Nếu Kafka không có, ta sẽ default là 0)
    private BigDecimal balance; 
}