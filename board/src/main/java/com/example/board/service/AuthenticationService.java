package com.example.board.service;

import com.example.board.auth.AuthenticationResponse;
import com.example.board.auth.JwtService;
import com.example.board.domain.jwt.Token;
import com.example.board.domain.jwt.TokenRepository;
import com.example.board.domain.jwt.TokenType;
import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import com.example.board.dto.member.AuthenticationRequestDTO;
import com.example.board.dto.member.RegisterRequestDTO;
import com.example.board.dto.member.VerifyUserDTO;
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
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;

    // save to the database and return the generated token
    public Member register(RegisterRequestDTO request) {
        //create a user object out of the registerRequest
        var user = request.toEntity(
                passwordEncoder.encode(request.getPassword()),
                emailService.generateVerificationCode(),
                LocalDateTime.now().plusMinutes(15),
                false
        );

        emailService.sendVerificationEmail(user);

        return memberRepository.save(user);
    }

    public AuthenticationResponse verifyUser(VerifyUserDTO dto) {
        Optional<Member> optionalMember = memberRepository.findByEmail(dto.getEmail());
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();

            if (member.getVerificationCodeExpiredAt().isBefore(LocalDateTime.now()))
                throw new RuntimeException("Verification code has expired!");

            if (member.getVerificationCode().equals(dto.getVerificationCode())) {
                member.setVerified();
                memberRepository.save(member);

                var savedUser = memberRepository.save(member);
                var jwtToken = jwtService.generateToken(member);
                var refreshToken = jwtService.generateRefreshToken(member);

                saveUserToken(savedUser, jwtToken);

                return AuthenticationResponse.builder()
                        .accessToken(jwtToken)
                        .refreshToken(refreshToken)
                        .build();
            } else throw new RuntimeException("Invalid code");

        } else throw new RuntimeException("User not found");
    }


    public AuthenticationResponse authenticate(AuthenticationRequestDTO request) {
        //authenticationManager를 통해 검사를 모두 하고, 잘못된 경우 알아서 에러를 내고 끝내기 때문에 아래와 같은 모든 동작을 호출하는 것은 secure하다
        //authenticationManager를 통해서 이메일과 비밀번호가 일치하는지 확인
        var user = memberRepository.findByEmail(request.getEmail())
                .orElseThrow();

        if (!user.isEnabled()){
            throw new RuntimeException("Account is not verified. Please verify your account first");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()
                )
        );

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeToken(user);
        saveUserToken(user, jwtToken);

        return AuthenticationResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private void saveUserToken(Member member, String jwtToken) {
        var token = Token.builder()
                .member(member).token(jwtToken)
                .tokenType(TokenType.BEARER)
                .expired(false).revoked(false)
                .build();

        tokenRepository.save(token);
    }

    private void revokeToken(Member member) {
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

        if (userEmail != null) {
            var user = this.memberRepository.findByEmail(userEmail).orElseThrow();

            if (jwtService.isTokenValid(refreshToken, user)) {
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
