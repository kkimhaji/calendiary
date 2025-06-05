package com.example.board.auth;

import com.example.board.auth.token.TokenRepository;
import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {

    private final TokenRepository tokenRepository;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer "))
            return;

        String accessToken = authHeader.substring(7);
        tokenRepository.findByToken(accessToken).ifPresent(token -> {
            token.setTokenExpired();
            tokenRepository.save(token);
        });

        try {
            String refreshToken = request.getParameter("refreshToken");
            if (refreshToken != null) {
                tokenRepository.findByToken(refreshToken)
                        .ifPresent(token -> {
                            token.setTokenExpired();
                            tokenRepository.save(token);
                        });
            }
        } catch (IOException e) {
            throw new RuntimeException("Refresh token parsing failed");
        }
    }
}
