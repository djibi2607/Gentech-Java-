package com.abdoul.gentech_fintech.DTO;

import com.abdoul.gentech_fintech.Models.AuditLogs;
import com.abdoul.gentech_fintech.Models.KycModel;
import com.abdoul.gentech_fintech.Models.TransactionModel;
import com.abdoul.gentech_fintech.Models.TwoFactorModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

public class AdminDTO {
    @Getter
    @Setter
    public static class Unflag{
        private Long id;
        private String email;
        private String phone;
    }
    @Getter
    @Setter
    @NoArgsConstructor
    public static class UserCredentials{
        private Long id;
        private String name;
        private String email;
        private String phone;
        private String role;
        private boolean flagged;
        private boolean deleted;
        private BigDecimal balance;
        private ZonedDateTime createdAt;
        private List<TransactionModel> transactionsSent;
        private List<TransactionModel> transactionsReceived;
        private List<KycModel> kycModels;
        private TwoFactorModel twoFactorModel;
        private List<AuditLogs> userLogs;
    }
}
