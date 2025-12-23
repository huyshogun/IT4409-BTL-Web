package com.example.notification.repository;

import com.example.notification.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    // Lấy danh sách thông báo của 1 tài khoản, sắp xếp mới nhất lên đầu
    List<Notification> findByAccountNumberOrderByTransactionDateDesc(String accountNumber);
}