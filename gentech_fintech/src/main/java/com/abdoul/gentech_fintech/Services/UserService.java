package com.abdoul.gentech_fintech.Services;

import com.abdoul.gentech_fintech.DTO.UserDTO;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {



    public Map<String, String> createAccount(UserDTO.SignUp data){
        if (data.getEmail() == null && data.getPhone() == null){

        }
    }
}
