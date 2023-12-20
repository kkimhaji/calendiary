package com.example.board.config;

import com.example.board.jwt.JwtAuthenticationFilter;
import com.example.board.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig{
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * httpBasic().disable().csrf().disable(): rest api이므로 basic auth 및 csrf 보안을 사용하지 않는다는 설정
     *  sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS): JWT를 사용하기 때문에 세션을 사용하지 않는다는 설정
     *  antMatchers().permitAll(): 해당 API에 대해서는 모든 요청을 허가한다는 설정
     *  antMatchers().hasRole("USER"): USER 권한이 있어야 요청할 수 있다는 설정
     *  anyRequest().authenticated(): 이 밖에 모든 요청에 대해서 인증을 필요로 한다는 설정
     *  addFilterBefore(new JwtAUthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class): JWT 인증을 위하여 직접 구현한 필터를 UsernamePasswordAuthenticationFilter 전에 실행하겠다는 설정
     *  passwordEncoder: JWT를 사용하기 위해서는 기본적으로 password encoder가 필요한데, 여기서는 Bycrypt encoder를 사용
     */

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sessionManagementConfigurer -> sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                                .requestMatchers(HttpMethod.valueOf("/members/login")).permitAll()
                                .requestMatchers(HttpMethod.valueOf("/members/test")).hasRole("USER")
                                .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
