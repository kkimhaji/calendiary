//package com.example.board.config;
//
//import com.example.board.auth.JwtAuthenticationFilter;
//import com.example.board.auth.JwtTokenProvider;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.factory.PasswordEncoderFactories;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
//import org.springframework.web.servlet.config.annotation.CorsRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//import static org.springframework.security.config.Customizer.withDefaults;
//
//@RequiredArgsConstructor
//@Configuration
//@EnableMethodSecurity(securedEnabled = true)
//public class SecurityConfig implements WebMvcConfigurer{
//    private final JwtTokenProvider jwtTokenProvider;
//    @Bean
//    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .httpBasic(HttpBasicConfigurer::disable)
//                .csrf(AbstractHttpConfigurer::disable)
//                .cors(withDefaults())
//                .formLogin(AbstractHttpConfigurer::disable)
//                .sessionManagement(sessionManagementConfigurer -> sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
//                .authorizeHttpRequests(authorize -> authorize
//                                .requestMatchers("/user/login", "/user/join").permitAll()
//                                .anyRequest().authenticated()
////                                .requestMatchers("/db/**").access(new WebExpressionAuthorizationManager("hasRole('ADMIN') and hasRole('DBA')"))
////                        // .requestMatchers("/db/**").access(AuthorizationManagers.allOf(AuthorityAuthorizationManager.hasRole("ADMIN"), AuthorityAuthorizationManager.hasRole("DBA")))
//                )
//                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }
//
//    @Override
//    public void addCorsMappings(CorsRegistry registry) {
//        registry.addMapping("/**")
//                .allowedOrigins("http://localhost:8080")
//                .allowedMethods("*")
//                .allowedHeaders("*");
//        WebMvcConfigurer.super.addCorsMappings(registry);
//    }
////
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
//    }
//
//}
