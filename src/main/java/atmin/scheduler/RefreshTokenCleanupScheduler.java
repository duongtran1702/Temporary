package atmin.scheduler;

import atmin.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupScheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Dọn dẹp định kỳ các Refresh Token đã hết hạn sử dụng.
     * Chạy tự động vào lúc 2h sáng mỗi ngày.
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanExpiredTokens() {
        System.out.println("Scheduler: Running cleanup for expired refresh tokens...");
        refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}
