package com.example.board.dto.member;

import com.example.board.domain.member.Member;
import lombok.*;

import java.time.LocalDateTime;

public record MemberRegisterResponseDTO(
         String email,
         String nickname,
         String verificationCode,
         boolean enable,
         LocalDateTime verificationCodeExpiredAt
) {
    public static MemberRegisterResponseDTO from(Member member){
        return new MemberRegisterResponseDTO(
                member.getEmail(),
                member.getNickname(),
                member.getVerificationCode(),
                member.isEnabled(),
                member.getVerificationCodeExpiredAt()
        );
    }
}
