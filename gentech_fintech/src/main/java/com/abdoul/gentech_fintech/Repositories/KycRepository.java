package com.abdoul.gentech_fintech.Repositories;

import com.abdoul.gentech_fintech.Models.KycModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KycRepository extends JpaRepository<KycModel, Long> {
}
