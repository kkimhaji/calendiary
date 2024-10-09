package com.example.board.dto.member;

import com.example.board.domain.member.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class SignUpRequestDto {
    private String email;
    private String nickname;
    private String password;

    @Builder
    public SignUpRequestDto(String email, String nickname, String password){
        this.email = email;
        this.nickname = nickname;
        this.password = password;
    }

//    public Member toEntity(String pwd, List<String> roles){
//        return Member.builder().email(email).nickname(nickname).password(pwd).roles(roles).build();
//    }
}
