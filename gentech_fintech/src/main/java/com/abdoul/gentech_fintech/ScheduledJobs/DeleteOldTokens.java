package com.abdoul.gentech_fintech.ScheduledJobs;

import com.abdoul.gentech_fintech.Repositories.RefreshRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class DeleteOldTokens {
    private final RefreshRepository refreshRepository;

    public DeleteOldTokens (RefreshRepository refreshRepository){
        this.refreshRepository = refreshRepository;
    }

    @Scheduled(cron = "0 0 0 */3 * *")
    private void deleteRefreshTokens (){
        refreshRepository.deleteByExpiresAtBefore(ZonedDateTime.now(ZoneId.of("UTC")).minusDays(1));
    }
}
