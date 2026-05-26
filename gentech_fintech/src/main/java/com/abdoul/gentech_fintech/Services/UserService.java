package com.abdoul.gentech_fintech.Services;

import com.abdoul.gentech_fintech.DTO.UserDTO;
import com.abdoul.gentech_fintech.Exceptions.BadRequestException;
import com.abdoul.gentech_fintech.Exceptions.ConflictException;
import com.abdoul.gentech_fintech.Models.UserModel;
import com.abdoul.gentech_fintech.Repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService (UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public Map<String, String> createAccount(UserDTO.SignUp data){
        if (data.getEmail() == null && data.getPhone() == null){
            throw new BadRequestException("You must enter an email or phone number");
        }

        UserModel existingUser = userRepository.findByEmailOrPhone(data.getEmail(), data.getPhone());

        if (existingUser != null && !existingUser.isDeleted()){
            throw new ConflictException("Account already exists");
        }


    }
}
