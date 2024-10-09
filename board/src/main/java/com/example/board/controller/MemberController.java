package com.example.board.controller;

import com.example.board.auth.AuthenticationResponse;
import com.example.board.domain.member.Member;
import com.example.board.dto.member.AuthenticationRequest;
import com.example.board.dto.member.LoginRequestDto;
import com.example.board.dto.member.RegisterRequest;
import com.example.board.dto.member.SignUpRequestDto;
import com.example.board.dto.jwt.TokenDto;
import com.example.board.service.AuthenticationService;
//import com.example.board.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class MemberController {

//    private final MemberService memberService;
    private final AuthenticationService authService;

//    @PutMapping("/change/nickname")
//    public String changeNickName(HttpServletRequest request, @RequestParam String newName){
//        return memberService.changeName(request, newName);
//    }
//
//    @PostMapping("/login")
//    public TokenDto login(@RequestBody LoginRequestDto requestDto){
//        return memberService.login(requestDto);
//    }
//
//    @PostMapping("/join")
//    public Member signup(@RequestBody SignUpRequestDto requestDto){
//        return memberService.signup(requestDto);
//    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody RegisterRequest request){
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody AuthenticationRequest request){
        return ResponseEntity.ok(authService.authenticate(request));
    }

//    @GetMapping("/{id}")
//    public Member getMember(@PathVariable Long id){
//        return memberService.getMemberInfo(id);
//    }
}
