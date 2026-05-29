package com.abdoul.gentech_fintech.Repositories;

import com.abdoul.gentech_fintech.Models.TransactionModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionModel, Long> {

}
