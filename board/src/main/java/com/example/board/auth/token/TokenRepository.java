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
public interface TokenRepository extends JpaRepository<Token, Long> {
    @Query(value = """
            select t from Token t inner join Member m\s
            on t.member.memberId = m.memberId\s
            where m.memberId = :id and (t.expired = false or t.revoked = false)\s
            """)
    List<Token> findAllValidTokenByUser(@Param("id") Long id);

    Optional<Token> findByToken(String token);

    /**
     * 만료된 토큰을 배치 단위로 삭제
     */
    @Modifying
    @Query(value = "DELETE FROM tokens t WHERE t.expires_at < :currentTime LIMIT :batchSize",
            nativeQuery = true)
    int deleteExpiredTokensInBatch(@Param("currentTime") LocalDateTime currentTime,
                                   @Param("batchSize") int batchSize);

    /**
     * 취소된 토큰을 배치 단위로 삭제
     */
    @Modifying
    @Query(value = "DELETE FROM tokens t WHERE t.revoked = true LIMIT :batchSize",
            nativeQuery = true)
    int deleteRevokedTokensInBatch(@Param("batchSize") int batchSize);

    /**
     * 오래된 만료 토큰을 배치 단위로 삭제
     */
    @Modifying
    @Query(value = "DELETE FROM tokens t WHERE t.expires_at < :cutoffTime LIMIT :batchSize",
            nativeQuery = true)
    int deleteOldExpiredTokensInBatch(@Param("cutoffTime") LocalDateTime cutoffTime,
                                      @Param("batchSize") int batchSize);
}