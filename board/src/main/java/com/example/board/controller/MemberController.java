package com.example.board.controller;

import com.example.board.domain.member.Member;
import com.example.board.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/getloginuser")
    public ResponseEntity<Member> getLoginUser(HttpServletRequest request){
        return ResponseEntity.ok(memberService.getMember(request).get());
    }

    @GetMapping("/loginUser")
    public Member getMember(HttpServletRequest request){
        return memberService.getLoginUser(request);
    }
}
