package com.example.account.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.account.dto.AccountRequestDto;
import com.example.account.dto.AccountResponseDto;
import com.example.account.model.Account;
import com.example.account.respository.AccountRespository; // Lưu ý: Tên package bạn đang gõ sai là respository -> repository

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    private final AccountRespository accountRepository;

    @Transactional
    public AccountResponseDto createAccount(AccountRequestDto accountRequest) {
        // Logic kiểm tra User đã có tài khoản chưa (Thông báo lỗi đang hơi sai logic so với code)
        if (accountRepository.existsByUserId(accountRequest.getUserId())) {
            throw new IllegalArgumentException("User already has an account"); // Sửa lại message cho đúng ngữ cảnh
        } else {
            System.out.println("Creating account for userId: " + accountRequest.getUserId());
        }

        String accountNumber;
        do {
            accountNumber = generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        Account account = Account.builder()
                .accountNumber(accountNumber)
                .userId(accountRequest.getUserId())
                .accountType(accountRequest.getAccountType())
                .balance(accountRequest.getInitialDeposit() != null ? accountRequest.getInitialDeposit() : BigDecimal.ZERO)
                .currency(accountRequest.getCurrency()) // BỔ SUNG: Bạn quên map field này, sẽ bị lỗi DB vì nullable=false
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        Account savedAccount = accountRepository.save(account);
        log.info("Account created with ID: {}", savedAccount.getId());

        return mapToResponseDto(savedAccount);
    }

    // Không nên cache hàm getAll vì dữ liệu quá lớn và thay đổi liên tục
    public List<AccountResponseDto> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll();
        return accounts.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * @Cacheable:
     * - Khi gọi hàm: Kiểm tra Redis xem có key 'accounts::123456' chưa.
     * - Nếu CÓ: Trả về luôn (Không chạy code trong hàm, không query DB).
     * - Nếu KHÔNG: Chạy hàm, lấy từ DB, lưu vào Redis rồi trả về.
     */
    @Cacheable(value = "accounts", key = "#userId")
    public AccountResponseDto getAccountByUserId(UUID userId) {
        log.info("Fetching account from Database for: {}", userId); // Log để test xem có chọc vào DB không
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with user ID: " + userId));
        return mapToResponseDto(account);
    }

    @Cacheable(value = "accounts", key = "#accountNumber")
    public AccountResponseDto getAccountByAccountNumber(String accountNumber) {
        log.info("Fetching account from Database for account number: {}", accountNumber); // Log để test xem có chọc vào DB không
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with account number: " + accountNumber));
        return mapToResponseDto(account);
    }

    /**
     * @CachePut:
     * - Luôn chạy code trong hàm để update xuống DB.
     * - Sau khi xong, lấy kết quả trả về (AccountResponseDto mới) cập nhật đè vào Redis.
     * - Giúp dữ liệu trong Cache luôn tươi mới giống DB.
     */
    @Transactional
    @CachePut(value = "accounts", key = "#userId")
    public AccountResponseDto updateAccount(UUID userId, AccountRequestDto accountRequest) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with user ID: " + userId));

        account.setAccountType(accountRequest.getAccountType());
        account.setUpdatedAt(LocalDateTime.now());
        
        // Lưu ý: Logic update balance nên cẩn thận, thường không update trực tiếp ở đây
        
        Account updatedAccount = accountRepository.save(account);
        log.info("Account updated with ID: {}", updatedAccount.getId());

        return mapToResponseDto(updatedAccount);
    }

    /**
     * @CacheEvict:
     * - Khi xóa tài khoản trong DB, phải xóa luôn trong Cache.
     * - Nếu không xóa, DB mất rồi mà Cache vẫn còn -> Người dùng vẫn xem được tài khoản ma.
     */
    @Transactional
    @CacheEvict(value = "accounts", key = "#userId")
    public void deleteAccount(UUID userId) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with user ID: " + userId));
        accountRepository.delete(account);
        log.info("Account deleted with account number: {}", account.getAccountNumber());
    }

    @Transactional
    @CachePut(value = "accounts", key = "#accountNumber")
    public AccountResponseDto updateAccountBalance(String accountNumber, BigDecimal newBalance) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new IllegalArgumentException("Account not found with account number: " + accountNumber));

        account.setBalance(newBalance);
        account.setUpdatedAt(LocalDateTime.now());

        Account updatedAccount = accountRepository.save(account);
        log.info("Account balance updated for account number: {}", updatedAccount.getAccountNumber());

        return mapToResponseDto(updatedAccount);
    }

    // ... Giữ nguyên các hàm private bên dưới
    private String generateAccountNumber() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 20).toUpperCase();
    }

    private AccountResponseDto mapToResponseDto(Account account) {
        return AccountResponseDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .userId(account.getUserId())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency()) // Nhớ map thêm cái này
                .createdAt(account.getCreatedAt())
                .isActive(account.getIsActive())
                .build();
    }
}
