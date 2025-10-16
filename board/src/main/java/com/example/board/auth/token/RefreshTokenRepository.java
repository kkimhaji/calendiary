package com.example.board.auth.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // 회원 ID로 리프레시 토큰 조회
    @Query(value = "SELECT * FROM refresh_token WHERE member_id = :memberId", nativeQuery = true)
    List<RefreshToken> findAllByMemberId(@Param("memberId") Long memberId);

    /**
     * 만료된 리프레시 토큰을 배치 단위로 삭제
     */
    @Modifying
    @Query(value = "DELETE FROM refresh_tokens rt WHERE rt.expires_at < :currentTime LIMIT :batchSize",
            nativeQuery = true)
    int deleteExpiredRefreshTokensInBatch(@Param("currentTime") LocalDateTime currentTime,
                                          @Param("batchSize") int batchSize);

    /**
     * 취소된 리프레시 토큰을 배치 단위로 삭제
     */
    @Modifying
    @Query(value = "DELETE FROM refresh_tokens rt WHERE rt.revoked = true LIMIT :batchSize",
            nativeQuery = true)
    int deleteRevokedRefreshTokensInBatch(@Param("batchSize") int batchSize);

    /**
     * 오래된 만료 리프레시 토큰을 배치 단위로 삭제
     */
    @Modifying
    @Query(value = "DELETE FROM refresh_tokens rt WHERE rt.expires_at < :cutoffTime LIMIT :batchSize",
            nativeQuery = true)
    int deleteOldExpiredRefreshTokensInBatch(@Param("cutoffTime") LocalDateTime cutoffTime,
                                             @Param("batchSize") int batchSize);

    // 회원의 모든 리프레시 토큰 삭제
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.member.memberId = :memberId")
    void deleteAllByMemberId(@Param("memberId") Long memberId);

}