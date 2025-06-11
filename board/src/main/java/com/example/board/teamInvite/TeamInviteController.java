package com.example.board.teamInvite;

import com.example.board.auth.UserPrincipal;
import com.example.board.teamInvite.dto.InviteCreateRequest;
import com.example.board.teamInvite.dto.InviteResponse;
import com.example.board.teamInvite.dto.TeamJoinRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/team")
public class TeamInviteController {
    private final TeamInviteService teamInviteService;

    @PostMapping("/invite")
    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_MEMBERS)")
    public ResponseEntity<InviteResponse> createInvite(@RequestBody InviteCreateRequest request){
        return ResponseEntity.ok(teamInviteService.createInvite(request));
    }

    @PostMapping("/{teamId}/join")
    public ResponseEntity<Void> joinTeam(@PathVariable("teamId") Long teamId, @RequestBody TeamJoinRequest request,
                                         @AuthenticationPrincipal UserPrincipal userPrincipal){
        teamInviteService.joinTeam(teamId, request, userPrincipal.getMember());
        return ResponseEntity.ok().build();
    }

}
