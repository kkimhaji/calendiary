package com.example.board.support;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestDataFactory {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member createMember(String email, String nickname, String password){
        return memberRepository.save(
                Member.createMember(
                        email, nickname, passwordEncoder.encode(password), true, null, null
                )
        );
    }
}