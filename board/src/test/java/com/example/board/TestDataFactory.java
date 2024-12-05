package com.example.board;

import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import com.example.board.domain.role.TeamRoleRepository;
import com.example.board.domain.team.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestDataFactory {
    private final MemberRepository memberRepository;
    private final TeamRepository teamRepository;
    private final TeamRoleRepository teamRoleRepository;
    private final PasswordEncoder passwordEncoder;

    public Member createMember(String email, String nickname, String password){
        return memberRepository.save(
                Member.builder()
                        .email(email)
                        .nickname(nickname)
                        .password(passwordEncoder.encode(password))
                        .build()
        );
    }
}
