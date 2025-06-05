package com.example.board.member.dto;

import com.example.board.member.Member;

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
