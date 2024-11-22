package com.example.board.dto.member;

import com.example.board.domain.member.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public record RegisterRequestDTO(
         String nickname,
         String email,
         String password
) {

    public Member toEntity(String pwd, String verificationCode, LocalDateTime codeExpiredAt, boolean enable){
        return Member.builder()
                .nickname(this.nickname).email(this.email).password(pwd)
                .verificationCode(verificationCode).verificationCodeExpiredAt(codeExpiredAt)
                .enabled(enable)
                .build();
    }
}
