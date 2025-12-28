package com.example.notification.service;

import com.example.notification.account.AccountClient;
import com.example.notification.dto.AccountResponseDto;
import com.example.notification.event.TransactionEvent;
import com.example.notification.model.Notification;
import com.example.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final AccountClient accountClient;

    // 1. Lắng nghe Kafka
    @KafkaListener(topics = "${application.topics.transaction-events}", 
                   containerFactory = "transactionKafkaListenerContainerFactory") // Dùng config factory bạn đã có
    public void consumeTransactionEvent(TransactionEvent event) {
        log.info("Received transaction event for account: {}", event.getTransactionAccountNumber());

        try {
            // 2. Map dữ liệu từ Event sang Entity
            Notification notification = Notification.builder()
                    .accountNumber(event.getTransactionAccountNumber())
                    .amount(event.getAmount())
                    // Xử lý balance: Nếu Kafka null thì default 0 để không lỗi DB
                    .balance(event.getBalance() != null ? event.getBalance() : BigDecimal.ZERO)
                    .description(event.getDescription())
                    .transactionReference(event.getTransactionReference())
                    .transactionDate(event.getCreatedAt())
                    .isRead(false) // Mặc định là chưa đọc
                    .build();

            // 3. Lưu vào Database
            Notification savedNotification = notificationRepository.save(notification);

            // 4. Đẩy ra WebSocket (Real-time)
            // Client sẽ subscribe: /queue/notifications/{accountNumber}
            String destination = "/queue/notifications/" + savedNotification.getAccountNumber();
            messagingTemplate.convertAndSend(destination, savedNotification);
            
            log.info("Notification sent to WebSocket destination: {}", destination);

        } catch (Exception e) {
            log.error("Error processing notification", e);
        }
    }

    // API: Lấy lịch sử thông báo
    public List<Notification> getNotificationsByUserId(UUID userId) {
            // Gọi hàm helper đã được Cache ở dưới
            String accountNumber = getAccountNumberFromCache(userId);

            if (accountNumber == null) {
                return List.of();
            }

            // Truy vấn DB Notification bằng AccountNumber đã cache
            return notificationRepository.findByAccountNumberOrderByTransactionDateDesc(accountNumber);
    }

    // --- HÀM HELPER CÓ CACHE ---
    
    // value: Tên phân vùng cache trong Redis
    // key: Khóa để tìm kiếm (ở đây là userId)
    // unless: Nếu kết quả null thì không cache
    @Cacheable(value = "userId_to_accountNumber", key = "#userId", unless = "#result == null")
    public String getAccountNumberFromCache(UUID userId) {
        log.info("Cache miss! Calling Account Service for userId: {}", userId);
        
        try {
            AccountResponseDto accountDto = accountClient.getAccountByUserId(userId);
            if (accountDto != null && accountDto.getAccountNumber() != null) {
                return accountDto.getAccountNumber();
            }
        } catch (Exception e) {
            log.error("Error calling Account Service: {}", e.getMessage());
        }
        
        return null; 
    }

    // API: Đánh dấu đã đọc
    public void markAsRead(Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found with ID: " + id));
        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notificationRepository.save(notification);
        }
    }
}