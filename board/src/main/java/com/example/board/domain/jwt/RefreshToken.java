package com.example.board.domain.jwt;

import com.example.board.domain.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Entity
@NoArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime expiryDate;
    private boolean revoked;

    public RefreshToken(String token, Member member, long expirationMs) {
        this.token = token;
        this.member = member;
        this.expiryDate = LocalDateTime.now().plus(expirationMs, ChronoUnit.MILLIS);
        this.revoked = false;
    }

    public void revoke() {
        this.revoked = true;
    }
}
