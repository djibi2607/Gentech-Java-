package com.abdoul.gentech_fintech.ScheduledJobs;

import com.abdoul.gentech_fintech.Configuration.TransType;
import com.abdoul.gentech_fintech.Repositories.TransactionRepository;
import com.abdoul.gentech_fintech.Util.Resend;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class DailyReports {
    private final TransactionRepository transactionRepository;
    private final Resend resend;

    public DailyReports(TransactionRepository transactionRepository, Resend resend){
        this.transactionRepository = transactionRepository;
        this.resend = resend;
    }

    @Scheduled(cron = "0 0 0 * * *")
    private void sendReport (){
        ZonedDateTime start = ZonedDateTime.now(ZoneId.of("UTC")).minusDays(1).toLocalDate().atStartOfDay(ZoneId.of("UTC"));

        Long deposit = transactionRepository.countTodayTransactions(TransType.DEPOSIT, start);
        Long withdrawal = transactionRepository.countTodayTransactions(TransType.WITHDRAWAL, start);
        Long transfer = transactionRepository.countTodayTransactions(TransType.TRANSFER_OUT, start);

        BigDecimal amount = transactionRepository.sumTodayAmount(start);

        resend.sendDailyReports(deposit, withdrawal, transfer, amount, "Daily Report");
    }
}
