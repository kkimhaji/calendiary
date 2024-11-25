package com.example.board.controller;

import com.example.board.domain.member.Member;
import com.example.board.domain.team.Team;
import com.example.board.dto.team.AddMemberRequestDTO;
import com.example.board.dto.team.TeamCreateRequestDTO;
import com.example.board.dto.team.TeamCreateResponse;
import com.example.board.service.MemberService;
import com.example.board.service.TeamService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final MemberService memberService;

    @PostMapping("/create")
    public ResponseEntity<TeamCreateResponse> createTeam(@AuthenticationPrincipal Member member, @RequestBody TeamCreateRequestDTO dto){
//        var loginMember = memberService.getMember(request)
//                .orElseThrow(() ->new IllegalArgumentException("no user"));
        return ResponseEntity.ok(teamService.createTeam(member, dto));
    }

    @PostMapping("/addmember")
    @PreAuthorize("hasPermission(#team, 'MANAGE_MEMBERS')")
    public ResponseEntity<?> addMember(@AuthenticationPrincipal Member member, @RequestBody AddMemberRequestDTO dto){
        return ResponseEntity.ok(teamService.addMember(dto));
    }

    //팀 삭제 시 role과 teamMember 정보도 삭제
}
