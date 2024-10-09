package com.example.board.service;

import com.example.board.auth.AuthenticationResponse;
import com.example.board.auth.JwtService;
import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import com.example.board.domain.member.Role;
import com.example.board.dto.member.AuthenticationRequest;
import com.example.board.dto.member.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // save to the database and return the generated token
    public AuthenticationResponse register(RegisterRequest request) {
        //create a user object out of the registerRequest
        var user = Member.builder().nickname(request.getNickname()).email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();

        memberRepository.save(user);
        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        //authenticationManager를 통해 검사를 모두 하고, 잘못된 경우 알아서 에러를 내고 끝내기 때문에 아래와 같은 모든 동작을 호출하는 것은 secure하다
        //authenticationManager를 통해서 이메일과 비밀번호가 일치하는지 확인
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()
                )
        );

        var user = memberRepository.findByEmail(request.getEmail())
                .orElseThrow();
        var jwtToken = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .token(jwtToken)
                .build();
    }
}
