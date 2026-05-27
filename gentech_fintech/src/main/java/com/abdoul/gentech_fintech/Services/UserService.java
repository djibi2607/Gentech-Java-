package com.abdoul.gentech_fintech.Services;

import com.abdoul.gentech_fintech.DTO.UserDTO;
import com.abdoul.gentech_fintech.Exceptions.BadRequestException;
import com.abdoul.gentech_fintech.Exceptions.ConflictException;
import com.abdoul.gentech_fintech.Models.UserModel;
import com.abdoul.gentech_fintech.Models.WalletModel;
import com.abdoul.gentech_fintech.Repositories.UserRepository;
import com.abdoul.gentech_fintech.Repositories.WalletRepository;
import com.abdoul.gentech_fintech.Util.Resend;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final WalletRepository walletRepository;
    private final Resend resend;

    public UserService (UserRepository userRepository, BCryptPasswordEncoder encoder, WalletRepository walletRepository, Resend resend){
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.walletRepository = walletRepository;
        this.resend = resend;
    }

    @Transactional
    public Map<String, String> createAccount(UserDTO.SignUp data){
        if (data.getEmail() == null && data.getPhone() == null){
            throw new BadRequestException("You must enter an email or phone number");
        }

        UserModel existingUser = userRepository.findByEmailOrPhone(data.getEmail(), data.getPhone());

        if (existingUser != null && !existingUser.isDeleted()){
            throw new ConflictException("Account already exists");
        }

        UserModel newUser = new UserModel();
        newUser.setName(data.getName());
        newUser.setEmail(data.getEmail());
        newUser.setPassword(encoder.encode(data.getPassword()));
        newUser.setPhone(data.getPhone());

        userRepository.saveAndFlush(newUser);

        WalletModel newWallet = new WalletModel();
        newWallet.setUser(newUser);

        walletRepository.save(newWallet);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("notice", "Welcome to Gentech " + data.getName());
        response.put("balance", newWallet.getBalance().toPlainString());

        resend.sendWelcomeEmail(data.getName(), "Account successfully created");
        return response;
    }
}
