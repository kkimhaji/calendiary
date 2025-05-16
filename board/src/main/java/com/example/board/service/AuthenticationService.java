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
import com.example.board.exception.RefreshTokenExpiredException;
import com.example.board.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class AuthenticationService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CustomUserDetailsService userDetailsService;
    private final CookieUtil cookieUtil;


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

    public AuthenticationResponse verifyUser(VerifyUserDTO dto, HttpServletResponse response) {
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
                var jwtToken = jwtService.generateToken(user, false);
                var refreshToken = jwtService.generateRefreshToken(user);

                saveUserToken(savedUser, jwtToken);
// Refresh Token을 HTTP-Only 쿠키에 저장
                cookieUtil.addRefreshTokenCookie(response, refreshToken, jwtService.getRefreshTokenExpiration());
                return new AuthenticationResponse(
                        jwtToken, refreshToken
                );
            } else throw new RuntimeException("Invalid code");

        } else throw new RuntimeException("User not found");
    }

    public AuthenticationResponse authenticateWithAutoLogin(AuthenticationRequestDTO request, HttpServletResponse response) {
        // 인증 로직은 일반 로그인과 동일
        var member = memberRepository.findByEmail(request.email())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        if (!member.isEnabled()){
            throw new RuntimeException("Account is not verified. Please verify your account first");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(), request.password()
                )
        );
        UserPrincipal user = new UserPrincipal(member);

        // 일반 Access Token 생성
        var jwtToken = jwtService.generateToken(user, true);

        // 자동 로그인용 장기 Refresh Token 생성
        var autoLoginRefreshToken = jwtService.generateAutoLoginRefreshToken(user);

        revokeAllTokens(member);
        saveUserToken(member, jwtToken);
        saveRefreshToken(member, autoLoginRefreshToken, true);

        // 자동 로그인 Refresh Token을 HTTP-Only 쿠키에 저장 (30일 유효)
        cookieUtil.addRefreshTokenCookie(
                response,
                autoLoginRefreshToken,
                jwtService.getAutoLoginExpiration()
        );

        return new AuthenticationResponse(jwtToken, autoLoginRefreshToken);
    }

    public AuthenticationResponse authenticate(AuthenticationRequestDTO request, HttpServletResponse response) {
        // 로그인 유지 여부 확인
        boolean rememberMe = request.rememberMe() != null && request.rememberMe();
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

        var jwtToken = jwtService.generateToken(user, rememberMe);
        //rememberMe 선택 시만 refreshToken 생성
        String refreshToken = null;
        if (rememberMe) {
            refreshToken = jwtService.generateRefreshToken(user);
            revokeToken(member);
            saveRefreshToken(member, refreshToken);
        }

        revokeToken(member);
        saveUserToken(member, jwtToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken, jwtService.getRefreshTokenExpiration());
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
            throw new RefreshTokenExpiredException("Refresh token expired or revoked");
        }

        Member member = storedToken.getMember();
        UserPrincipal user = new UserPrincipal(member);

        // 1. 새로운 토큰 생성
        String newAccessToken = jwtService.generateToken(user, storedToken.isAutoLogin());
        String newRefreshToken = jwtService.generateRefreshToken(user);

        // 2. 기존 토큰 폐기
        storedToken.revoke();
        refreshTokenRepository.save(storedToken);

        // 3. 새로운 토큰 저장
        saveRefreshToken(member, newRefreshToken);
        saveUserToken(member, newAccessToken);

        return new AuthenticationResponse(newAccessToken, newRefreshToken);
    }

    private void saveRefreshToken(Member member, String refreshToken, boolean autoLogin) {
        long expiration = autoLogin ?
                jwtService.getAutoLoginExpiration() :
                jwtService.getRefreshTokenExpiration();

        RefreshToken newToken = new RefreshToken(
                refreshToken,
                member,
                expiration,
                autoLogin
        );
        refreshTokenRepository.save(newToken);
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

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        cookieUtil.deleteRefreshTokenCookie(response);

        try {
            // 2. 쿠키에서 리프레시 토큰 추출
            Optional<String> refreshTokenOpt = cookieUtil.extractRefreshTokenFromCookies(request);

            // 2.1 헤더에서 액세스 토큰도 추출 (선택적)
            String authHeader = request.getHeader("Authorization");
            String accessToken = null;

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                accessToken = authHeader.substring(7);
            }

            // 3. 리프레시 토큰이 있는 경우에만 처리 진행
            if (refreshTokenOpt.isPresent()) {
                String refreshToken = refreshTokenOpt.get();

                // 3.1 토큰 유효성 검증
                if (refreshToken != null && !refreshToken.trim().isEmpty()) {
                    // 3.2 사용자 정보 추출
                    try {
                        String userEmail = jwtService.extractUsername(refreshToken);
                        Member member = memberRepository.findByEmail(userEmail)
                                .orElse(null);

                        if (member != null) {
                            // 3.3 모든 토큰 폐기
                            revokeAllTokens(member);
                        }

                        // 3.4 데이터베이스에서 리프레시 토큰 찾아 폐기
                        refreshTokenRepository.findByToken(refreshToken)
                                .ifPresent(token -> {
                                    token.revoke();
                                    refreshTokenRepository.save(token);
                                });
                    } catch (Exception e) {
                        log.warn("리프레시 토큰 처리 중 오류 발생, 계속 진행: {}", e.getMessage());
                    }
                }
            } else {
                log.info("리프레시 토큰 없음 - 쿠키만 삭제하고 로그아웃 진행");
            }

        } catch (Exception e) {
            log.error("로그아웃 처리 중 예기치 않은 오류 발생", e);
        }

        log.info("로그아웃 처리 완료");
    }


    /**
     * 자동 로그인 처리
     */
    public AuthenticationResponse attemptAutoLogin(HttpServletRequest request, HttpServletResponse response) {
        // 1. 쿠키에서 리프레시 토큰 추출
        return cookieUtil.extractRefreshTokenFromCookies(request)
                .map(refreshToken -> {
                    try {
                        // 2. 토큰 검증
                        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                                .orElseThrow(() -> new RuntimeException("Token not found"));

                        if (storedToken.isRevoked() || storedToken.isExpired() || !storedToken.isAutoLogin()) {
                            throw new RuntimeException("Invalid auto-login token");
                        }

                        // 3. 사용자 정보 추출
                        Member member = storedToken.getMember();
                        UserPrincipal user = new UserPrincipal(member);

                        // 4. 새 Access Token 발급
                        String newAccessToken = jwtService.generateToken(user, true);
                        saveUserToken(member, newAccessToken);

                        return new AuthenticationResponse(newAccessToken, refreshToken);
                    } catch (Exception e) {
                        log.error("Auto login failed", e);
                        cookieUtil.deleteRefreshTokenCookie(response);
                        throw new RuntimeException("Auto login failed", e);
                    }
                })
                .orElseThrow(() -> new RuntimeException("No auto login cookie found"));
    }
}