package com.example.board.domain.member;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;
    @Column(unique = true, nullable = false)
    private String email;
    @Column(nullable = false)
    private String nickname;
    @Column(nullable = false)
    private String password;

    private boolean enabled;
    private String verificationCode;
    private LocalDateTime verificationCodeExpiredAt;

    public void setVerified(){
        this.enabled = true;
        this.verificationCode = null;
        this.verificationCodeExpiredAt = null;
    }

    public void setVerification(String code, LocalDateTime expiredAt){
        this.verificationCode = code;
        this.verificationCodeExpiredAt = expiredAt;
    }

    public void updateName(String newNickname) {
        this.nickname = newNickname;
    }

    public void updatePassword(String newPassword)
    {
        this.password = newPassword;
    }

    //builder대신 다른 메서드로 대체할 것
    @Builder
    public Member(Long memberId, String email, String nickname, String password, boolean enabled, String verificationCode, LocalDateTime verificationCodeExpiredAt){
        this.memberId = memberId;
        this.email = email;
        this.nickname = nickname;
        this.password = password;
        this.enabled = enabled;
        this.verificationCode = verificationCode;
        this.verificationCodeExpiredAt = verificationCodeExpiredAt;
    }
}
