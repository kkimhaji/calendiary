package com.example.board.service;

import com.example.board.auth.AuthenticationResponse;
import com.example.board.auth.JwtService;
import com.example.board.domain.jwt.Token;
import com.example.board.domain.jwt.TokenRepository;
import com.example.board.domain.jwt.TokenType;
import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import com.example.board.domain.member.Role;
import com.example.board.dto.member.AuthenticationRequest;
import com.example.board.dto.member.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;

    // save to the database and return the generated token
    public AuthenticationResponse register(RegisterRequest request) {
        //create a user object out of the registerRequest
        var user = Member.builder().nickname(request.getNickname()).email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        memberRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);
        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        //authenticationManager를 통해 검사를 모두 하고, 잘못된 경우 알아서 에러를 내고 끝내기 때문에 아래와 같은 모든 동작을 호출하는 것은 secure하다
        //authenticationManager를 통해서 이메일과 비밀번호가 일치하는지 확인
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()
                )
        );

        var user = memberRepository.findByEmail(request.getEmail())
                .orElseThrow();
        var refreshToken = jwtService.generateRefreshToken(user);
        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void saveUserToken(Member member, String jwtToken){
        var token = Token.builder()
                .member(member).token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false).revoked(false)
                .build();

        tokenRepository.save(token);
    }

    private void revokeToken(Member member){
        var validUserTokens = tokenRepository.findAllValidTokenByUser(member.getMemberId());
        if (validUserTokens.isEmpty()) return;

        validUserTokens.forEach(token -> {
            token.setExpired(true);
            token.setRevoked(true);
        });
        tokenRepository.saveAll(validUserTokens);
    }

    //refresh token을 기반으로 새로 access token 발행
    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return;

        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);

        if (userEmail!= null){
            var user = this.memberRepository.findByEmail(userEmail).orElseThrow();

            if (jwtService.isTokenValid(refreshToken, user)){
                var accessToken = jwtService.generateToken(user);
                revokeToken(user);
                saveUserToken(user, accessToken);

                var authResponse = AuthenticationResponse.builder()
                        .accessToken(accessToken).refreshToken(refreshToken)
                        .build();

                new ObjectMapper().writeValue(response.getOutputStream(), authResponse);
            }
        }
    }
}
