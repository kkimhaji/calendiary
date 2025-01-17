package com.example.board.controller;

import com.example.board.domain.role.TeamRole;
import com.example.board.dto.role.*;
import com.example.board.permission.TeamPermission;
import com.example.board.service.TeamRoleService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}/roles")
public class TeamRoleController {

    private final TeamRoleService teamRoleService;

    @PostMapping("/manage/create")
    @PreAuthorize("hasPermission(@teamRepository.findById(#teamId).orElse(null), 'MANAGE_ROLES')")
    public ResponseEntity<TeamRoleResponse> createRole(@PathVariable(name="teamId") Long teamId, @RequestBody CreateRoleRequest request){
        TeamRole newRole = teamRoleService.createRole(teamId, request);
        return ResponseEntity.ok(TeamRoleResponse.from(newRole));
    }

    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<Set<TeamPermission>> getRolePermissions(@PathVariable(name="teamId") Long roleId){
        TeamRole role = teamRoleService.getRoleById(roleId);
        return ResponseEntity.ok(role.getPermissionSet());
    }

    @PostMapping("/manage/delete/{roleId}")
    @PreAuthorize("hasPermission(@teamRepository.findById(#teamId).orElse(null), 'MANAGE_ROLES')")
    public void deleteRole(@PathVariable(name="teamId") Long teamId, @PathVariable Long roleId){
        teamRoleService.deleteRole(teamId, roleId);
    }

    //관리자 권한 넘기기
    //역할에 팀 멤버 추가하기
    @PostMapping("/manage/member")
    public ResponseEntity<AddMembersToRoleResponse> addMembersToRole(@PathVariable(name="teamId") Long teamId, @RequestBody AddMembersToRoleRequest request){
        return ResponseEntity.ok(teamRoleService.addMemberToRole(teamId, request));
    }

    @GetMapping("/get")
    public ResponseEntity<List<TeamRoleDetailDto>> getRolesWithCount(@PathVariable(name="teamId") Long teamId){
        return ResponseEntity.ok(teamRoleService.getRolesByTeam(teamId));
    }

    //카테고리 생성 시 권한을 주기 위해
    @GetMapping("/get_roles")
    public ResponseEntity<List<TeamRoleInfoDTO>> getRoles(@PathVariable(name="teamId") Long teamId){
        return ResponseEntity.ok(teamRoleService.getRolesInfo(teamId));
    }

}
