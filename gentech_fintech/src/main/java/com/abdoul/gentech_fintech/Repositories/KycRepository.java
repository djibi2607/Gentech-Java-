package com.abdoul.gentech_fintech.Repositories;

import com.abdoul.gentech_fintech.Configuration.KycStatus;
import com.abdoul.gentech_fintech.Configuration.KycType;
import com.abdoul.gentech_fintech.Models.KycModel;
import com.abdoul.gentech_fintech.Models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycRepository extends JpaRepository<KycModel, Long> {
    KycModel findByUserAndKycType(UserModel user, KycType kycType);
}
