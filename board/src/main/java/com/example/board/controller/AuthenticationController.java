package com.example.board.controller;

import com.example.board.auth.AuthenticationResponse;
import com.example.board.auth.JwtService;
import com.example.board.domain.member.Member;
import com.example.board.dto.member.*;
import com.example.board.service.AuthenticationService;
import com.example.board.service.EmailService;
import com.example.board.service.MemberService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthenticationController {

    private final AuthenticationService authService;
    private final EmailService emailService;
    private final JwtService jwtService;
    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<MemberRegisterResponseDTO> register(@RequestBody RegisterRequestDTO dto){
        return ResponseEntity.ok(authService.register(dto));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDTO dto, HttpServletResponse response){
        try{
            return ResponseEntity.ok(authService.verifyUser(dto, response));
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/resend")
    public ResponseEntity<?> resendVerificationCode(@RequestParam String email){
        try {
            emailService.resendVerificationCode(email);
            return ResponseEntity.ok("Verification code was resent.");
        }catch (RuntimeException e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequestDTO dto, HttpServletResponse response){
        return ResponseEntity.ok(authService.authenticate(dto, response));
    }

    @PostMapping("/reissue")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response, boolean rememberMe) throws IOException {
        authService.refreshToken(request, response);
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader){
        return ResponseEntity.ok(authService.validateToken(authHeader));
    }

    /**
     * 자동 로그인 옵션이 있는 로그인
     */
    @PostMapping("/authenticate/auto-login")
    public ResponseEntity<AuthenticationResponse> authenticateWithAutoLogin(
            @RequestBody AuthenticationRequestDTO request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.authenticateWithAutoLogin(request, response));
    }

    /**
     * 쿠키 기반 리프레시 토큰으로 액세스 토큰 갱신
     */
    @PostMapping("/refresh-token")
    public ResponseEntity<AuthenticationResponse> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        return ResponseEntity.ok(authService.refreshToken(request, response));
    }

    /**
     * 자동 로그인 시도
     */
    @PostMapping("/auto-login")
    public ResponseEntity<AuthenticationResponse> attemptAutoLogin(
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            return ResponseEntity.ok(authService.attemptAutoLogin(request, response));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response) {
        Cookie cookie = new Cookie("refresh_token", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        authService.logout(request, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/get-temp-password")
    public ResponseEntity<Void> issueTempPassword(@RequestBody PasswordResetRequest request){
        memberService.issueTempPassword(request);
        return ResponseEntity.ok().build();
    }
}
