package com.example.board.domain.jwt;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    // 회원 ID로 리프레시 토큰 조회
//    Optional<RefreshToken> findByMemberId(Long memberId);
}
