package com.abdoul.gentech_fintech.Repositories;

import com.abdoul.gentech_fintech.Models.RefreshModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.ZonedDateTime;
import java.util.List;

@Repository
public interface RefreshRepository extends JpaRepository<RefreshModel, Long> {
    void deleteByExpiresAtBefore(ZonedDateTime expired);
}
