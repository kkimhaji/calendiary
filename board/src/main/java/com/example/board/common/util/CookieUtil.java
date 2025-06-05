package com.example.board.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@Slf4j
public class CookieUtil {

    @Value("${security.jwt.refresh-token.cookie-name:refresh_token}")
    private String refreshTokenCookieName;

    @Value("${security.jwt.refresh-token.cookie-path:/}")
    private String cookiePath;

    @Value("${security.cookie.secure:true}")
    private boolean secure;

    /**
     * Refresh Token을 HTTP-Only 쿠키에 저장
     */
    public void addRefreshTokenCookie(HttpServletResponse response, String refreshToken, long maxAgeInMilliseconds) {
        int maxAge = (int) (maxAgeInMilliseconds / 1000); // 초 단위로 변환

        Cookie cookie = new Cookie(refreshTokenCookieName, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure); // HTTPS에서만 전송 (개발 환경에서는 false로 설정 가능)
        cookie.setPath(cookiePath);
        cookie.setMaxAge(maxAge);

        response.addCookie(cookie);
        log.debug("Added refresh token cookie with max age: {}", maxAge);
    }

    /**
     * 요청에서 Refresh Token 쿠키 추출
     */
    public Optional<String> extractRefreshTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
                .filter(cookie -> refreshTokenCookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    /**
     * Refresh Token 쿠키 삭제
     */
    public void deleteRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(refreshTokenCookieName, null);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath(cookiePath);
        cookie.setMaxAge(0); // 즉시 만료

        response.addCookie(cookie);
        log.debug("Deleted refresh token cookie");
    }
}
