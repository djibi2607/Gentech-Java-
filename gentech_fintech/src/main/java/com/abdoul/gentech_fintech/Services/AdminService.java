package com.abdoul.gentech_fintech.Services;

import com.abdoul.gentech_fintech.DTO.AdminDTO;
import com.abdoul.gentech_fintech.Exceptions.BadRequestException;
import com.abdoul.gentech_fintech.Exceptions.ForbiddenException;
import com.abdoul.gentech_fintech.Exceptions.NotFoundException;
import com.abdoul.gentech_fintech.Models.AuditLogs;
import com.abdoul.gentech_fintech.Models.UserModel;
import com.abdoul.gentech_fintech.Repositories.LogRepository;
import com.abdoul.gentech_fintech.Repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class AdminService {
    private final UserRepository userRepository;
    private final LogRepository logRepository;

    public AdminService(UserRepository userRepository, LogRepository logRepository){
        this.userRepository = userRepository;
        this.logRepository = logRepository;
    }

    public Map<String, String> unflagUser (AdminDTO.Unflag data, UserModel currentUser){

        UserModel user = null;

        if (data.getId() != null){
            user = userRepository.findById(data.getId()).orElse(null);
        }

        if (data.getEmail() != null || data.getPhone() != null){
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

        Map<String, String> response = new LinkedHashMap<>();
        response.put("notice", user.getName() + " has been unflagged");

        return response;
    }


}
