package com.example.notification.account;

import com.example.notification.dto.AccountResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import java.util.UUID;

// name: Tên service trên Eureka
// path: Prefix chung của Account Controller
@FeignClient(name = "ACCOUNT-SERVICE", path = "/api/v1/account")
public interface AccountClient {

    // Gọi API lấy thông tin Account theo UserId (Internal API)
    @GetMapping("/internalApi/user/{userId}")
    AccountResponseDto getAccountByUserId(@PathVariable("userId") UUID userId);
}