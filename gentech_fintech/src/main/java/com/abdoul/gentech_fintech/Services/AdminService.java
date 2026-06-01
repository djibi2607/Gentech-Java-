package com.abdoul.gentech_fintech.Services;

import com.abdoul.gentech_fintech.Configuration.KycStatus;
import com.abdoul.gentech_fintech.DTO.AdminDTO;
import com.abdoul.gentech_fintech.Exceptions.BadRequestException;
import com.abdoul.gentech_fintech.Exceptions.ForbiddenException;
import com.abdoul.gentech_fintech.Exceptions.NotFoundException;
import com.abdoul.gentech_fintech.Models.AuditLogs;
import com.abdoul.gentech_fintech.Models.UserModel;
import com.abdoul.gentech_fintech.Repositories.LogRepository;
import com.abdoul.gentech_fintech.Repositories.UserRepository;
import com.abdoul.gentech_fintech.Util.Resend;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final LogRepository logRepository;
    private final Resend resend;

    public AdminService(UserRepository userRepository, LogRepository logRepository, Resend resend){
        this.userRepository = userRepository;
        this.logRepository = logRepository;
        this.resend = resend;
    }
    @Transactional
    public Map<String, String> unflagUser (AdminDTO.Unflag data, UserModel currentUser){

        requireAdmin(currentUser);
        UserModel user = findUser(data);

        if (!user.isFlagged()){
            throw new BadRequestException("User account is not flagged");
        }

        user.setFlagged(false);
        userRepository.save(user);
        AuditLogs newLog = new AuditLogs();
        newLog.setUser(currentUser);
        newLog.setAction("Admin has unflagged user with id " + user.getId());
        logRepository.save(newLog);

        resend.UnflagEmail(user.getName(), "The flag on your account has been lifted");

        Map<String, String> response = new LinkedHashMap<>();
        response.put("notice", user.getName() + " has been unflagged");

        return response;
    }

    @Transactional
    public Map<String, String> promoteAgent (AdminDTO.Unflag data, UserModel currentUser){
        requireAdmin(currentUser);
        UserModel user = findUser(data);

        if (user.isFlagged()){
            throw new BadRequestException("User account is flagged");
        }

        boolean kycVerified = user.getKyc().stream().allMatch(kyc -> kyc.getStatus().equals(KycStatus.Completed));

        if (!kycVerified){
            throw new BadRequestException("User must complete KYC before being promoted");
        }

        user.setRole("agents");
        userRepository.save(user);

        AuditLogs newLog = new AuditLogs();
        newLog.setAction("Admin " + currentUser.getId() + " has promoted user " + user.getId() + " to agent");
        newLog.setUser(currentUser);
        logRepository.save(newLog);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("notice", "User has successfully been promoted to agents");

        return response;
    }

    @Transactional
    public Map<String, String> deleteUser (AdminDTO.Unflag data, UserModel currentUser){
        requireAdmin(currentUser);
        UserModel user = findUser(data);
        user.setDeleted(true);
        userRepository.save(user);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("notice", "Account successfully deleted");

        AuditLogs newLog = new AuditLogs();
        newLog.setAction("Admin " + currentUser.getId() + " has deleted user " + user.getId());
        newLog.setUser(currentUser);
        logRepository.save(newLog);

        return response;
    }

    private UserModel findUser (AdminDTO.Unflag data){
        UserModel user = null;

        if (data.getId() != null){
            user = userRepository.findById(data.getId()).orElse(null);
        }

        else if (data.getEmail() != null || data.getPhone() != null){
            user = userRepository.findByEmailOrPhone(data.getEmail(), data.getPhone());
        }

        if (user == null || user.isDeleted()){
            throw new NotFoundException("User not found");
        }

        return user;
    }

    @Transactional
    private void requireAdmin(UserModel currentUser) {
        if (!currentUser.getRole().equals("admin")) {
            currentUser.setFlagged(true);
            userRepository.save(currentUser);
            AuditLogs newLog = new AuditLogs();
            newLog.setUser(currentUser);
            newLog.setAction("User flagged for attempting unauthorized admin action");
            logRepository.save(newLog);
            throw new ForbiddenException("Your account has been flagged.");
        }
    }

    @Transactional
    public Map<String, String> demoteAgent (AdminDTO.Unflag data, UserModel currentUser){
        requireAdmin(currentUser);
        UserModel user = findUser(data);

        if (!user.getRole().equals("agents")){
            throw new BadRequestException("User is not an agent");
        }

        user.setRole("users");
        userRepository.save(user);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("notice", "Account successfully demoted to user");

        AuditLogs newLog = new AuditLogs();
        newLog.setAction("Admin " + currentUser.getId() + " has demoted agent with id " + user.getId() + " to user");
        newLog.setUser(currentUser);
        logRepository.save(newLog);

        return response;
    }

    public AdminDTO.UserCredentials getUserDetails (UserModel currentUser, AdminDTO.Unflag data){
        requireAdmin(currentUser);
        UserModel user = findUser(data);


        AdminDTO.UserCredentials details = new AdminDTO.UserCredentials();
        details.setId(user.getId());
        details.setName(user.getName());
        details.setEmail(user.getEmail());
        details.setPhone(user.getPhone());
        details.setRole(user.getRole());
        details.setFlagged(user.isFlagged());
        details.setDeleted(user.isDeleted());
        details.setBalance(user.getWallet().getBalance());
        details.setCreatedAt(user.getCreatedAt());
        details.setTransactionsSent(user.getTransactionsSent());
        details.setTransactionsReceived(user.getTransactionsReceived());
        details.setKycModels(user.getKyc());
        details.setTwoFactorModel(user.getTwoFactor());
        details.setUserLogs(user.getLogs());

        AuditLogs newLog = new AuditLogs();
        newLog.setAction("Admin " + currentUser.getId() + " viewed credentials of user " + user.getId());
        newLog.setUser(currentUser);
        logRepository.save(newLog);

        return details;
    }

}
