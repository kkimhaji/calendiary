package com.example.board.controller;

import com.example.board.auth.UserPrincipal;
import com.example.board.domain.member.Member;
import com.example.board.dto.team.*;
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
    public ResponseEntity<TeamCreateResponse> createTeam(@AuthenticationPrincipal UserPrincipal user, @RequestBody TeamCreateRequestDTO dto){
        return ResponseEntity.ok(TeamCreateResponse.fromEntity(teamService.createTeam(user.getMember(), dto)));
    }

    @PostMapping("/addmember")
    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_MEMBERS)")
    public ResponseEntity<?> addMember(@AuthenticationPrincipal UserPrincipal user, @RequestBody AddMemberRequestDTO dto){
        return ResponseEntity.ok(teamService.addMember(dto));
    }

    //팀 삭제 시 role과 teamMember 정보, category&post도 삭제
    @DeleteMapping("/delete/{teamId}")
    @PreAuthorize("hasPermission(#team, 'ADMIN')")
    public void deleteTeam(@PathVariable(name="teamId") Long teamId){
        teamService.deleteTeam(teamId);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamInfoDTO> getTeamInfo(@PathVariable(name="teamId") Long teamId){
        return ResponseEntity.ok(teamService.getTeamInfo(teamId));
    }

    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_TEAM)")
    @PutMapping("/{teamId}")
    public ResponseEntity<Long> updateTeamInfo(@PathVariable(name = "teamId") Long teamId, @RequestBody TeamUpdateRequestDTO dto){
        return ResponseEntity.ok(teamService.updateTeamInfo(teamId, dto));
    }
}
