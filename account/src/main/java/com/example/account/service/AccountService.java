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
import com.example.account.respository.AccountRespository; 
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.fasterxml.jackson.databind.ObjectMapper; // Dùng để tạo JSON string
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    private final AccountRespository accountRepository;

    @Transactional
    public AccountResponseDto createAccount(AccountRequestDto accountRequest) {
        // 1. Kiểm tra User đã có tài khoản chưa
        if (accountRepository.existsByUserId(accountRequest.getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Account already exists for user ID: " + accountRequest.getUserId());
        }

        // 2. Sinh số tài khoản ngẫu nhiên (đảm bảo không trùng)
        String accountNumber;
        do {
            accountNumber = generateAccountNumber();
        } while (accountRepository.existsByAccountNumber(accountNumber));

        // 3. Chuẩn bị dữ liệu Account (Chưa lưu DB vội)
        Account account = Account.builder()
                .accountNumber(accountNumber)
                .userId(accountRequest.getUserId())
                .accountName(accountRequest.getAccountName())
                .accountType(accountRequest.getAccountType())
                .balance(accountRequest.getInitialDeposit() != null ? accountRequest.getInitialDeposit() : BigDecimal.ZERO)
                .currency(accountRequest.getCurrency()) // Đã fix lỗi thiếu field này
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        // 4. --- TẠO MÃ QR VÀ GÁN VÀO MODEL ---
        try {
            // Tạo nội dung cho mã QR (Dạng JSON để các máy khác dễ đọc)
            String qrContent = createQrPayload(account);
            
            // Sinh ảnh QR dưới dạng chuỗi Base64
            String qrBase64 = generateQrBase64(qrContent, 300, 300);
            
            // Gán vào entity
            account.setQrCode(qrBase64);
            
        } catch (Exception e) {
            // Nếu lỗi tạo QR thì chỉ log warning, không chặn việc tạo tài khoản
            log.warn("Failed to generate QR Code for account: {}", accountNumber, e);
            account.setQrCode(null); 
        }

        // 5. Lưu xuống DB
        Account savedAccount = accountRepository.save(account);
        log.info("Account created with ID: {}", savedAccount.getId());

        return mapToResponseDto(savedAccount);
    }

    // --- HÀM BỔ TRỢ 1: Tạo nội dung JSON cho QR ---
    private String createQrPayload(Account account) {
        try {
            // Tạo Map chứa thông tin cần thiết để chuyển khoản
            Map<String, String> qrData = new HashMap<>();
            qrData.put("type", "TRANSFER");
            qrData.put("accountName", account.getAccountName());
            qrData.put("accountNumber", account.getAccountNumber());
            qrData.put("bankCode", "HUY_BANK_CORE");
            
            // Chuyển Map thành chuỗi JSON: {"type":"TRANSFER", ...}
            return new ObjectMapper().writeValueAsString(qrData);
        } catch (Exception e) {
            return account.getAccountNumber(); // Fallback về số tài khoản thường nếu lỗi JSON
        }
    }

    // --- HÀM BỔ TRỢ 2: Sinh ảnh QR Base64 ---
    private String generateQrBase64(String content, int width, int height) throws Exception {
        QRCodeWriter barcodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = barcodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height);

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", bos);
        
        // Trả về chuỗi chuẩn để Frontend hiển thị được ngay trong thẻ <img>
        return "data:image/png;base64," + Base64.getEncoder().encodeToString(bos.toByteArray());
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                                    "Account not found with user ID: " + userId));
        return mapToResponseDto(account);
    }

    @Cacheable(value = "accounts", key = "#accountNumber")
    public AccountResponseDto getAccountByAccountNumber(String accountNumber) {
        log.info("Fetching account from Database for account number: {}", accountNumber); // Log để test xem có chọc vào DB không
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                                    "Account not found with account number: " + accountNumber));
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                                    "Account not found with user ID: " + userId));

        account.setAccountType(accountRequest.getAccountType());
        account.setUpdatedAt(LocalDateTime.now());
        account.setAccountName(accountRequest.getAccountName());
        
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                                    "Account not found with user ID: " + userId));
        accountRepository.delete(account);
        log.info("Account deleted with account number: {}", account.getAccountNumber());
    }

    @Transactional
    @CachePut(value = "accounts", key = "#accountNumber")
    public AccountResponseDto updateAccountBalance(String accountNumber, BigDecimal newBalance) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                                    "Account not found with account number: " + accountNumber));

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
                .accountName(account.getAccountName())
                .accountType(account.getAccountType())
                .balance(account.getBalance())
                .currency(account.getCurrency()) // Nhớ map thêm cái này
                .qrCode(account.getQrCode())
                .createdAt(account.getCreatedAt())
                .isActive(account.getIsActive())
                .build();
    }
}
