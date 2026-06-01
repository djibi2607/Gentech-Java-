package com.abdoul.gentech_fintech.ScheduledJobs;

import com.abdoul.gentech_fintech.Configuration.KycStatus;
import com.abdoul.gentech_fintech.Models.KycModel;
import com.abdoul.gentech_fintech.Repositories.KycRepository;
import com.abdoul.gentech_fintech.Util.Resend;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Component
public class KycRemainder {
    private final KycRepository kycRepository;
    private final Resend resend;

    public KycRemainder(KycRepository kycRepository, Resend resend){
        this.kycRepository = kycRepository;
        this.resend = resend;
    }

    @Scheduled(cron = "0 0 0 * * *")
    private void sendKycRemainder (){
        List<KycModel> kycs = kycRepository.findKyc(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(10), KycStatus.Pending);
        kycs.forEach(kyc ->{
            resend.sendKycRemainder(kyc.getUser().getName(), "Document upload remainder");
            kyc.setKycRemainder(true);
            kycRepository.save(kyc);
        });

    }
}
