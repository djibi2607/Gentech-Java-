package com.abdoul.gentech_fintech.Repositories;

import com.abdoul.gentech_fintech.Configuration.TransType;
import com.abdoul.gentech_fintech.Models.TransactionModel;
import com.abdoul.gentech_fintech.Models.WalletModel;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

public interface TransactionRepository extends JpaRepository<TransactionModel, Long> {
    @Query("SELECT COUNT(t) FROM TransactionModel t where t.transType = :type AND t.createdAt >= :start")
    Long countTodayTransactions(@Param("type")TransType transType, @Param("start")ZonedDateTime today);

    @Query("SELECT SUM(t.amount) FROM TransactionModel t WHERE t.createdAt >= :start")
    BigDecimal sumTodayAmount (@Param("start") ZonedDateTime today);

    Page<TransactionModel> findBySenderWalletOrReceiverWallet (WalletModel sender, WalletModel receiver, Pageable pageable);

    @Query(value = "SELECT * FROM transactions t WHERE " +
            "(to_tsvector ('english', t.description) @@plainto_tsquery ('english', :keyword) OR :keyword IS NULL) " +
            "AND (t.trans_type = :type OR CAST(:type AS VARCHAR) IS NULL) " +
            "AND (t.sender_wallet = :senderWalletId OR t.receiver_wallet = :receiverWalletId)", nativeQuery = true)
    Page<TransactionModel> findTransactionByDescriptionAndType (@Param("keyword") String description, @Param("type") TransType type, @Param("senderWalletId") Long senderWalletId, @Param("receiverWalletId") Long receiverWalletId, Pageable pageable);
}
