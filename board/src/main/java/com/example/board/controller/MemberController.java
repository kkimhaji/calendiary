package com.example.board.controller;

import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import com.example.board.dto.SignUpRequestDto;
import com.example.board.dto.TokenDto;
import com.example.board.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class MemberController {

    private final MemberService memberService;

    @PutMapping("/change/nickname")
    public String changeNickName(HttpServletRequest request, @RequestParam String newName){
        return memberService.changeName(request, newName);
    }

    @PostMapping("/login")
    public TokenDto login(@RequestParam String email, @RequestParam String password){
        return memberService.login(email, password);
    }

    @PostMapping("/join")
    public Member signup(@RequestBody SignUpRequestDto requestDto){
        return memberService.signup(requestDto);
    }


}
