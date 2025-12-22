package com.example.transaction.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.transaction.account.AccountClient;
import com.example.transaction.account.AccountResponse;
import com.example.transaction.dto.TransactionRequestDto;
import com.example.transaction.dto.TransactionResponseDto;
import com.example.transaction.model.Transaction;
import com.example.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Objects; // Nhớ import cái này
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    // Sửa tên biến cho chuẩn: respository -> repository
    private final TransactionRepository transactionRepository;
    private final AccountClient accountClient;

    /**
     * TẠO GIAO DỊCH (GHI)
     * Khi có giao dịch mới -> Cần xóa Cache lịch sử của người dùng đó để họ thấy dữ liệu mới nhất.
     */
    @Transactional
    // Xóa cache của người gửi và người nhận (nếu có) để lần sau load lại list mới
    @Caching(evict = {
        @CacheEvict(value = "transaction-history", allEntries = true), // Cách đơn giản: Xóa toàn bộ cache history (hoặc dùng key cụ thể nếu custom CacheManager)
        @CacheEvict(value = "transactions", allEntries = true) 
    }) 
    public TransactionResponseDto createTransaction(UUID userId, TransactionRequestDto requestDto) {
        
        // Logic tìm tài khoản nguồn
        var sourceAccount = accountClient.findAccountByAccountNumber(requestDto.getSourceAccountNumber())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản nguồn"));

        var checkAccountUserId = accountClient.findAccountByUserId(userId.toString())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản của userId"));
        
        if (!Objects.equals(sourceAccount.userId(), checkAccountUserId.userId())) {
            throw new RuntimeException("Tài khoản nguồn không thuộc về userId này");
        }

        Transaction.TransactionType transactionType = requestDto.getTransactionType();
        TransactionResponseDto responseDto;

        switch (transactionType) {
            case DEPOSIT:
                responseDto = handleDeposit(requestDto, sourceAccount);
                break;
            case WITHDRAWAL:
                responseDto = handleWithdrawal(requestDto, sourceAccount);
                break;
            case TRANSFER:
                var destinationAccount = accountClient.findAccountByAccountNumber(requestDto.getDestinationAccountNumber())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản đích"));
                responseDto = handleTransfer(requestDto, sourceAccount, destinationAccount);
                break;
            default:
                throw new IllegalArgumentException("Invalid transaction type");
        }

        return responseDto;
    }

    /**
     * LẤY CHI TIẾT GIAO DỊCH (ĐỌC)
     * Cache lại kết quả theo referenceId.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "transaction-detail", key = "#referenceId") 
    public TransactionResponseDto getTransactionByReferenceId(String referenceId) {
        log.info("Fetching transaction from DB: {}", referenceId); // Log để kiểm tra có hit cache không
        Transaction transaction = transactionRepository.findByTransactionReference(referenceId)
                .orElseThrow(() -> new RuntimeException("Transaction not found with reference ID: " + referenceId));
        return mapToResponseDto(transaction);
    }

    // Các hàm get List này ít dùng phân trang, có thể cache nhẹ hoặc không
    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getTransactionsBySourceAccountNumber(String sourceAccountNumber) {
        List<Transaction> transactionsList = transactionRepository.findBySourceAccountNumber(sourceAccountNumber);
        return transactionsList.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TransactionResponseDto> getTransactionsByDestinationAccountNumber(String sourceAccountNumber) {
        List<Transaction> transactionsList = transactionRepository.findByDestinationAccountNumber(sourceAccountNumber);
        return transactionsList.stream().map(this::mapToResponseDto).collect(Collectors.toList());
    }

   @Transactional(readOnly = true)
    public String getTransactionStatus(String referenceId) {
        Transaction transaction = transactionRepository.findByTransactionReference(referenceId)
       .orElseThrow(() -> new RuntimeException("Transaction not found with reference ID: " + referenceId));
        return transaction.getTransactionStatus().name();

    }




    /**
     * LẤY LỊCH SỬ GIAO DỊCH CÓ PHÂN TRANG (QUAN TRỌNG)
     * Đây là hàm nặng nhất, cần Cache.
     * Key cache sẽ bao gồm: AccountID + Số trang + Kích thước trang
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "transaction-history", key = "#accountId + '-' + #pageable.pageNumber + '-' + #pageable.pageSize")
    public Page<TransactionResponseDto> getTransactionsBySourceAccountNumberOrDestinationAccountNumber(String accountId, Pageable pageable) {
        log.info("Fetching history from DB for account: {}", accountId);
        Page<Transaction> transactionsPage = transactionRepository.findBySourceAccountNumberOrDestinationAccountNumber(accountId, accountId, pageable);
        return transactionsPage.map(this::mapToResponseDto);
    }

    // ... (Giữ nguyên các hàm khác)

    // --- CÁC HÀM PRIVATE XỬ LÝ LOGIC ---
    // LƯU Ý: Đã xóa @Transactional ở đây vì hàm private không nhận @Transactional.
    // Transaction sẽ được quản lý bởi hàm createTransaction ở trên.

    private TransactionResponseDto handleDeposit(TransactionRequestDto requestDto, AccountResponse sourceAccount) {
        String transactionReference = generateUniqueReference();

        BigDecimal newBalance = sourceAccount.balance().add(requestDto.getAmount());
        accountClient.updateAccountBalance(requestDto.getSourceAccountNumber(), newBalance);

        Transaction transaction = buildTransactionFromDto(requestDto, transactionReference);
        transaction.setTransactionType(Transaction.TransactionType.DEPOSIT);
        transaction.setTransactionStatus(Transaction.TransactionStatus.COMPLETED);

        return mapToResponseDto(transactionRepository.save(transaction));
    }

    private TransactionResponseDto handleWithdrawal(TransactionRequestDto requestDto, AccountResponse sourceAccount) {
        String transactionReference = generateUniqueReference();

        if (sourceAccount.balance().compareTo(requestDto.getAmount()) < 0) {
            throw new RuntimeException("Số dư tài khoản không đủ");
        }

        BigDecimal newBalance = sourceAccount.balance().subtract(requestDto.getAmount());
        accountClient.updateAccountBalance(requestDto.getSourceAccountNumber(), newBalance);

        Transaction transaction = buildTransactionFromDto(requestDto, transactionReference);
        transaction.setTransactionType(Transaction.TransactionType.WITHDRAWAL);
        transaction.setTransactionStatus(Transaction.TransactionStatus.COMPLETED);

        return mapToResponseDto(transactionRepository.save(transaction));
    }

    private TransactionResponseDto handleTransfer(TransactionRequestDto requestDto, AccountResponse sourceAccount, AccountResponse destinationAccount) {
        String transactionReference = generateUniqueReference();

        if (sourceAccount.balance().compareTo(requestDto.getAmount()) < 0) {
            System.out.println("Số dư tài khoản nguồn: " + sourceAccount.balance());

            throw new RuntimeException("Số dư tài khoản không đủ");
        }

        // Trừ tiền nguồn
        accountClient.updateAccountBalance(requestDto.getSourceAccountNumber(), 
                sourceAccount.balance().subtract(requestDto.getAmount()));

        // Cộng tiền đích
        accountClient.updateAccountBalance(requestDto.getDestinationAccountNumber(), 
                destinationAccount.balance().add(requestDto.getAmount()));

        Transaction transaction = buildTransactionFromDto(requestDto, transactionReference);
        transaction.setTransactionType(Transaction.TransactionType.TRANSFER);
        transaction.setTransactionStatus(Transaction.TransactionStatus.COMPLETED);

        return mapToResponseDto(transactionRepository.save(transaction));
    }

    // Tách logic sinh mã để code gọn hơn
    private String generateUniqueReference() {
        String ref;
        do {
            ref = UUID.randomUUID().toString();
        } while (transactionRepository.findByTransactionReference(ref).isPresent());
        return ref;
    }

    // ... (Giữ nguyên các hàm build và map) ...
    private Transaction buildTransactionFromDto(TransactionRequestDto requestDto, String transactionReference) {
        return Transaction.builder()
                .transactionReference(transactionReference)
                .sourceAccountNumber(requestDto.getSourceAccountNumber())
                .destinationAccountNumber(requestDto.getDestinationAccountNumber())
                .amount(requestDto.getAmount())
                .transactionType(requestDto.getTransactionType())
                .transactionStatus(Transaction.TransactionStatus.PENDING)
                .currency(requestDto.getCurrency())
                .description(requestDto.getDescription())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    private TransactionResponseDto mapToResponseDto(Transaction transaction) {
        return TransactionResponseDto.builder()
                .transactionReference(transaction.getTransactionReference())
                .sourceAccountNumber(transaction.getSourceAccountNumber())
                .destinationAccountNumber(transaction.getDestinationAccountNumber())
                .amount(transaction.getAmount())
                .transactionType(transaction.getTransactionType())
                .transactionStatus(transaction.getTransactionStatus())
                .transactionCurrency(transaction.getCurrency())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}