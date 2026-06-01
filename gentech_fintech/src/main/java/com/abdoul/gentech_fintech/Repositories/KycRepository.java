package com.abdoul.gentech_fintech.Repositories;

import com.abdoul.gentech_fintech.Configuration.KycStatus;
import com.abdoul.gentech_fintech.Configuration.KycType;
import com.abdoul.gentech_fintech.Models.KycModel;
import com.abdoul.gentech_fintech.Models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.ZonedDateTime;
import java.util.List;

public interface KycRepository extends JpaRepository<KycModel, Long> {
    KycModel findByUserAndKycType(UserModel user, KycType kycType);
    @Query("SELECT k FROM KycModel k JOIN k.user u WHERE k.submittedAt < :tenDaysAgo AND k.status = :status AND k.kycRemainder = false AND u.deleted = false AND k.url IS NULL")
    List<KycModel> findKyc(@Param("tenDaysAgo")ZonedDateTime tenDayAgo, @Param("status")KycStatus kycStatus);
}
