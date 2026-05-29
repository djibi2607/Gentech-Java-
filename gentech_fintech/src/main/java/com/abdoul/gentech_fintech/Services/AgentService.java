package com.abdoul.gentech_fintech.Services;

import com.abdoul.gentech_fintech.DTO.AgentDTO;
import com.abdoul.gentech_fintech.Exceptions.BadRequestException;
import com.abdoul.gentech_fintech.Exceptions.ForbiddenException;
import com.abdoul.gentech_fintech.Exceptions.NotFoundException;
import com.abdoul.gentech_fintech.Models.AuditLogs;
import com.abdoul.gentech_fintech.Models.TwoFactorModel;
import com.abdoul.gentech_fintech.Models.UserModel;
import com.abdoul.gentech_fintech.Repositories.LogRepository;
import com.abdoul.gentech_fintech.Repositories.TwoFactorRepository;
import com.abdoul.gentech_fintech.Repositories.UserRepository;
import com.abdoul.gentech_fintech.Util.JwtUtil;
import com.abdoul.gentech_fintech.Util.Resend;
import com.abdoul.gentech_fintech.Util.TwoFactorUtil;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AgentService {
    private final UserRepository userRepository;
    private final List<String> allowed_roles = List.of("agents", "admin");
    private final Resend resend;
    private final TwoFactorUtil twoFactorUtil;
    private final LogRepository logRepository;
    private final JwtUtil jwt;
    private final TwoFactorRepository twoFactorRepository;

    public AgentService (UserRepository userRepository, Resend resend, TwoFactorUtil twoFactorUtil, LogRepository logRepository, JwtUtil jwt, TwoFactorRepository twoFactorRepository){
        this.userRepository = userRepository;
        this.resend = resend;
        this.twoFactorUtil = twoFactorUtil;
        this.logRepository = logRepository;
        this.jwt = jwt;
        this.twoFactorRepository = twoFactorRepository;
    }

    public Map<String, String> getUserCredentials (AgentDTO.UserCredentials data, UserModel currentUser){
        if (!allowed_roles.contains(currentUser.getRole())){
            throw new ForbiddenException("Unauthorized");
        }

        UserModel user = userRepository.findByEmailOrPhone(data.getEmail(), data.getPhone());

        if (user == null || user.isDeleted()){
            throw new NotFoundException("Account not found");
        }

        if (user.isFlagged()){
            throw new BadRequestException("Unable to deposit into a flagged account");
        }

        if (user.getId().equals(currentUser.getId())){
            currentUser.setFlagged(true);
            userRepository.save(currentUser);
            AuditLogs newLog = new AuditLogs();
            newLog.setAction("Agent has been flagged for trying to deposit in their own account");
            newLog.setUser(currentUser);
            logRepository.save(newLog);
            throw new BadRequestException("Your account has been flagged");
        }

        if (currentUser.isFaEnabled() && currentUser.getEmail() != null){

            String code = twoFactorUtil.create2FactorCode();

            resend.sendCodeEmail(user.getName(), "Deposit verification code", code);


            if (user.getTwoFactor() == null) {
                TwoFactorModel newCode = new TwoFactorModel();
                newCode.setUser(user);
                newCode.setCode(code);
                twoFactorRepository.save(newCode);
            }
            else {
                user.getTwoFactor().setCode(code);
                twoFactorRepository.save(user.getTwoFactor());
            }

            AuditLogs newLog = new AuditLogs();
            newLog.setUser(currentUser);
            newLog.setAction(currentUser.getRole() + "/ " + currentUser.getName() + "/ " + currentUser.getEmail() + "/ " + currentUser.getPhone() + " has requested a verification code to get the credentials for user with id " + user.getId());
            logRepository.save(newLog);
            Map<String, String> response = new LinkedHashMap<>();

            response.put ("notice", "A verification code has been sent to " + user.getName());

            String temporaryToken = jwt.createAccessToken(user.getId());

            response.put("temporary token", temporaryToken);

            return response;
        }

        AuditLogs newLog = new AuditLogs();
        newLog.setUser(currentUser);
        newLog.setAction(currentUser.getRole() + "/ " + currentUser.getName() + "/ " + currentUser.getEmail() + "/ " + currentUser.getPhone() + " has requested to get the credentials for user with id " + user.getId());
        logRepository.save(newLog);
        Map<String, String> response = new LinkedHashMap<>();

        response.put("name", user.getName());
        response.put("wallet-id", String.valueOf(user.getWallet().getId()));

        return response;
    }
}
