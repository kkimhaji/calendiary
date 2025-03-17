package com.example.board.domain.jwt;

import com.example.board.domain.member.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
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
    private boolean expired;
    @Column(nullable = false)
    private boolean autoLogin = false;


    public RefreshToken(String token, Member member, long expirationMs) {
        this.token = token;
        this.member = member;
        this.expiryDate = LocalDateTime.now().plus(expirationMs, ChronoUnit.MILLIS);
        this.revoked = false;
    }

    public RefreshToken(String token, Member member, long expirationInMillis, boolean autoLogin) {
        this(token, member, expirationInMillis);
        this.autoLogin = autoLogin;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }

    public void revoke() {
        this.revoked = true;
    }

    public void setTokenExpired(){
        this.expired = true;
        this.revoked = true;
    }
}
