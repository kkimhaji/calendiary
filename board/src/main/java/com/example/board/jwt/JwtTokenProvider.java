package com.example.board.jwt;

import com.example.board.dto.TokenDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtTokenProvider {
    private final String secret;
    private final long tokenValidateTime;
    private static final String AUTORITIES_KEY = "auth";
    private String ROLES = "roles";
    private final UserDetailsService userDetailsService;
    private Key key;


    public JwtTokenProvider(@Value("${custom.jwt.key}") String secretKey,
                            @Value("${custom.jwt.token-validate-time}") long tokenValidateTime, UserDetailsService userDetailsService) {
        this.secret = Encoders.BASE64URL.encode(secretKey.getBytes());
        this.userDetailsService = userDetailsService;
        this.tokenValidateTime = tokenValidateTime * 1000;
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
//        secretKey = Base64UrlCodec.BASE64URL.encode(secretKey.getBytes());

    }

    public TokenDto createToken(Long userPK, List<String> roles){
        Claims claims = Jwts.claims().setSubject(String.valueOf(userPK));
        claims.put(ROLES, roles);

        //권한 가져오기
//        String authorities = authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority)
//                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity = new Date(now + this.tokenValidateTime);

        String accessToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setClaims(claims).setIssuedAt(new Date())
                .setExpiration(validity)
                .signWith(key)
                .compact();

        String refreshToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setExpiration(validity)
                .signWith(key)
                .compact();


//        String accessToken = Jwts.builder()
//                .setSubject(authentication.getName())
////                .claim(AUTORITIES_KEY, authorities)
//                .setClaims(claims).setIssuedAt(now)
//                .setExpiration(validity)
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
//
//        String refreshToken = Jwts.builder()
//                .setExpiration(validity)
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();

        return TokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpired(tokenValidateTime)
                .build();
    }

    // JWT 토큰을 복호화하여 토큰에 들어있는 정보를 꺼내는 메서드
    public Authentication getAuthentication(String accessToken){
        //토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // UserDetails 객체를 만들어서 Authentication 리턴
        UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUserPK(accessToken));

        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public String getUserPK(String token){
        return (Jwts.parserBuilder().setSigningKey(secret.getBytes())).build().parseClaimsJws(token).getBody().getSubject();
    }

    // 토큰 정보를 검증하는 메서드
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(secret.getBytes()).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new IllegalArgumentException("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            throw new IllegalArgumentException("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            throw new IllegalArgumentException("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("JWT claims string is empty.", e);
        }
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(secret.getBytes()).build().parseClaimsJws(accessToken).getBody();
        }catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    // Request Header 에서 토큰 정보 추출
    public String resolveToken(HttpServletRequest request) {
        return request.getHeader("X-AUTH-TOKEN");
    }
}
