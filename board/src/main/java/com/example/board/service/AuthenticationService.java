package com.example.board.service;

import com.example.board.auth.AuthenticationResponse;
import com.example.board.auth.JwtService;
import com.example.board.auth.UserPrincipal;
import com.example.board.domain.jwt.*;
import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import com.example.board.dto.member.AuthenticationRequestDTO;
import com.example.board.dto.member.MemberRegisterResponseDTO;
import com.example.board.dto.member.RegisterRequestDTO;
import com.example.board.dto.member.VerifyUserDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.nio.file.attribute.UserPrincipalNotFoundException;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomUserDetailsService userDetailsService;


    // save to the database and return the generated token
    public MemberRegisterResponseDTO register(RegisterRequestDTO request) {
        //create a user object out of the registerRequest
        var member = request.toEntity(
                passwordEncoder.encode(request.password()),
                emailService.generateRandomCode(),
                LocalDateTime.now().plusMinutes(15),
                false
        );

        emailService.sendVerificationEmail(member);
        memberRepository.save(member);

        return MemberRegisterResponseDTO.from(member);
    }

    public AuthenticationResponse verifyUser(VerifyUserDTO dto) {
        Optional<Member> optionalMember = memberRepository.findByEmail(dto.email());
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();

            if (member.getVerificationCodeExpiredAt().isBefore(LocalDateTime.now()))
                throw new RuntimeException("Verification code has expired!");

            if (member.getVerificationCode().equals(dto.verificationCode())) {
                member.setVerified();
                memberRepository.save(member);
                UserPrincipal user = new UserPrincipal(member);
                var savedUser = memberRepository.save(member);
                var jwtToken = jwtService.generateToken(user);
                var refreshToken = jwtService.generateRefreshToken(user);

                saveUserToken(savedUser, jwtToken);

                return new AuthenticationResponse(
                        jwtToken, refreshToken
                );
            } else throw new RuntimeException("Invalid code");

        } else throw new RuntimeException("User not found");
    }


    public AuthenticationResponse authenticate(AuthenticationRequestDTO request) {
        //authenticationManager를 통해 검사를 모두 하고, 잘못된 경우 알아서 에러를 내고 끝내기 때문에 아래와 같은 모든 동작을 호출하는 것은 secure하다
        //authenticationManager를 통해서 이메일과 비밀번호가 일치하는지 확인
        var member = memberRepository.findByEmail(request.email())
                .orElseThrow();

        if (!member.isEnabled()){
            throw new RuntimeException("Account is not verified. Please verify your account first");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(), request.password()
                )
        );
        UserPrincipal user = new UserPrincipal(member);

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = jwtService.generateRefreshToken(user);

        revokeToken(member);
        saveUserToken(member, jwtToken);

        return new AuthenticationResponse(jwtToken, refreshToken);
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

        validUserTokens.forEach(Token::setTokenExpired);
        tokenRepository.saveAll(validUserTokens);
    }

    private void revokeRefreshToken(Member member){
        var validRefreshTokens = refreshTokenRepository.findAllByMemberId(member.getMemberId());
        if (validRefreshTokens.isEmpty()) return;

        validRefreshTokens.forEach(RefreshToken::setTokenExpired);
        refreshTokenRepository.saveAll(validRefreshTokens);
    }

    public void revokeAllTokens(Member member){
        revokeToken(member);
        revokeRefreshToken(member);
    }
    //refresh token을 기반으로 새로 access token 발행
    public AuthenticationResponse refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        final String refreshToken;
        final String userEmail;
        if (authHeader == null || !authHeader.startsWith("Bearer ")) throw new RuntimeException("Invalid Header");

        refreshToken = authHeader.substring(7);
        userEmail = jwtService.extractUsername(refreshToken);
        if (userEmail == null) throw new UsernameNotFoundException("이메일에 해당하는 사용자를 찾을 수 없습니다.");

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (storedToken.isRevoked() || storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired or revoked");
        }

        Member member = storedToken.getMember();
        UserPrincipal user = new UserPrincipal(member);

        // 1. 새로운 토큰 생성
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // 2. 기존 토큰 폐기
        storedToken.revoke();
        refreshTokenRepository.save(storedToken);

        // 3. 새로운 토큰 저장
        saveRefreshToken(member, newRefreshToken);
        saveUserToken(member, newAccessToken);

        return new AuthenticationResponse(newAccessToken, newRefreshToken);
    }

    private void saveRefreshToken(Member member, String refreshToken) {
        RefreshToken newToken = new RefreshToken(
                refreshToken,
                member,
                jwtService.getRefreshTokenExpiration()
        );
        refreshTokenRepository.save(newToken);
    }

    public boolean validateToken(String authHeader){
        String token = authHeader.substring(7);
        try{

            String username = jwtService.extractUsername(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (!jwtService.isTokenValid(token, userDetails)){
                throw new MalformedJwtException("token is not valid");
            }

            return true;
//            if (!jwtService.isTokenRevoked())
        } catch (JwtException | UsernameNotFoundException e) {
            throw new RuntimeException();
        }
    }
}
