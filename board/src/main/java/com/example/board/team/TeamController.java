package com.example.board.team;

import com.example.board.auth.UserPrincipal;
import com.example.board.team.dto.*;
import com.example.board.team.enums.UserTeamStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService teamService;

    @PostMapping("/create")
    public ResponseEntity<TeamCreateResponse> createTeam(@AuthenticationPrincipal UserPrincipal user, @RequestBody TeamCreateRequestDTO dto) {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(TeamCreateResponse.fromEntity(teamService.createTeam(user.getMember(), dto)));
    }

    @DeleteMapping("/delete/{teamId}")
    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_TEAM)")
    public void deleteTeam(@PathVariable(name = "teamId") @P("teamId") Long teamId) {
        teamService.deleteTeam(teamId);
    }

    @GetMapping("/{teamId}")
    public ResponseEntity<TeamInfoPageResponse> getTeamInfo(@PathVariable(name = "teamId") Long teamId, @AuthenticationPrincipal UserPrincipal principal,
                                                            @RequestParam(required = false, value = "code") String code) {
        TeamInfoPageResponse response = teamService.getTeamInfo(teamId, principal, code);
        // 접근 권한 없는 경우 403 반환
        if (response.userStatus() == UserTeamStatus.NO_ACCESS) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_TEAM)")
    @PutMapping("/{teamId}")
    public ResponseEntity<Long> updateTeamInfo(@PathVariable(name = "teamId") @P("teamId") Long teamId, @RequestBody TeamUpdateRequestDTO dto) {
        return ResponseEntity.ok(teamService.updateTeamInfo(teamId, dto));
    }
}
