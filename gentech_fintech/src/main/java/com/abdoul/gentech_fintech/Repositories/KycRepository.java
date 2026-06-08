package com.abdoul.gentech_fintech.Repositories;

import com.abdoul.gentech_fintech.Configuration.KycStatus;
import com.abdoul.gentech_fintech.Configuration.KycType;
import com.abdoul.gentech_fintech.Models.KycModel;
import com.abdoul.gentech_fintech.Models.UserModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

public interface KycRepository extends JpaRepository<KycModel, Long> {
    KycModel findByUserAndKycType(UserModel user, KycType kycType);
    @Query("SELECT k FROM KycModel k JOIN k.user u WHERE k.submittedAt < :tenDaysAgo AND k.status = :status AND k.kycRemainder = false AND u.deleted = false AND k.url IS NULL")
    List<KycModel> findKyc(@Param("tenDaysAgo")ZonedDateTime tenDayAgo, @Param("status")KycStatus kycStatus);

    @Query("SELECT k FROM KycModel k JOIN k.user u WHERE k.status = :status AND u.deleted = false")
    Page<KycModel> findUnresolvedKyc (@Param("status") KycStatus status, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT k FROM KycModel k WHERE k.id = :id")
    Optional<KycModel> findByIdWithLock (@Param("id") Long id);
}
