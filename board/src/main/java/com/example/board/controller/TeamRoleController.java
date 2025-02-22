package com.example.board.controller;

import com.example.board.auth.UserPrincipal;
import com.example.board.domain.role.TeamRole;
import com.example.board.dto.role.*;
import com.example.board.permission.CategoryPermission;
import com.example.board.permission.TeamPermission;
import com.example.board.service.TeamMemberService;
import com.example.board.service.TeamRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
public class TeamRoleController {

    private final TeamRoleService teamRoleService;
    private final TeamMemberService teamMemberService;

    @PostMapping("teams/{teamId}/manage/create")
    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_ROLES)")
    public ResponseEntity<TeamRoleResponse> createRole(@PathVariable(name="teamId") Long teamId, @RequestBody CreateRoleRequest request){
        TeamRole newRole = teamRoleService.createRole(teamId, request);
        return ResponseEntity.ok(TeamRoleResponse.from(newRole));
    }

    @GetMapping("teams/{teamId}/{roleId}/permissions")
    public ResponseEntity<Set<TeamPermission>> getRolePermissions(@PathVariable(name="roleId") Long roleId){
        TeamRole role = teamRoleService.getRoleById(roleId);
        return ResponseEntity.ok(role.getPermissionSet());
    }

    @PostMapping("teams/{teamId}/manage/delete/{roleId}")
    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_ROLES)")
    public void deleteRole(@PathVariable(name="teamId") Long teamId, @PathVariable Long roleId){
        teamRoleService.deleteRole(teamId, roleId);
    }

    //관리자 권한 넘기기
    //역할에 팀 멤버 추가하기
    @PostMapping("teams/{teamId}/manage/member")
    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_ROLES)")
    public ResponseEntity<AddMembersToRoleResponse> addMembersToRole(@PathVariable(name="teamId") Long teamId, @RequestBody AddMembersToRoleRequest request){
        return ResponseEntity.ok(teamRoleService.addMemberToRole(teamId, request));
    }

    // 팀 내의 역할들 가져오기
    // 팀에서 역할, 멤버 등 수정할 때
    @GetMapping("teams/{teamId}/get")
    public ResponseEntity<List<TeamRoleDetailDto>> getRolesWithCount(@PathVariable(name="teamId") Long teamId){
        return ResponseEntity.ok(teamRoleService.getRolesByTeam(teamId));
    }

    //카테고리 생성 시 권한을 주기 위해
    @GetMapping("teams/{teamId}/get_roles")
    public ResponseEntity<List<TeamRoleInfoDTO>> getRoles(@PathVariable(name="teamId") Long teamId){
        return ResponseEntity.ok(teamRoleService.getRolesInfo(teamId));
    }

    //현재 로그인한 사용자의 팀 내 역할 조회
    @GetMapping("teams/{teamId}/getrole")
    public ResponseEntity<TeamRoleResponse> getMembersRole(@PathVariable(name = "teamId") Long teamId, @AuthenticationPrincipal UserPrincipal user){
        return ResponseEntity.ok(teamRoleService.getMembersRole(teamId, user.getMember()));
    }

    @GetMapping("/post-edit-delete/check")
    public ResponseEntity<EditAndDeletePermissionResponse> checkPostPermission(@RequestParam(name="postId") Long postId){
        return ResponseEntity.ok(teamRoleService.checkEditAndDeletePostPermission(postId));
    }

    @GetMapping("/post-create/check")
    public ResponseEntity<Boolean> checkCreatePermission(@RequestParam(name="categoryId") Long categoryId){
        return ResponseEntity.ok(teamRoleService.checkCreatePostPermission(categoryId));
    }

    @GetMapping("/comment-edit-delete/check")
    public ResponseEntity<EditAndDeletePermissionResponse> checkCommentPermission(@RequestParam(name="commentId") Long commentId){
        return ResponseEntity.ok(teamRoleService.checkEditAndDeleteCommentPermission(commentId));
    }
}