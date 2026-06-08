package com.abdoul.gentech_fintech.Services;

import com.abdoul.gentech_fintech.Configuration.KycStatus;
import com.abdoul.gentech_fintech.Configuration.KycType;
import com.abdoul.gentech_fintech.Configuration.TransType;
import com.abdoul.gentech_fintech.DTO.UserDTO;
import com.abdoul.gentech_fintech.Exceptions.*;
import com.abdoul.gentech_fintech.Models.*;
import com.abdoul.gentech_fintech.Repositories.*;
import com.abdoul.gentech_fintech.Util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private final IpUtil ipUtil;
    private final UserAgentUtil userAgentUtil;
    private final TransactionRepository transactionRepository;
    private final CacheManager cacheManager;
    private final S3Util s3;

    public UserService (S3Util s3,UrlUtil url,TransactionRepository transactionRepository,UserAgentUtil userAgentUtil,IpUtil ipUtil, UserRepository userRepository, TwoFactorRepository twoFactorRepository, TwoFactorUtil twoFactor,CacheManager cacheManager, BCryptPasswordEncoder encoder, WalletRepository walletRepository, Resend resend, LogRepository logRepository, JwtUtil jwtUtil, RefreshRepository refreshRepository, KycRepository kycRepository){
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
        this.ipUtil = ipUtil;
        this.userAgentUtil = userAgentUtil;
        this.transactionRepository = transactionRepository;
        this.cacheManager = cacheManager;
        this.s3 = s3;
    }

    @Transactional
    public Map<String, String> createAccount(UserDTO.SignUp data, String ip, String device){
        try {
            if (data.getEmail() == null && data.getPhone() == null) {
                throw new BadRequestException("You must enter an email or phone number");
            }

            UserModel existingUser = userRepository.findByEmailOrPhone(data.getEmail(), data.getPhone());

            if (existingUser != null && !existingUser.isDeleted()) {
                throw new ConflictException("Account already exists");
            }

            Map<String, String> infos = ipUtil.getIpDetails(ip);
            Map<String, String> userAgents = userAgentUtil.getDeviceInfo(device);

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
            idKyc.setKycType(KycType.ID);

            KycModel selfieKyc = new KycModel();
            selfieKyc.setUser(newUser);
            selfieKyc.setKycType(KycType.SELFIE);

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
            newLog.setCity(infos.get("City"));
            newLog.setCountry(infos.get("Country"));
            newLog.setLongitude(infos.get("Longitude"));
            newLog.setLatitude(infos.get("Latitude"));
            newLog.setOs(userAgents.get("OS"));
            newLog.setDevice(userAgents.get("Device"));
            newLog.setBrowser(userAgents.get("Browser"));

            logRepository.save(newLog);

            return response;

        }
        catch (Exception ex){
            log.error("Service failed {}", ex.getMessage());
            throw ex;
        }
    }

    @Transactional
    public Map<String, String> logIn (UserDTO.Login data, String ip, String device){
        if (data.getEmail() == null && data.getPhone() == null){
            throw new BadRequestException("You must enter an email or phone number");
        }

        UserModel currentUser = userRepository.findByEmailOrPhone(data.getEmail(), data.getPhone());

        if (currentUser == null || currentUser.isDeleted()){
            throw new NotFoundException("Account not found. Please sign up first");
        }

        if (!encoder.matches(data.getPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Invalid credentials");
        }

        Map<String, String> infos = ipUtil.getIpDetails(ip);
        Map<String, String> userAgents = userAgentUtil.getDeviceInfo(device);

        Map<String, String> response = new LinkedHashMap<>();

        ZonedDateTime today = ZonedDateTime.now(ZoneId.of("UTC"));

        KycModel idKyc = kycRepository.findByUserAndKycType(currentUser, KycType.ID);

        if (currentUser.getCreatedAt().plusDays(14).isBefore(today) && idKyc.getStatus() == KycStatus.Pending){
            throw new BadRequestException("This account has been deactivated because of failure to kyc submission");
        }

        KycModel selfieKyc = kycRepository.findByUserAndKycType(currentUser, KycType.SELFIE);

        if (currentUser.getCreatedAt().plusDays(14).isBefore(today) && selfieKyc.getStatus() == KycStatus.Pending){
            throw new BadRequestException("This account has been deactivated because of failure to kyc submission");
        }

        if (currentUser.isFaEnabled()){
            String temporaryToken = jwtUtil.createAccessToken(currentUser.getId());

            String code = twoFactor.create2FactorCode();

            TwoFactorModel twoFactorModel = currentUser.getTwoFactor();

            if (twoFactorModel != null){
                twoFactorModel.setCode(code);
                twoFactorRepository.save(twoFactorModel);
            }
            else {
                TwoFactorModel new2fa = new TwoFactorModel();
                new2fa.setUser(currentUser);
                new2fa.setCode(code);

                twoFactorRepository.saveAndFlush(new2fa);
            }

            resend.sendCodeEmail(currentUser.getName(), "Verification Code", code);

            response.put("notice", "A verification code has been sent to your email");
            response.put("temporary token", temporaryToken);

            AuditLogs newLog = new AuditLogs();
            newLog.setUser(currentUser);
            newLog.setAction("User received an email containing a verification code to login with 2fa");
            newLog.setCity(infos.get("City"));
            newLog.setCountry(infos.get("Country"));
            newLog.setLongitude(infos.get("Longitude"));
            newLog.setLatitude(infos.get("Latitude"));
            newLog.setOs(userAgents.get("OS"));
            newLog.setDevice(userAgents.get("Device"));
            newLog.setBrowser(userAgents.get("Browser"));

            logRepository.save(newLog);

            return response;

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
        newLog.setCity(infos.get("City"));
        newLog.setCountry(infos.get("Country"));
        newLog.setLongitude(infos.get("Longitude"));
        newLog.setLatitude(infos.get("Latitude"));
        newLog.setOs(userAgents.get("OS"));
        newLog.setDevice(userAgents.get("Device"));
        newLog.setBrowser(userAgents.get("Browser"));

        logRepository.save(newLog);

        response.put ("notice", "Login successful");
        response.put("access token", accessToken);
        response.put("refresh token", refreshToken);
        response.put("token type", "Bearer ");

        return response;
    }

    @Transactional(noRollbackFor = BadRequestException.class)
    public Map<String, String> loginWith2fa (UserDTO.LoginWith2fa data, String ip, String device){
        if (!jwtUtil.isTokenValid(data.getToken())){
            throw new JwtException("Invalid credentials");
        }

        Map<String, String> infos = ipUtil.getIpDetails(ip);
        Map<String, String> userAgents = userAgentUtil.getDeviceInfo(device);

        Long id = Long.parseLong(jwtUtil.extractIdFromToken(data.getToken()));

        UserModel currentUser = userRepository.findById(id).orElse(null);

        if (currentUser == null || currentUser.isDeleted()){
            throw new NotFoundException("Account not found");

        }

        if (currentUser.getTwoFactor().getExpiresAt().isBefore(ZonedDateTime.now(ZoneId.of("UTC"))) || currentUser.getTwoFactor().isRevoked()){
            AuditLogs newLog = new AuditLogs();
            newLog.setUser(currentUser);
            newLog.setAction("Failed login attempt for expired code");
            newLog.setCity(infos.get("City"));
            newLog.setCountry(infos.get("Country"));
            newLog.setLongitude(infos.get("Longitude"));
            newLog.setLatitude(infos.get("Latitude"));
            newLog.setOs(userAgents.get("OS"));
            newLog.setDevice(userAgents.get("Device"));
            newLog.setBrowser(userAgents.get("Browser"));
            logRepository.save(newLog);
            throw new BadRequestException("Expired code");
        }

        if (!currentUser.getTwoFactor().getCode().equals(data.getCode())){
            AuditLogs newLog = new AuditLogs();
            newLog.setUser(currentUser);
            newLog.setAction("Failed login attempt for wrong verification code");
            newLog.setCity(infos.get("City"));
            newLog.setCountry(infos.get("Country"));
            newLog.setLongitude(infos.get("Longitude"));
            newLog.setLatitude(infos.get("Latitude"));
            newLog.setOs(userAgents.get("OS"));
            newLog.setDevice(userAgents.get("Device"));
            newLog.setBrowser(userAgents.get("Browser"));
            logRepository.save(newLog);
            throw new BadRequestException("Invalid code");
        }

        currentUser.getTwoFactor().setRevoked(true);

        AuditLogs newLog = new AuditLogs();
        newLog.setUser(currentUser);
        newLog.setAction("User successfully logged in with 2fa factor");
        newLog.setCity(infos.get("City"));
        newLog.setCountry(infos.get("Country"));
        newLog.setLongitude(infos.get("Longitude"));
        newLog.setLatitude(infos.get("Latitude"));
        newLog.setOs(userAgents.get("OS"));
        newLog.setDevice(userAgents.get("Device"));
        newLog.setBrowser(userAgents.get("Browser"));
        logRepository.save(newLog);

        String accessToken = jwtUtil.createAccessToken(id);

        String refreshToken = jwtUtil.createRefresh();

        RefreshModel newRefresh = new RefreshModel();
        newRefresh.setToken(refreshToken);
        newRefresh.setUser(currentUser);

        refreshRepository.save(newRefresh);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("notice", "Login successful");
        response.put("access token", accessToken);
        response.put("refresh token", refreshToken);
        response.put("token type", "Bearer ");

        return response;
    }

    @Transactional
    public Map<String, String> transfer (UserDTO.Transfer data, UserModel currentUser, String ip, String device){
        if (currentUser.isFlagged()){
            throw new ForbiddenException("Unable to transfer due to account flag");
        }

        UserModel receiver = userRepository.findByEmailOrPhone(data.getReceiverEmail(), data.getReceiverPhone());

        if (receiver == null || receiver.isDeleted()){
            throw new NotFoundException("Receiver account not found");
        }

        WalletModel senderWallet = walletRepository.findByIdWithLock(currentUser.getWallet().getId());
        WalletModel receiverWallet = walletRepository.findByIdWithLock(receiver.getWallet().getId());

        if (senderWallet.getBalance().compareTo(data.getAmount()) < 0){
            throw new BadRequestException("Insufficient funds");
        }

        if (currentUser.getId().equals(receiver.getId())){
            throw new BadRequestException("Unable to transfer money to your account");
        }

        Map<String, String> infos = ipUtil.getIpDetails(ip);
        Map<String, String> userAgents = userAgentUtil.getDeviceInfo(device);

        BigDecimal senderBalance = senderWallet.getBalance().subtract(data.getAmount());
        BigDecimal receiverBalance = receiverWallet.getBalance().add(data.getAmount());

        senderWallet.setBalance(senderBalance);
        receiverWallet.setBalance(receiverBalance);
        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        TransactionModel senderTrans = new TransactionModel();
        senderTrans.setSenderWallet(currentUser.getWallet());
        senderTrans.setTransType(TransType.TRANSFER_OUT);
        senderTrans.setDescription(data.getDescription());
        senderTrans.setReceiverWallet(receiver.getWallet());
        senderTrans.setAmount(data.getAmount());

        TransactionModel receiverTrans = new TransactionModel();
        receiverTrans.setSenderWallet(currentUser.getWallet());
        receiverTrans.setTransType(TransType.TRANSFER_IN);
        receiverTrans.setDescription(data.getDescription());
        receiverTrans.setReceiverWallet(receiver.getWallet());
        receiverTrans.setAmount(data.getAmount());

        transactionRepository.save(senderTrans);
        transactionRepository.save(receiverTrans);

        AuditLogs newLog = new AuditLogs();
        newLog.setUser(currentUser);
        newLog.setAction("User transferred $" + data.getAmount() + " to user with id " + receiver.getId());
        newLog.setCity(infos.get("City"));
        newLog.setCountry(infos.get("Country"));
        newLog.setLongitude(infos.get("Longitude"));
        newLog.setLatitude(infos.get("Latitude"));
        newLog.setOs(userAgents.get("OS"));
        newLog.setDevice(userAgents.get("Device"));
        newLog.setBrowser(userAgents.get("Browser"));
        logRepository.save(newLog);

        AuditLogs newLog1 = new AuditLogs();
        newLog1.setUser(receiver);
        newLog1.setAction("User receiver $" + data.getAmount() + " from user with id " + currentUser.getId());
        newLog1.setCity(infos.get("City"));
        newLog1.setCountry(infos.get("Country"));
        newLog1.setLongitude(infos.get("Longitude"));
        newLog1.setLatitude(infos.get("Latitude"));
        newLog1.setOs(userAgents.get("OS"));
        newLog1.setDevice(userAgents.get("Device"));
        newLog1.setBrowser(userAgents.get("Browser"));
        logRepository.save(newLog1);

        cacheManager.getCache("userBalance").evictIfPresent(currentUser.getId());
        cacheManager.getCache("userBalance").evictIfPresent(receiver.getId());

        Map<String, String> response = new LinkedHashMap<>();
        response.put("notice", "Your transfer to " + receiver.getName() + " has been successfully");

        return response;
    }

    @Transactional(noRollbackFor = BadRequestException.class)
    public Map<String, String> refreshToken (UserDTO.Refresh data, UserModel currentUser, String ip, String device){
        RefreshModel refresh = refreshRepository.findTopByUserOrderByCreatedAtDesc(currentUser);

        if (refresh == null){
            throw new UnauthorizedException("Invalid token");
        }

        Map<String, String> infos = ipUtil.getIpDetails(ip);
        Map<String, String> userAgents = userAgentUtil.getDeviceInfo(device);

        if (!refresh.getToken().equals(data.getToken())){
            throw new UnauthorizedException("Invalid token");
        }

        if (refresh.isRevoked() || refresh.getExpiresAt().isBefore(ZonedDateTime.now(ZoneId.of("UTC")))){
            currentUser.setFlagged(true);
            userRepository.save(currentUser);
            AuditLogs newLog1 = new AuditLogs();
            newLog1.setUser(currentUser);
            newLog1.setAction("User has been flagged for using invalid or expired tokens");
            newLog1.setCity(infos.get("City"));
            newLog1.setCountry(infos.get("Country"));
            newLog1.setLongitude(infos.get("Longitude"));
            newLog1.setLatitude(infos.get("Latitude"));
            newLog1.setOs(userAgents.get("OS"));
            newLog1.setDevice(userAgents.get("Device"));
            newLog1.setBrowser(userAgents.get("Browser"));
            logRepository.save(newLog1);
            throw new BadRequestException("Invalid credentials. Your account has been flagged");
        }

        refresh.setRevoked(true);
        refreshRepository.save(refresh);

        String accessToken = jwtUtil.createAccessToken(currentUser.getId());
        String refreshToken = jwtUtil.createRefresh();

        RefreshModel newRefresh = new RefreshModel();
        newRefresh.setUser(currentUser);
        newRefresh.setToken(refreshToken);
        refreshRepository.save(newRefresh);

        Map<String, String> response = new LinkedHashMap<>();
        response.put("access token", accessToken);
        response.put("refresh token", refreshToken);
        response.put("token type", "Bearer ");

        return response;
    }

    @Cacheable(cacheNames = "userBalance", key = "#currentUser.id")
    public Map<String , BigDecimal> getBalance (UserModel currentUser){
        Map<String, BigDecimal> response = new LinkedHashMap<>();

        response.put("balance", currentUser.getWallet().getBalance());

        return response;
    }

    @Cacheable(cacheNames = "userTransactions", key = "#currentUser.id + '_' + #page + '_' + #size")
    public List<UserDTO.Transactions> getAllTransactions (UserModel currentUser, int page, int size){
        if (page < 1){
            throw new BadRequestException("Page number can't be less than 1");
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<TransactionModel> transactions = transactionRepository.findBySenderWalletOrReceiverWallet(currentUser.getWallet(), currentUser.getWallet(), pageable);

        return transactions.getContent().stream().map(transaction -> {
            UserDTO.Transactions dto = new UserDTO.Transactions();
            dto.setId(transaction.getId());
            dto.setCreatedAt(transaction.getCreatedAt());
            dto.setAmount(transaction.getAmount());
            dto.setDescription(transaction.getDescription());
            dto.setTransType(transaction.getTransType());

            if (transaction.getTransType().equals(TransType.TRANSFER_IN)){
                dto.setFrom(transaction.getSenderWallet().getUser().getName());
                dto.setTo("Your account");
            }
            else if (transaction.getTransType().equals(TransType.TRANSFER_OUT)){
                dto.setFrom("Your account");
                dto.setTo(transaction.getReceiverWallet().getUser().getName());
            }
            else {
                dto.setFrom("Agent");
                dto.setTo("Your account");
            }

            return dto;
        }).collect(Collectors.toList());
    }

    public Page<TransactionModel> searchForTransactions (UserModel currentUser, int page, int size, String description, TransType transType){
        if (page < 1){
            throw new BadRequestException("Page number can't be less than 1");
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "created_at"));

        Page<TransactionModel> transactionsFound = transactionRepository.findTransactionByDescriptionAndType(description, transType, currentUser.getWallet().getId(), currentUser.getWallet().getId(), pageable);

        return transactionsFound;
    }

    @Transactional(rollbackFor = IOException.class)
    public Map<String, String> UploadKycDocumentToS3 (MultipartFile file, MultipartFile file1, UserModel currentUser, String ip, String device) throws IOException{

            KycModel kyc = kycRepository.findByUserAndKycType(currentUser, KycType.ID);
            KycModel kyc1 = kycRepository.findByUserAndKycType(currentUser, KycType.SELFIE);

            if (!kyc.getStatus().equals(KycStatus.Pending)){
                throw new BadRequestException("Kyc not needed at this time");
            }

            if (!kyc1.getStatus().equals(KycStatus.Pending)){
            throw new BadRequestException("Kyc not needed at this time");
            }

            String key = s3.uploadIdFileToS3(String.valueOf(currentUser.getId()), file);
            String key1 = s3.uploadPictureFileToS3(String.valueOf(currentUser.getId()), file1);

            kyc.setSubmittedAt(ZonedDateTime.now(ZoneId.of("UTC")));
            kyc.setStatus(KycStatus.Under_review);
            kyc.setUrl(key);
            kycRepository.save(kyc);

            kyc1.setSubmittedAt(ZonedDateTime.now(ZoneId.of("UTC")));
            kyc1.setStatus(KycStatus.Under_review);
            kyc1.setUrl(key1);
            kycRepository.save(kyc1);

            Map<String, String> infos = ipUtil.getIpDetails(ip);
            Map<String, String> ua = userAgentUtil.getDeviceInfo(device);

            AuditLogs newLog = new AuditLogs();
            newLog.setUser(currentUser);
            newLog.setAction("User uploaded his kyc document for verification purposes");
            newLog.setCity(infos.get("City"));
            newLog.setCountry(infos.get("Country"));
            newLog.setLongitude(infos.get("Longitude"));
            newLog.setLatitude(infos.get("Latitude"));
            newLog.setOs(ua.get("OS"));
            newLog.setDevice(ua.get("Device"));
            newLog.setBrowser(ua.get("Browser"));

            logRepository.save(newLog);

            Map<String, String> response = new LinkedHashMap<>();

            response.put("notice", "Document successfully uploaded");

            return response;
    }
}
