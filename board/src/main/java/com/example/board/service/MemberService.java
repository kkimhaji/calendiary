package com.example.board.service;

import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import com.example.board.jwt.JwtAuthenticationFilter;
import com.example.board.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider tokenProvider;

    public Member getMember(HttpServletRequest request){
        String token = tokenProvider.resolveToken(request);
        Authentication authentication = tokenProvider.getAuthentication(token);
        return (Member) authentication.getPrincipal();
    }


}
