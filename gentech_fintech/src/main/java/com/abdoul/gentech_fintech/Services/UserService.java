package com.abdoul.gentech_fintech.Services;

import com.abdoul.gentech_fintech.Configuration.KycStatus;
import com.abdoul.gentech_fintech.Configuration.KycType;
import com.abdoul.gentech_fintech.DTO.UserDTO;
import com.abdoul.gentech_fintech.Exceptions.BadRequestException;
import com.abdoul.gentech_fintech.Exceptions.ConflictException;
import com.abdoul.gentech_fintech.Exceptions.NotFoundException;
import com.abdoul.gentech_fintech.Models.*;
import com.abdoul.gentech_fintech.Repositories.*;
import com.abdoul.gentech_fintech.Util.JwtUtil;
import com.abdoul.gentech_fintech.Util.Resend;
import com.abdoul.gentech_fintech.Util.TwoFactorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
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
    private final RefreshRepository refreshRepository;
    private final KycRepository kycRepository;
    private final TwoFactorUtil twoFactor;
    private final TwoFactorRepository twoFactorRepository;

    public UserService (UserRepository userRepository, TwoFactorRepository twoFactorRepository, TwoFactorUtil twoFactor, BCryptPasswordEncoder encoder, WalletRepository walletRepository, Resend resend, LogRepository logRepository, JwtUtil jwtUtil, RefreshRepository refreshRepository, KycRepository kycRepository){
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.walletRepository = walletRepository;
        this.resend = resend;
        this.logRepository = logRepository;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
        this.kycRepository = kycRepository;
        this.twoFactor = twoFactor;
        this.twoFactorRepository = twoFactorRepository;
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

            KycModel idKyc = new KycModel();
            idKyc.setUser(newUser);
            idKyc.setKycType(KycType.id);

            KycModel selfieKyc = new KycModel();
            selfieKyc.setUser(newUser);
            selfieKyc.setKycType(KycType.selfie);

            List<KycModel> kyc = List.of(selfieKyc, idKyc);

            kycRepository.saveAll(kyc);


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

        Map<String, String> response = new LinkedHashMap<>();

        ZonedDateTime today = ZonedDateTime.now(ZoneId.of("UTC"));

        KycModel idKyc = kycRepository.findByUserAndKycType(currentUser, KycType.id);

        if (currentUser.getCreatedAt().plusDays(14).isBefore(today) && idKyc.getStatus() == KycStatus.Pending){
            throw new BadRequestException("This account has been deactivated because of failure to kyc submission");
        }

        KycModel selfieKyc = kycRepository.findByUserAndKycType(currentUser, KycType.selfie);

        if (currentUser.getCreatedAt().plusDays(14).isBefore(today) && selfieKyc.getStatus() == KycStatus.Pending){
            throw new BadRequestException("This account has been deactivated because of failure to kyc submission");
        }

        if (currentUser.isFaEnabled()){
            String temporaryToken = jwtUtil.createAccessToken(currentUser.getId());

            String code = twoFactor.create2FactorCode();

            TwoFactorModel twoFactorModel = currentUser.getTwoFactor();

            if (twoFactorModel != null){
                twoFactorModel.setCode(code);
            }
            else {
                TwoFactorModel new2fa = new TwoFactorModel();
                new2fa.setUser(currentUser);
                new2fa.setCode(code);

                twoFactorRepository.saveAndFlush(new2fa);
            }

            response.put("temporary token", temporaryToken);

        }

        String accessToken = jwtUtil.createAccessToken(currentUser.getId());

        String refreshToken = jwtUtil.createRefresh();

        RefreshModel newRefresh = new RefreshModel();
        newRefresh.setUser(currentUser);
        newRefresh.setToken(refreshToken);

        refreshRepository.save(newRefresh);

        AuditLogs newLog = new AuditLogs();
        newLog.setAction("User logged in with 2fa disabled");
        newLog.setUser(currentUser);

        logRepository.save(newLog);

        response.put ("notice", "Login successful");
        response.put("access token", accessToken);
        response.put("refresh token", refreshToken);
        response.put("token type", "Bearer ");

        return response;
    }

}
