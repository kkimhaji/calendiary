package com.example.board.controller;

import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamRole;
import com.example.board.dto.role.CreateRoleRequest;
import com.example.board.dto.role.TeamRoleResponse;
import com.example.board.permission.TeamPermission;
import com.example.board.service.TeamRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}/roles")
public class TeamRoleController {

    private final TeamRoleService teamRoleService;

    @PostMapping
    @PreAuthorize("hasPermission(@teamRepository.findById(#teamId).orElse(null), 'MANAGE_ROLES')")
    public ResponseEntity<TeamRoleResponse> createRole(@PathVariable Long teamId, @RequestBody CreateRoleRequest request){
        TeamRole newRole = teamRoleService.createRole(teamId, request);
        return ResponseEntity.ok(TeamRoleResponse.from(newRole));
    }

    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<Set<TeamPermission>> getRolePermissions(@PathVariable Long roleId){
        TeamRole role = teamRoleService.getRoleById(roleId);
        return ResponseEntity.ok(role.getPermissionSet());
    }
}
