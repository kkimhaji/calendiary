package com.example.board.controller;

import com.example.board.domain.member.Member;
import com.example.board.dto.team.AddMemberRequestDTO;
import com.example.board.dto.team.TeamCreateRequestDTO;
import com.example.board.dto.team.TeamCreateResponse;
import com.example.board.dto.team.TeamInfoDTO;
import com.example.board.service.MemberService;
import com.example.board.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(TeamCreateResponse.fromEntity(teamService.createTeam(member, dto)));
    }

    @PostMapping("/addmember")
    @PreAuthorize("hasPermission(#team, 'MANAGE_MEMBERS')")
    public ResponseEntity<?> addMember(@AuthenticationPrincipal Member member, @RequestBody AddMemberRequestDTO dto){
        return ResponseEntity.ok(teamService.addMember(dto));
    }

    //팀 삭제 시 role과 teamMember 정보, category&post도 삭제
    @DeleteMapping("/delete/{teamId}")
    @PreAuthorize("hasPermission(#team, 'ADMIN')")
    public void deleteTeam(@PathVariable Long teamId){
        teamService.deleteTeam(teamId);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamInfoDTO> getTeamInfo(@PathVariable Long teamId){
        return ResponseEntity.ok(teamService.getTeamInfo(teamId));
    }
}
