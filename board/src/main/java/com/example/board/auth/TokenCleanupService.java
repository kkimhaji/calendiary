package com.example.board.auth;

import com.example.board.auth.token.RefreshTokenRepository;
import com.example.board.auth.token.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenCleanupService {
    private final TokenRepository tokenRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${token.cleanup.batch-size:1000}")
    private int batchSize;

    @Value("${token.cleanup.retention-days:7}")
    private int retentionDays;

    /**
     * 만료된 토큰과 취소된 토큰을 일괄 삭제하는 스케줄링 작업
     * 매일 새벽 3시에 실행
     */

    @Scheduled(cron = "${token.cleanup.cron:0 0 3 * * ?}")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("토큰 정리 작업 시작");
        LocalDateTime now = LocalDateTime.now();

        try {
            // 배치 단위로 만료된 토큰 삭제
            int totalDeletedTokens = deleteExpiredTokensInBatches(now);
            log.info("만료된 토큰 {}개 삭제 완료", totalDeletedTokens);

            // 배치 단위로 취소된 토큰 삭제
            int totalDeletedRevokedTokens = deleteRevokedTokensInBatches();
            log.info("취소된 토큰 {}개 삭제 완료", totalDeletedRevokedTokens);

            // 배치 단위로 만료된 리프레시 토큰 삭제
            int totalDeletedRefreshTokens = deleteExpiredRefreshTokensInBatches(now);
            log.info("만료된 리프레시 토큰 {}개 삭제 완료", totalDeletedRefreshTokens);

            // 배치 단위로 취소된 리프레시 토큰 삭제
            int totalDeletedRevokedRefreshTokens = deleteRevokedRefreshTokensInBatches();
            log.info("취소된 리프레시 토큰 {}개 삭제 완료", totalDeletedRevokedRefreshTokens);

            log.info("토큰 정리 작업 완료 - 총 {}개 토큰 삭제",
                    totalDeletedTokens + totalDeletedRevokedTokens +
                            totalDeletedRefreshTokens + totalDeletedRevokedRefreshTokens);
        } catch (Exception e) {
            log.error("토큰 정리 작업 중 오류 발생", e);
        }
    }

    /**
     * 오래된 만료 토큰 정리 (감사 목적으로 일정 기간 보관 후 삭제)
     * 매주 일요일 새벽 1시에 실행
     */
    @Scheduled(cron = "${token.cleanup.old.cron:0 0 1 * * 0}")
    @Transactional
    public void cleanupOldTokens() {
        log.info("오래된 토큰 정리 작업 시작");
        LocalDateTime cutoffTime = LocalDateTime.now().minusDays(retentionDays);

        try {
            // 배치 단위로 오래된 만료 토큰 삭제
            int totalDeletedOldTokens = deleteOldExpiredTokensInBatches(cutoffTime);
            log.info("{}일 이상 지난 만료 토큰 {}개 삭제 완료", retentionDays, totalDeletedOldTokens);

            // 배치 단위로 오래된 만료 리프레시 토큰 삭제
            int totalDeletedOldRefreshTokens = deleteOldExpiredRefreshTokensInBatches(cutoffTime);
            log.info("{}일 이상 지난 만료 리프레시 토큰 {}개 삭제 완료", retentionDays, totalDeletedOldRefreshTokens);

            log.info("오래된 토큰 정리 작업 완료 - 총 {}개 토큰 삭제",
                    totalDeletedOldTokens + totalDeletedOldRefreshTokens);
        } catch (Exception e) {
            log.error("오래된 토큰 정리 작업 중 오류 발생", e);
        }
    }

    private int deleteExpiredTokensInBatches(LocalDateTime currentTime) {
        int totalDeleted = 0;
        int deleted;

        do {
            deleted = tokenRepository.deleteExpiredTokensInBatch(currentTime, batchSize);
            totalDeleted += deleted;
            log.debug("만료된 토큰 배치 삭제: {}개", deleted);
        } while (deleted == batchSize);

        return totalDeleted;
    }

    private int deleteRevokedTokensInBatches() {
        int totalDeleted = 0;
        int deleted;

        do {
            deleted = tokenRepository.deleteRevokedTokensInBatch(batchSize);
            totalDeleted += deleted;
            log.debug("취소된 토큰 배치 삭제: {}개", deleted);
        } while (deleted == batchSize);

        return totalDeleted;
    }

    private int deleteOldExpiredTokensInBatches(LocalDateTime cutoffTime) {
        int totalDeleted = 0;
        int deleted;

        do {
            deleted = tokenRepository.deleteOldExpiredTokensInBatch(cutoffTime, batchSize);
            totalDeleted += deleted;
            log.debug("오래된 만료 토큰 배치 삭제: {}개", deleted);
        } while (deleted == batchSize);

        return totalDeleted;
    }

    private int deleteExpiredRefreshTokensInBatches(LocalDateTime currentTime) {
        int totalDeleted = 0;
        int deleted;

        do {
            deleted = refreshTokenRepository.deleteExpiredRefreshTokensInBatch(currentTime, batchSize);
            totalDeleted += deleted;
            log.debug("만료된 리프레시 토큰 배치 삭제: {}개", deleted);
        } while (deleted == batchSize);

        return totalDeleted;
    }

    private int deleteRevokedRefreshTokensInBatches() {
        int totalDeleted = 0;
        int deleted;

        do {
            deleted = refreshTokenRepository.deleteRevokedRefreshTokensInBatch(batchSize);
            totalDeleted += deleted;
            log.debug("취소된 리프레시 토큰 배치 삭제: {}개", deleted);
        } while (deleted == batchSize);

        return totalDeleted;
    }
    private int deleteOldExpiredRefreshTokensInBatches(LocalDateTime cutoffTime) {
        int totalDeleted = 0;
        int deleted;

        do {
            deleted = refreshTokenRepository.deleteOldExpiredRefreshTokensInBatch(cutoffTime, batchSize);
            totalDeleted += deleted;
            log.debug("오래된 만료 리프레시 토큰 배치 삭제: {}개", deleted);
        } while (deleted == batchSize);

        return totalDeleted;
    }

}
