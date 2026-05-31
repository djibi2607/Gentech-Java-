package com.abdoul.gentech_fintech.Services;

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

    public Map<String, String> unflagUser (AdminDTO.Unflag data, UserModel currentUser){

        UserModel user = null;

        if (data.getId() != null){
            user = userRepository.findById(data.getId()).orElse(null);
        }

        else if (data.getEmail() != null || data.getPhone() != null){
            user = userRepository.findByEmailOrPhone(data.getEmail(), data.getPhone());
        }

        if (user == null){
            throw new NotFoundException("User not found");
        }

        if (!currentUser.getRole().equals("admin")){
            currentUser.setFlagged(true);
            userRepository.save(currentUser);
            AuditLogs newLog = new AuditLogs();
            newLog.setUser(currentUser);
            newLog.setAction("User has been flagged for attempting to unflag user with id " + user.getId());
            logRepository.save(newLog);
            throw new ForbiddenException("Your account has been flagged.");
        }

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


}
