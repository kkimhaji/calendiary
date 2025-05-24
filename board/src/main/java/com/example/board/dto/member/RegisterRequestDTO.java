package com.example.board.dto.member;

import com.example.board.domain.member.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
public record RegisterRequestDTO(
         String nickname,
         String email,
         String password
) {

    public Member toEntity(String pwd, String verificationCode, LocalDateTime codeExpiredAt, boolean enable){
        return Member.createMember(nickname, email, pwd, enable, verificationCode, codeExpiredAt);
    }
}
