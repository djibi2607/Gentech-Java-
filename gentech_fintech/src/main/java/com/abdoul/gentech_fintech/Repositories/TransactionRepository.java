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
}
