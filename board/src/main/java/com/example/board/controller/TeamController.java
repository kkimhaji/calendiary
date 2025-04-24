package com.example.board.controller;

import com.example.board.auth.UserPrincipal;
import com.example.board.domain.team.enums.UserTeamStatus;
import com.example.board.dto.PageResponse;
import com.example.board.dto.member.AddTeamMemberToRoleDTO;
import com.example.board.dto.teamMember.TeamMemberInfoListDTO;
import com.example.board.dto.team.*;
import com.example.board.dto.teamMember.ChangeTeamNicknameRequest;
import com.example.board.service.TeamMemberService;
import com.example.board.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final TeamMemberService teamMemberService;

    @PostMapping("/create")
    public ResponseEntity<TeamCreateResponse> createTeam(@AuthenticationPrincipal UserPrincipal user, @RequestBody TeamCreateRequestDTO dto){
        return ResponseEntity.ok(TeamCreateResponse.fromEntity(teamService.createTeam(user.getMember(), dto)));
    }

    @PostMapping("/addmember")
    public ResponseEntity<?> addMember(@RequestBody AddMemberRequestDTO dto){
        return ResponseEntity.ok(teamService.addMember(dto));
    }

    @DeleteMapping("/delete/{teamId}")
    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_TEAM)")
    public void deleteTeam(@PathVariable(name="teamId") @P("teamId") Long teamId){
        teamService.deleteTeam(teamId);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamInfoPageResponse> getTeamInfo(@PathVariable(name="teamId") Long teamId, @AuthenticationPrincipal UserPrincipal principal,
                                                            @RequestParam(required = false, value = "code") String code){
        TeamInfoPageResponse response = teamService.getTeamInfo(teamId, principal, code);
        // 접근 권한 없는 경우 403 반환
        if (response.userStatus() == UserTeamStatus.NO_ACCESS) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_TEAM)")
    @PutMapping("/{teamId}")
    public ResponseEntity<Long> updateTeamInfo(@PathVariable(name = "teamId") @P("teamId") Long teamId, @RequestBody TeamUpdateRequestDTO dto){
        return ResponseEntity.ok(teamService.updateTeamInfo(teamId, dto));
    }

    //팀 정보에서 멤버 리스트 받아올 때
    @GetMapping("/{teamId}/members")
    public ResponseEntity<List<TeamMemberInfoListDTO>> getTeamMembersList(@PathVariable(name="teamId") Long teamId){
        return ResponseEntity.ok(teamMemberService.getTeamMembersWithRole(teamId));
    }

    @GetMapping("/{teamId}/get-members")
    public ResponseEntity<PageResponse<AddTeamMemberToRoleDTO>> getTeamMembersWithSearch(@PathVariable("teamId") Long teamId,
                                                                                         @RequestParam(value="page", defaultValue = "0") int page,
                                                                                         @RequestParam(value="size", defaultValue = "10") int size,
                                                                                         @RequestParam(value="keyword", defaultValue = "") String keyword){
        Page<AddTeamMemberToRoleDTO> result = teamMemberService.getTeamMembers(teamId, page, size, keyword);

        return ResponseEntity.ok(new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalPages(),
                result.getTotalElements()
        ));
    }

    @PostMapping("/invite")
    public ResponseEntity<InviteResponse> createInvite(@RequestBody InviteCreateRequest request){
        return ResponseEntity.ok(teamService.createInvite(request));
    }

    @PostMapping("/{teamId}/join")
    public ResponseEntity<Void> joinTeam(@PathVariable("teamId") Long teamId, @RequestBody TeamJoinRequest request,
                                         @AuthenticationPrincipal UserPrincipal userPrincipal){
        teamService.joinTeam(teamId, request, userPrincipal.getMember());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{teamId}/nickname")
    public ResponseEntity<?> updateTeamNickname(@PathVariable("teamId") Long teamId,
            @AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody ChangeTeamNicknameRequest request){
        return ResponseEntity.ok(teamMemberService.updateTeamNickname(teamId, userPrincipal.getMember(), request.newNickname()));
    }
}
