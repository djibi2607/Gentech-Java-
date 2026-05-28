package com.abdoul.gentech_fintech.Repositories;

import com.abdoul.gentech_fintech.Models.RefreshModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshRepository extends JpaRepository<RefreshModel, Long> {
}
