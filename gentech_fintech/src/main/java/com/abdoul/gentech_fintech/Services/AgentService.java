package com.abdoul.gentech_fintech.Services;

import com.abdoul.gentech_fintech.Configuration.TransType;
import com.abdoul.gentech_fintech.DTO.AgentDTO;
import com.abdoul.gentech_fintech.Exceptions.BadRequestException;
import com.abdoul.gentech_fintech.Exceptions.ForbiddenException;
import com.abdoul.gentech_fintech.Exceptions.NotFoundException;
import com.abdoul.gentech_fintech.Models.*;
import com.abdoul.gentech_fintech.Repositories.*;
import com.abdoul.gentech_fintech.Util.JwtUtil;
import com.abdoul.gentech_fintech.Util.Resend;
import com.abdoul.gentech_fintech.Util.TwoFactorUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public AgentService (UserRepository userRepository, Resend resend, TwoFactorUtil twoFactorUtil, LogRepository logRepository, JwtUtil jwt, TwoFactorRepository twoFactorRepository, WalletRepository walletRepository, TransactionRepository transactionRepository){
        this.userRepository = userRepository;
        this.resend = resend;
        this.twoFactorUtil = twoFactorUtil;
        this.logRepository = logRepository;
        this.jwt = jwt;
        this.twoFactorRepository = twoFactorRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
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
        newLog.setAction("Agent/ " + currentUser.getName() + "/ " + currentUser.getEmail() + "/ " + currentUser.getPhone() + " has requested to get the credentials for user with id " + user.getId());
        logRepository.save(newLog);
        Map<String, String> response = new LinkedHashMap<>();

        response.put("name", user.getName());
        response.put("wallet-id", String.valueOf(user.getWallet().getId()));
        response.put("user-id", String.valueOf(user.getId()));

        return response;
    }

    @Transactional
    public Map<String, String> deposit (AgentDTO.DepositWith data, UserModel currentUser){
        if (!allowed_roles.contains(currentUser.getRole())){
            throw new ForbiddenException("Unauthorized");
        }

        WalletModel userWallet = walletRepository.findByIdWithLock(data.getWalletId());

        if (userWallet == null){
            throw new NotFoundException("Wallet not found");
        }

        if (userWallet.getId().equals(currentUser.getWallet().getId())){
            currentUser.setFlagged(true);
            userRepository.save(currentUser);
            AuditLogs newLog = new AuditLogs();
            newLog.setUser(currentUser);
            newLog.setAction("Agent has been flagged for attempting to deposit in their account");
            logRepository.save(newLog);
            throw new ForbiddenException("Your account has been flagged");
        }

        TransactionModel newTrans = new TransactionModel();
        newTrans.setAmount(data.getAmount());
        newTrans.setTransType(TransType.DEPOSIT);
        newTrans.setReceiverWallet(userWallet);
        newTrans.setDescription(data.getDescription());

        transactionRepository.save(newTrans);

        BigDecimal newBalance = userWallet.getBalance().add(data.getAmount());
        userWallet.setBalance(newBalance);
        walletRepository.save(userWallet);

        AuditLogs agentLog = new AuditLogs();
        agentLog.setAction("Agent/ " + currentUser.getName() + "/ " + currentUser.getEmail() + "/ " + currentUser.getPhone() + " has deposited $" + data.getAmount() + "into user with id " + userWallet.getUser().getId() + " wallet");
        agentLog.setUser(currentUser);

        AuditLogs userLog = new AuditLogs();
        userLog.setUser(userWallet.getUser());
        userLog.setAction("User deposited $" + data.getAmount());

        List<AuditLogs> logs = List.of(userLog, agentLog);

        logRepository.saveAll(logs);

        resend.sendTransactionEmail(userWallet.getUser().getName(), "Deposit has been made to your account", data.getAmount(), TransType.DEPOSIT);
        Map<String, String> response = new LinkedHashMap<>();
        response.put("notice", "Deposit successful");

        return response;
    }

    @Transactional
    public Map<String, String> depositWith2fa (AgentDTO.DepositWith2fa data, UserModel currentUser){
        if (!allowed_roles.contains(currentUser.getRole())){
            throw new ForbiddenException("Unauthorized");
        }

        if (!jwt.isTokenValid(data.getToken())) {
            throw new BadRequestException("Invalid or expired token");
        }

        Long id = Long.parseLong(jwt.extractIdFromToken(data.getToken()));

        UserModel user = userRepository.findById(id).orElse(null);

        if (user == null){
            throw new RuntimeException("User not found");
        }

        WalletModel userWallet = walletRepository.findByIdWithLock(user.getWallet().getId());

        if (userWallet == null){
            throw new NotFoundException("Wallet not found");
        }

        if (userWallet.getUser().getTwoFactor().getExpiresAt().isBefore(ZonedDateTime.now(ZoneId.of("UTC"))) || userWallet.getUser().getTwoFactor().isRevoked()) {
            throw new BadRequestException("Expired code");
        }

        if (!userWallet.getUser().getTwoFactor().getCode().equals(data.getCode())){
            throw new BadRequestException("Invalid code");
        }

        userWallet.getUser().getTwoFactor().setRevoked(true);
        twoFactorRepository.save(userWallet.getUser().getTwoFactor());

        if (userWallet.getId().equals(currentUser.getWallet().getId())){
            currentUser.setFlagged(true);
            userRepository.save(currentUser);
            AuditLogs newLog = new AuditLogs();
            newLog.setUser(currentUser);
            newLog.setAction("Agent has been flagged for attempting to deposit in their account");
            logRepository.save(newLog);
            throw new ForbiddenException("Your account has been flagged");
        }

        TransactionModel newTrans = new TransactionModel();
        newTrans.setAmount(data.getAmount());
        newTrans.setTransType(TransType.DEPOSIT);
        newTrans.setReceiverWallet(userWallet);
        newTrans.setDescription(data.getDescription());

        transactionRepository.save(newTrans);

        BigDecimal newBalance = userWallet.getBalance().add(data.getAmount());
        userWallet.setBalance(newBalance);
        walletRepository.save(userWallet);

        AuditLogs agentLog = new AuditLogs();
        agentLog.setAction("Agent/ " + currentUser.getName() + "/ " + currentUser.getEmail() + "/ " + currentUser.getPhone() + " has deposited $" + data.getAmount() + "into user with id " + userWallet.getUser().getId() + " wallet with 2fa verification");
        agentLog.setUser(currentUser);

        AuditLogs userLog = new AuditLogs();
        userLog.setUser(userWallet.getUser());
        userLog.setAction("User deposited $" + data.getAmount());

        List<AuditLogs> logs = List.of(userLog, agentLog);

        logRepository.saveAll(logs);

        resend.sendTransactionEmail(userWallet.getUser().getName(), "Deposit has been made to your account", data.getAmount(), TransType.DEPOSIT);
        Map<String, String> response = new LinkedHashMap<>();
        response.put("notice", "Deposit successful");

        return response;
    }

    @Transactional
    public Map<String, String> withdraw (AgentDTO.DepositWith data, UserModel currentUser){
        if (!allowed_roles.contains(currentUser.getRole())){
            throw new ForbiddenException("Unauthorized");
        }

        WalletModel userWallet = walletRepository.findByIdWithLock(data.getWalletId());

        if (userWallet == null){
            throw new NotFoundException("Wallet not found");
        }

        if (userWallet.getId().equals(currentUser.getWallet().getId())){
            currentUser.setFlagged(true);
            userRepository.save(currentUser);
            AuditLogs newLog = new AuditLogs();
            newLog.setUser(currentUser);
            newLog.setAction("Agent has been flagged for attempting to deposit in their account");
            logRepository.save(newLog);
            throw new ForbiddenException("Your account has been flagged");
        }

        if (userWallet.getBalance().compareTo(data.getAmount()) < 0){
            throw new BadRequestException("Insufficient funds");
        }

        TransactionModel newTrans = new TransactionModel();
        newTrans.setAmount(data.getAmount());
        newTrans.setTransType(TransType.WITHDRAWAL);
        newTrans.setSenderWallet(userWallet);
        newTrans.setDescription(data.getDescription());

        transactionRepository.save(newTrans);

        BigDecimal newBalance = userWallet.getBalance().subtract(data.getAmount());
        userWallet.setBalance(newBalance);
        walletRepository.save(userWallet);

        AuditLogs agentLog = new AuditLogs();
        agentLog.setAction("Agent/ " + currentUser.getName() + "/ " + currentUser.getEmail() + "/ " + currentUser.getPhone() + " has withdrawn $" + data.getAmount() + "into user with id " + userWallet.getUser().getId() + " wallet");
        agentLog.setUser(currentUser);

        AuditLogs userLog = new AuditLogs();
        userLog.setUser(userWallet.getUser());
        userLog.setAction("User withdrew $" + data.getAmount());

        List<AuditLogs> logs = List.of(userLog, agentLog);

        logRepository.saveAll(logs);

        resend.sendTransactionEmail(userWallet.getUser().getName(), "Withdrawal has been made to your account", data.getAmount(), TransType.WITHDRAWAL);
        Map<String, String> response = new LinkedHashMap<>();
        response.put("notice", "Withdrawal successful");

        return response;
    }

    @Transactional
    public Map<String, String> withdrawWith2fa (AgentDTO.DepositWith2fa data, UserModel currentUser){
        if (!allowed_roles.contains(currentUser.getRole())){
            throw new ForbiddenException("Unauthorized");
        }

        if (!jwt.isTokenValid(data.getToken())) {
            throw new BadRequestException("Invalid or expired token");
        }

        Long id = Long.parseLong(jwt.extractIdFromToken(data.getToken()));

        UserModel user = userRepository.findById(id).orElse(null);

        if (user == null){
            throw new RuntimeException("User not found");
        }

        WalletModel userWallet = walletRepository.findByIdWithLock(user.getWallet().getId());

        if (userWallet == null){
            throw new NotFoundException("Wallet not found");
        }

        if (userWallet.getUser().getTwoFactor().getExpiresAt().isBefore(ZonedDateTime.now(ZoneId.of("UTC"))) || userWallet.getUser().getTwoFactor().isRevoked()) {
            throw new BadRequestException("Expired code");
        }

        if (!userWallet.getUser().getTwoFactor().getCode().equals(data.getCode())){
            throw new BadRequestException("Invalid code");
        }

        if (userWallet.getBalance().compareTo(data.getAmount()) < 0){
            throw new BadRequestException("Insufficient funds");
        }

        userWallet.getUser().getTwoFactor().setRevoked(true);
        twoFactorRepository.save(userWallet.getUser().getTwoFactor());

        if (userWallet.getId().equals(currentUser.getWallet().getId())){
            currentUser.setFlagged(true);
            userRepository.save(currentUser);
            AuditLogs newLog = new AuditLogs();
            newLog.setUser(currentUser);
            newLog.setAction("Agent has been flagged for attempting to withdraw from their account");
            logRepository.save(newLog);
            throw new ForbiddenException("Your account has been flagged");
        }

        TransactionModel newTrans = new TransactionModel();
        newTrans.setAmount(data.getAmount());
        newTrans.setTransType(TransType.WITHDRAWAL);
        newTrans.setSenderWallet(userWallet);
        newTrans.setDescription(data.getDescription());

        transactionRepository.save(newTrans);

        BigDecimal newBalance = userWallet.getBalance().subtract(data.getAmount());
        userWallet.setBalance(newBalance);
        walletRepository.save(userWallet);

        AuditLogs agentLog = new AuditLogs();
        agentLog.setAction("Agent/ " + currentUser.getName() + "/ " + currentUser.getEmail() + "/ " + currentUser.getPhone() + " has withdrawn $" + data.getAmount() + "into user with id " + userWallet.getUser().getId() + " wallet with 2fa verification");
        agentLog.setUser(currentUser);

        AuditLogs userLog = new AuditLogs();
        userLog.setUser(userWallet.getUser());
        userLog.setAction("User withdrew $" + data.getAmount());

        List<AuditLogs> logs = List.of(userLog, agentLog);

        logRepository.saveAll(logs);

        resend.sendTransactionEmail(userWallet.getUser().getName(), "Withdraw has been made to your account", data.getAmount(), TransType.WITHDRAWAL);
        Map<String, String> response = new LinkedHashMap<>();
        response.put("notice", "Withdrawal successful");

        return response;
    }

    @Transactional
    public Map<String, String> flagUser (AgentDTO.Flag data, UserModel currentUser){
        if (!allowed_roles.contains(currentUser.getRole())){
            throw new ForbiddenException("Unauthorized");
        }

        UserModel user = null;

        if (data.getId() != null){
            user = userRepository.findById(data.getId()).orElse(null);
        }

        if (data.getEmail() != null || data.getPhone() != null){
            user = userRepository.findByEmailOrPhone(data.getEmail(), data.getPhone());
        }

        if (user == null) {
            throw new NotFoundException("User not found");
        }


        if (user.isFlagged()) {
            throw new BadRequestException("User is already flagged");
        }

        if (user.getId().equals(currentUser.getId())){
            AuditLogs newLog = new AuditLogs();
            newLog.setAction("Agent attempted to flag his account");
            newLog.setUser(user);
            logRepository.save(newLog);
            throw new ForbiddenException("Unauthorized");
        }

        user.setFlagged(true);
        userRepository.save(user);

        AuditLogs newLog = new AuditLogs();
        newLog.setAction("Agent/ " + currentUser.getName() + "/" + currentUser.getEmail() + "/" + currentUser.getPhone() + " has flagged user with id " + user.getId());
        newLog.setUser(currentUser);
        logRepository.save(newLog);

        Map<String,String> response = new LinkedHashMap<>();
        response.put("notice", "Successfully flagged");

        return response;
    }
}
