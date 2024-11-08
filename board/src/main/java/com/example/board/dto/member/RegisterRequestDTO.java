package com.example.board.dto.member;

import com.example.board.domain.member.Member;
import com.example.board.domain.member.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDTO {

    private String nickname;
    private String email;
    private String password;


    public Member toEntity(String pwd, String verificationCode, LocalDateTime codeExpiredAt, boolean enable){
        return Member.builder()
                .nickname(nickname).email(email).password(pwd)
                .verificationCode(verificationCode).verificationCodeExpiredAt(codeExpiredAt)
                .enabled(enable)
                .build();
    }
}
