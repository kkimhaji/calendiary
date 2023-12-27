package com.example.board.controller;

import com.example.board.domain.member.MemberRepository;
import com.example.board.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class MemberController {

    private final MemberRepository memberRepository;
    private final MemberService memberService;

    @PutMapping("/change/nickname")
    public String changeNickName(HttpServletRequest request, @RequestParam String newName){
        return memberService.changeName(request, newName);
    }

}
