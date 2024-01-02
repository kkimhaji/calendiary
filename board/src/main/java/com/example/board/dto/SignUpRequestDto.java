package com.example.board.dto;

import com.example.board.domain.member.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class SignUpRequestDto {
    private String email;
    private String nickName;
    private String password;

    @Builder
    public SignUpRequestDto(String email, String nickName, String password){
        this.email = email;
        this.nickName = nickName;
        this.password = password;
    }

    public Member toEntity(String pwd, List<String> roles){
        return Member.builder().email(email).nickname(nickName).password(pwd).roles(roles).build();
    }
}
