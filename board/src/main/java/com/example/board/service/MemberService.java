package com.example.board.service;

import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import com.example.board.dto.TokenDto;
import com.example.board.jwt.JwtAuthenticationFilter;
import com.example.board.jwt.JwtTokenProvider;
import com.example.board.jwt.RefreshToken;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.hibernate.boot.model.naming.IllegalIdentifierException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;


@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;


    public Member getMember(HttpServletRequest request){
        String token = tokenProvider.resolveToken(request);
        Authentication authentication = tokenProvider.getAuthentication(token);
        return (Member) authentication.getPrincipal();
    }

    @Transactional
    public TokenDto login(String email, String password){
        Member user = memberRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(password, user.getPassword()))
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
// 1. Login ID/PW 를 기반으로 Authentication 객체 생성
        // 이때 authentication 는 인증 여부를 확인하는 authenticated 값이 false
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);

        // 2. 실제 검증 (사용자 비밀번호 체크)이 이루어지는 부분
        // authenticate 매서드가 실행될 때 CustomUserDetailsService 에서 만든 loadUserByUsername 메서드가 실행
        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        // 3. 인증 정보를 기반으로 JWT 토큰 생성
        TokenDto tokenDto = tokenProvider.createToken(authentication);

        //accessToken, RefreshToken 발급
//        TokenDto tokenDTO = tokenProvider.createToken(user.getEmail(), user.getRoles());

        //RefreshToken 저장
//        RefreshToken refreshToken = RefreshToken.builder()
//                .key(user.getId()).token(tokenDTO.getRefreshToken()).build();
//        refreshTokenRepository.save(refreshToken);
        return tokenDto;
    }

    public String changeName(HttpServletRequest request, String newName){
        Member loginMember = getMember(request);
        if (newName == null ) throw new IllegalArgumentException("바꿀 닉네임을 입력해주세요");
        Member user = Member.builder().memberId(loginMember.getMemberId()).nickname(newName).email(loginMember.getEmail()).password(loginMember.getPassword()).build();
        memberRepository.save(user);
        return newName;
    }
}
