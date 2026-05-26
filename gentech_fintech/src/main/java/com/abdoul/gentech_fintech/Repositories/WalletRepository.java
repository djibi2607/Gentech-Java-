package com.abdoul.gentech_fintech.Repositories;

import com.abdoul.gentech_fintech.Models.WalletModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<WalletModel, Long> {

}
