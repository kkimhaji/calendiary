package com.example.board.auth.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // 회원 ID로 리프레시 토큰 조회
    @Query(value = "SELECT * FROM refresh_token WHERE member_id = :memberId", nativeQuery = true)
    List<RefreshToken> findAllByMemberId(@Param("memberId") Long memberId);
}
