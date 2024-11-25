package com.example.board.controller;

import com.example.board.domain.member.Member;
import com.example.board.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/getprincipal")
    public ResponseEntity<Member> getPrincipal(@AuthenticationPrincipal Member member){
        return ResponseEntity.ok(member);
    }
}
