package com.example.board.teamMember;

import com.example.board.auth.UserPrincipal;
import com.example.board.common.dto.PageResponse;
import com.example.board.member.dto.AddTeamMemberToRoleDTO;
import com.example.board.teamMember.dto.ChangeTeamNicknameRequest;
import com.example.board.teamMember.dto.TeamMemberInfoListDTO;
import com.example.board.teamMember.dto.TeamNicknameCheckResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/team/{teamId}")
public class TeamMemberController {
    private final TeamMemberService teamMemberService;

    //팀 정보에서 멤버 리스트 받아올 때
    @GetMapping("/members")
    public ResponseEntity<List<TeamMemberInfoListDTO>> getTeamMembersList(@PathVariable(name = "teamId") Long teamId) {
        return ResponseEntity.ok(teamMemberService.getTeamMembersWithRole(teamId));
    }

    @GetMapping("/get-members")
    public ResponseEntity<PageResponse<AddTeamMemberToRoleDTO>> getTeamMembersWithSearch(@PathVariable("teamId") Long teamId,
                                                                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                                                                         @RequestParam(value = "size", defaultValue = "10") int size,
                                                                                         @RequestParam(value = "keyword", defaultValue = "") String keyword) {
        Page<AddTeamMemberToRoleDTO> result = teamMemberService.getTeamMembers(teamId, page, size, keyword);

        return ResponseEntity.ok(new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalPages(),
                result.getTotalElements()
        ));
    }

    @PutMapping("/nickname")
    public ResponseEntity<String> updateTeamNickname(@PathVariable("teamId") Long teamId,
                                                     @AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody ChangeTeamNicknameRequest request) {
        return ResponseEntity.ok(teamMemberService.updateTeamNickname(teamId, userPrincipal.getMember(), request.newNickname()));
    }

    @GetMapping("/nickname/check")
    public ResponseEntity<TeamNicknameCheckResponse> checkTeamNicknameDuplicate(
            @PathVariable("teamId") Long teamId,
            @RequestParam("teamNickname") String teamNickname
    ) {
        boolean isDuplicate = teamMemberService.isTeamNicknameDuplicate(teamId, teamNickname);
        return ResponseEntity.ok(TeamNicknameCheckResponse.of(isDuplicate));
    }

}
