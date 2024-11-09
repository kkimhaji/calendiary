package com.example.board.dto.member;

import com.example.board.domain.member.Member;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class MemberRegisterResponseDTO {
    private String email;
    private String nickname;
    private String verificationCode;
    private boolean enable;
    private LocalDateTime verificationCodeExpiredAt;

    public MemberRegisterResponseDTO(Member member){
        email = member.getEmail();
        nickname = member.getNickname();
        verificationCode = member.getVerificationCode();
        enable = member.isEnabled();
        verificationCodeExpiredAt = member.getVerificationCodeExpiredAt();
    }
}
