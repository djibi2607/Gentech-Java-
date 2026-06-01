package com.abdoul.gentech_fintech.Repositories;

import com.abdoul.gentech_fintech.Models.WalletModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<WalletModel, Long> {
    @Lock(value = LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM WalletModel w WHERE w.id = :id")
    WalletModel findByIdWithLock(@Param("id") Long id);

}
