package com.abdoul.gentech_fintech.Repositories;

import com.abdoul.gentech_fintech.Models.TwoFactorModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TwoFactorRepository extends JpaRepository<TwoFactorModel, Long> {
}
