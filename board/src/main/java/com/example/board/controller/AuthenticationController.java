package com.example.board.controller;

import com.example.board.auth.AuthenticationResponse;
import com.example.board.auth.JwtService;
import com.example.board.domain.member.Member;
import com.example.board.dto.member.AuthenticationRequestDTO;
import com.example.board.dto.member.MemberRegisterResponseDTO;
import com.example.board.dto.member.RegisterRequestDTO;
import com.example.board.dto.member.VerifyUserDTO;
import com.example.board.service.AuthenticationService;
import com.example.board.service.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

    @PostMapping("/register")
    public ResponseEntity<MemberRegisterResponseDTO> register(@RequestBody RegisterRequestDTO dto){
        return ResponseEntity.ok(authService.register(dto));
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestBody VerifyUserDTO dto){
        try{
            return ResponseEntity.ok(authService.verifyUser(dto));
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
    public ResponseEntity<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequestDTO dto){
        return ResponseEntity.ok(authService.authenticate(dto));
    }

    @PostMapping("/reissue")
    public void refreshToken(HttpServletRequest request, HttpServletResponse response, boolean rememberMe) throws IOException {
        authService.refreshToken(request, response);
    }

    @GetMapping("/validate")
    public ResponseEntity<Boolean> validateToken(@RequestHeader("Authorization") String authHeader){
        return ResponseEntity.ok(authService.validateToken(authHeader));
    }

}
