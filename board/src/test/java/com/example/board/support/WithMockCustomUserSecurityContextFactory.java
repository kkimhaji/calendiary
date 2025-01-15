package com.example.board.support;

import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import com.example.board.domain.team.Team;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;

@Component
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Member admin = memberRepository.save(
                Member.builder()
                        .email(customUser.email())
                        .nickname(customUser.nickname())
                        .password(passwordEncoder.encode(customUser.password()))
                        .enabled(true)
                        .build());
        //Authentication 객체 생성
//        UserDetails userDetails = userDetailsService.loadUserByUsername(admin.getEmail());
        Authentication auth = new UsernamePasswordAuthenticationToken(admin.getEmail(), admin.getPassword());

        context.setAuthentication(auth);
        return context;
    }
}
