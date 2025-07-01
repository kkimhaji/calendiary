package com.example.board.role;

import com.example.board.auth.UserPrincipal;
import com.example.board.common.dto.PageResponse;
import com.example.board.teamMember.dto.TeamMemberOfRoleDTO;
import com.example.board.permission.TeamPermission;
import com.example.board.role.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}/roles")
public class TeamRoleController {

    private final TeamRoleService teamRoleService;

    @PostMapping("/manage/create")
    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_ROLES)")
    public ResponseEntity<TeamRoleResponse> createRole(@PathVariable(name="teamId") @P("teamId") Long teamId, @RequestBody CreateRoleRequest request){
        TeamRole newRole = teamRoleService.createRole(teamId, request);
        return ResponseEntity.ok(TeamRoleResponse.from(newRole));
    }

    @GetMapping("/{roleId}/permissions")
    public ResponseEntity<Set<TeamPermission>> getRolePermissions(@PathVariable(name="roleId") Long roleId){
        TeamRole role = teamRoleService.getRoleById(roleId);
        return ResponseEntity.ok(role.getPermissionSet());
    }

    @DeleteMapping("/manage/delete/{roleId}")
    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_ROLES)")
    public ResponseEntity<Void> deleteRole(@PathVariable(name="teamId") @P("teamId") Long teamId, @PathVariable("roleId") Long roleId){
        teamRoleService.deleteRole(teamId, roleId);
        return ResponseEntity.ok().build();
    }

    //관리자 권한 넘기기
    //역할에 팀 멤버 추가하기
    @PostMapping("/manage/member")
    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_ROLES)")
    public ResponseEntity<AddMembersToRoleResponse> addMembersToRole(@PathVariable(name="teamId") @P("teamId") Long teamId, @RequestBody AddMembersToRoleRequest request){
        return ResponseEntity.ok(teamRoleService.addMemberToRole(teamId, request));
    }

    // 팀 내의 역할들 가져오기
    // 팀에서 역할, 멤버 등 수정할 때
    @GetMapping("/get")
    public ResponseEntity<List<TeamRoleDetailResponse>> getRolesWithCount(@PathVariable(name="teamId") Long teamId){
        return ResponseEntity.ok(teamRoleService.getRolesByTeam(teamId));
    }

    //카테고리 생성 시 권한을 주기 위해
    @GetMapping("/get_roles")
    public ResponseEntity<List<TeamRoleInfoDTO>> getRoles(@PathVariable(name="teamId") Long teamId){
        return ResponseEntity.ok(teamRoleService.getRolesInfo(teamId));
    }

    //현재 로그인한 사용자의 팀 내 역할 조회
    @GetMapping("/getrole")
    public ResponseEntity<TeamRoleResponse> getMembersRole(@PathVariable(name = "teamId") Long teamId, @AuthenticationPrincipal UserPrincipal user){
        return ResponseEntity.ok(teamRoleService.getMembersRole(teamId, user.getMember()));
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<TeamRoleResponse> getRolesDetailsWithMembers(@PathVariable(name="teamId") Long teamId, @PathVariable(name="roleId") Long roleId){
        return ResponseEntity.ok(teamRoleService.getRoleDetails(teamId, roleId));
    }

    @PutMapping("/{roleId}/update")
    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_ROLES)")
    public void updateRoleInfo(@PathVariable(name="teamId") @P("teamId") Long teamId, @PathVariable(name = "roleId") Long roleId, @RequestBody RoleUpdateRequest request){
        teamRoleService.updateRole(teamId, roleId, request);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<CategoryRolePermissionDTO>> getRolesWithPermissions(
            @PathVariable(name="teamId") Long teamId,
            @PathVariable(name = "categoryId") Long categoryId
    ) {
        return ResponseEntity.ok(teamRoleService.getRolesWithPermissions(teamId, categoryId));
    }

    @GetMapping("/{roleId}/members")
    public ResponseEntity<PageResponse<TeamMemberOfRoleDTO>> getRoleMembers(
            @PathVariable("teamId") Long teamId,
            @PathVariable("roleId") Long roleId,
            @RequestParam(value= "page",defaultValue = "0") int page,
            @RequestParam(value = "size",defaultValue = "10") int size,
            @RequestParam(value="keyword", defaultValue = "") String keyword
    ) {
        Page<TeamMemberOfRoleDTO> result = teamRoleService.getRoleMembers(teamId, roleId, page, size, keyword);
        return ResponseEntity.ok(PageResponse.from(result));
    }
}