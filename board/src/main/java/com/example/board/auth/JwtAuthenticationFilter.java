package com.example.board.auth;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    //intercept every request and do filter
    //same as doFilter from GenericFilterBean
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            //do filter to next filter
            filterChain.doFilter(request, response);
            return;
        }

        //extract token from header
        jwt = authHeader.substring(7);
        // 3. 빈 토큰이거나 올바른 형식이 아닌 경우 체크
        if (jwt.isEmpty() || !isValidJwtFormat(jwt)) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            // 4. 토큰에서 사용자 이메일 추출
            userEmail = jwtService.extractUsername(jwt);

            // 5. 사용자 이메일이 있고 현재 인증되지 않은 상태인 경우 인증 처리
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (ExpiredJwtException ex) {
            // 6. 토큰 처리 중 예외 발생 시 로그만 남기고 계속 진행
            // 로그인 유지 미선택 시 발생할 수 있는 예외도 여기서 처리됨
            logger.error("JWT 토큰 처리 중 오류 발생: " + ex.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{ \"code\": \"TOKEN_EXPIRED\", \"message\": \"session is expired\" }");
            return;
        }catch (JwtException | IllegalArgumentException ex) {
            // ✅ 2. 기타 JWT 관련 예외 통합 처리
            logger.error("JWT 토큰 오류: " + ex.getMessage());

            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                    "{ \"code\": \"INVALID_TOKEN\", \"message\": \"not valid token\" }"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    // 유효한 JWT 형식인지 확인하는 메서드 (최소한 2개의 마침표가 있어야 함)
    private boolean isValidJwtFormat(String token) {
        return token.split("\\.").length == 3;
    }
}