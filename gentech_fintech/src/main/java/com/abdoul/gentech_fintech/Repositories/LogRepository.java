package com.abdoul.gentech_fintech.Repositories;

import com.abdoul.gentech_fintech.Models.AuditLogs;
import com.abdoul.gentech_fintech.Models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LogRepository extends JpaRepository<AuditLogs, Long> {
    AuditLogs findTopByUserOrderByCreatedAtDesc (UserModel user);
}
