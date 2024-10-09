//package com.example.board.service;
//
//import com.example.board.domain.jwt.RefreshTokenRepository;
//import com.example.board.domain.member.Member;
//import com.example.board.domain.member.MemberRepository;
//import com.example.board.dto.member.LoginRequestDto;
//import com.example.board.dto.member.SignUpRequestDto;
//import com.example.board.dto.jwt.TokenDto;
//import com.example.board.dto.jwt.TokenRequestDto;
//import com.example.board.auth.JwtTokenProvider;
//import com.example.board.domain.jwt.RefreshToken;
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.Collections;
//
//
//@Service
//@Transactional(readOnly = true)
//@RequiredArgsConstructor
//public class MemberService {
//
//
//    private final MemberRepository memberRepository;
//    private final JwtTokenProvider tokenProvider;
//    private final PasswordEncoder passwordEncoder;
//    private final AuthenticationManagerBuilder authenticationManagerBuilder;
//    private final RefreshTokenRepository refreshTokenRepository;
//
//
//    public Member getMember(HttpServletRequest request){
//        String token = tokenProvider.resolveToken(request);
//        Authentication authentication = tokenProvider.getAuthentication(token);
//        return (Member) authentication.getPrincipal();
//    }
//
//    @Transactional(readOnly = false)
//    public TokenDto login(LoginRequestDto requestDto){
//        String email = requestDto.getEmail();
//        String password = requestDto.getPassword();
//        Member user = memberRepository.findByEmail(email)
//                .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));
//        if (!passwordEncoder.matches(password, user.getPassword()))
//            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
//
//        //accessToken, RefreshToken 발급
//        TokenDto tokenDTO = tokenProvider.createToken(user.getMemberId(), user.getRoles());
//
//        //RefreshToken 저장
//        RefreshToken refreshToken = RefreshToken.builder()
//                .tokenKey(user.getMemberId()).token(tokenDTO.getRefreshToken()).build();
//        refreshTokenRepository.save(refreshToken);
//
//        // 3. 인증 정보를 기반으로 JWT 토큰 생성
//        return tokenDTO;
//    }
//
//    @Transactional
//    public TokenDto login(String email, String password){
//        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);
//        AuthenticationManager authenticationManager = authenticationManagerBuilder.getObject();
//
//        Authentication authentication = authenticationManager.authenticate(authenticationToken);
//        return tokenProvider.generateToken(authentication);
//    }
//
//    public String changeName(HttpServletRequest request, String newName){
//        Member loginMember = getMember(request);
//        if (newName == null ) throw new IllegalArgumentException("바꿀 닉네임을 입력해주세요");
//        Member user = Member.builder().memberId(loginMember.getMemberId()).nickname(newName).email(loginMember.getEmail()).password(loginMember.getPassword()).build();
//        memberRepository.save(user);
//        return newName;
//    }
//
//    @Transactional(readOnly = false)
//    public Member signup(SignUpRequestDto requestDto){
//        if(memberRepository.findByEmail(requestDto.getEmail()).isPresent()) throw new RuntimeException("이미 존재하는 계정입니다.");
//        return memberRepository.save(requestDto.toEntity(passwordEncoder.encode(requestDto.getPassword()), Collections.singletonList("ROLE_USER")));
//
//    }
//
//    public TokenDto reissue(TokenRequestDto tokenRequestDTO){ //access, refresh token 재발급
//        //만료된 refresh token 에러
//        if (!tokenProvider.validateToken(tokenRequestDTO.getRefreshToken())){
//            throw new IllegalArgumentException("토큰이 만료됐습니다.");
//        }
//
//        //accessToken에서 username(pk) 가져오기
//        String accessToken = tokenRequestDTO.getAccessToken();
//        Authentication authentication = tokenProvider.getAuthentication(accessToken);
//
//        //user pk로 user 검색 - repos에 저장된 refresh token이 없음?
//        Member user = (Member) authentication.getPrincipal();
//
//        RefreshToken refreshToken = refreshTokenRepository.findByTokenKey(user.getMemberId()).orElseThrow(()->new IllegalArgumentException("토큰이 유효하지 않습니다."));
//
//        //access, refresh token 재발급 + refresh token save
//        TokenDto newCreatedToken = tokenProvider.createToken(user.getMemberId(), user.getRoles());
//
//
//        RefreshToken updateRefreshToken = refreshToken.builder().token(newCreatedToken.getRefreshToken()).tokenKey(user.getMemberId()).rTokenId(refreshToken.getRTokenId()).build();
//        refreshTokenRepository.save(updateRefreshToken);
//        return newCreatedToken;
//    }
//
//    public Member getMemberInfo(Long memberId){
//        if (memberRepository.findById(memberId).isEmpty()){
//            throw new RuntimeException("사용자가 존재하지 않습니다.");
//        }
//        return memberRepository.findById(memberId).get();
//    }
//}
