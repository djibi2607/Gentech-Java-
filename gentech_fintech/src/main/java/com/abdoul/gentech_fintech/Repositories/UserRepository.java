package com.abdoul.gentech_fintech.Repositories;

import com.abdoul.gentech_fintech.Models.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository <UserModel, Long>{
    UserModel findByEmailOrPhone(String email, String phone);
}
