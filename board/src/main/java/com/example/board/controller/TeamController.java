package com.example.board.controller;

import com.example.board.auth.UserPrincipal;
import com.example.board.domain.member.Member;
import com.example.board.dto.PageResponse;
import com.example.board.dto.member.AddTeamMemberToRoleDTO;
import com.example.board.dto.member.TeamMemberInfoListDTO;
import com.example.board.dto.team.*;
import com.example.board.service.MemberService;
import com.example.board.service.TeamMemberService;
import com.example.board.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.naming.ldap.PagedResultsResponseControl;
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

    @GetMapping("/invite/validate")
    public ResponseEntity<InviteValidationResponse> validateInvite(@RequestParam("code") String code){
        return ResponseEntity.ok(teamService.validateInvite(code));
    }

    @PostMapping("/{teamId}/join")
    public ResponseEntity<Void> joinTeam(@PathVariable("teamId") Long teamId, @RequestBody TeamJoinRequest request,
                                         @AuthenticationPrincipal UserPrincipal userPrincipal){
        teamService.joinTeam(teamId, request, userPrincipal.getMember());
        return ResponseEntity.ok().build();
    }
}
