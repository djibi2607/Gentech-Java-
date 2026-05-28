package com.abdoul.gentech_fintech.Services;

import com.abdoul.gentech_fintech.DTO.UserDTO;
import com.abdoul.gentech_fintech.Exceptions.BadRequestException;
import com.abdoul.gentech_fintech.Exceptions.ConflictException;
import com.abdoul.gentech_fintech.Exceptions.NotFoundException;
import com.abdoul.gentech_fintech.Models.AuditLogs;
import com.abdoul.gentech_fintech.Models.UserModel;
import com.abdoul.gentech_fintech.Models.WalletModel;
import com.abdoul.gentech_fintech.Repositories.LogRepository;
import com.abdoul.gentech_fintech.Repositories.UserRepository;
import com.abdoul.gentech_fintech.Repositories.WalletRepository;
import com.abdoul.gentech_fintech.Util.JwtUtil;
import com.abdoul.gentech_fintech.Util.Resend;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
@Slf4j
@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final WalletRepository walletRepository;
    private final Resend resend;
    private final LogRepository logRepository;
    private final JwtUtil jwtUtil;

    public UserService (UserRepository userRepository, BCryptPasswordEncoder encoder, WalletRepository walletRepository, Resend resend, LogRepository logRepository, JwtUtil jwtUtil){
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.walletRepository = walletRepository;
        this.resend = resend;
        this.logRepository = logRepository;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public Map<String, String> createAccount(UserDTO.SignUp data){
        try {
            if (data.getEmail() == null && data.getPhone() == null) {
                throw new BadRequestException("You must enter an email or phone number");
            }

            UserModel existingUser = userRepository.findByEmailOrPhone(data.getEmail(), data.getPhone());

            if (existingUser != null && !existingUser.isDeleted()) {
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

            if (data.getEmail() != null) {
                resend.sendWelcomeEmail(data.getName(), "Account successfully created");
            }

            AuditLogs newLog = new AuditLogs();
            newLog.setUser(newUser);
            newLog.setAction("User created his Gentech account");

            logRepository.save(newLog);

            return response;

        }
        catch (Exception ex){
            log.error("Service failed {}", ex.getMessage());
            throw ex;
        }
    }

    @Transactional
    public Map<String, String> logIn (UserDTO.Login data){
        if (data.getEmail() == null && data.getPhone() == null){
            throw new BadRequestException("You must enter an email or phone number");
        }

        UserModel currentUser = userRepository.findByEmailOrPhone(data.getEmail(), data.getPhone());

        if (currentUser == null || currentUser.isDeleted()){
            throw new NotFoundException("Account not found. Please sign up first");
        }

        if (currentUser.isFaEnabled()){

        }

        String accessToken = jwtUtil.createAccessToken(currentUser.getId());


    }

}
