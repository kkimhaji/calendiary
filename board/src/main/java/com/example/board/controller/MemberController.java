package com.example.board.controller;

import com.example.board.domain.member.Member;
import com.example.board.domain.teamMember.TeamMemberRepository;
import com.example.board.dto.team.TeamListDTO;
import com.example.board.service.MemberService;
import com.example.board.service.TeamMemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final TeamMemberService teamMemberService;

    @GetMapping("/getprincipal")
    public ResponseEntity<Member> getPrincipal(@AuthenticationPrincipal Member member){
        return ResponseEntity.ok(member);
    }

    @GetMapping("/get_teams")
    public ResponseEntity<List<TeamListDTO>> getTeams(@AuthenticationPrincipal Member member){
        return ResponseEntity.ok(teamMemberService.getTeams(member));
    }

}
