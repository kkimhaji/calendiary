package com.example.board.service;

import com.example.board.domain.team.CategoryRepository;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamRepository;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.role.TeamRoleRepository;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.domain.teamMember.TeamMemberRepository;
import com.example.board.dto.role.CreateRoleRequest;
import com.example.board.permission.PermissionUtils;
import com.example.board.permission.TeamPermission;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.board.permission.TeamPermission.*;

@Service
@RequiredArgsConstructor
public class TeamRoleService {
    private final TeamRepository teamRepository;
    private final TeamRoleRepository teamRoleRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final CategoryRepository categoryRepository;

    @PreAuthorize("hasPermission(#team, 'MANAGE_ROLES')")
    public TeamRole createRole(Long teamId, CreateRoleRequest request) {

        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        // 역할 이름 중복 검사
        if (teamRoleRepository.existsByTeamAndRoleName(team, request.roleName())) {
            throw new RuntimeException("Role name already exists in this team");
        }

        return teamRoleRepository.save(request.toEntity(team));
    }

    public TeamRole updateRolePermissions(Long roleId, Set<TeamPermission> newPermissions) {
        TeamRole role = teamRoleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        role.setPermissions(newPermissions);
        return teamRoleRepository.save(role);
    }

    public boolean checkPermission(Long roleId, TeamPermission permission) {
        return teamRoleRepository.findById(roleId)
                .map(role -> role.hasPermission(permission))
                .orElse(false);
    }
    public TeamRole getRoleById(Long roleId){
        var role = teamRoleRepository.findById(roleId);
        if (role.isEmpty())
            throw new EntityNotFoundException("That is not proper roleId");
        return role.get();
    }

    public TeamRole createAdmin(Team team){

        Set<TeamPermission> adminPermissions = new HashSet<>(Arrays.asList(
                CREATE_POST, DELETE_POST, MANAGE_ROLES, EDIT_POST, MANAGE_MEMBERS,
                VIEW_POST, CREATE_COMMENT, DELETE_COMMENT
        ));

        CreateRoleRequest request = new CreateRoleRequest("ADMIN", adminPermissions, "who made this team");
        return createRole(team.getId(), request);
    }

    public TeamRole createBasic(Team team){
        return createRole(team.getId(), new CreateRoleRequest("Member", new HashSet<>(List.of(VIEW_POST)), "member of this team"));
    }

    @Transactional
    public void deleteRole(Long teamId, Long roleId){
        //role 삭제 -> 기존 멤버들: default role로 변경
        // TeamMember 수정, Category의 role도 삭제
        TeamRole targetRole = teamRoleRepository.findById(roleId)
                .orElseThrow(()-> new EntityNotFoundException("role not found"));
        Team team = teamRepository.findById(teamId).orElseThrow(()-> new EntityNotFoundException("no such team"));
        Long basicRoleId = team.getBasicRoleId();

        if (roleId.equals(basicRoleId))
            throw new IllegalStateException("기본 역할은 삭제할 수 없습니다.");

        // defaultRole로 변경
        updateMembersRole(team, targetRole);
    }

    private void updateMembersRole(Team team, TeamRole targetRole){
        TeamRole basicRole = teamRoleRepository.findById(team.getBasicRoleId())
                .orElseThrow(() -> new EntityNotFoundException("basic role not found"));

        List<TeamMember> membersWithRole = teamMemberRepository.findAllByTeamAndRole(team, targetRole);

        membersWithRole.forEach(member -> member.setRole(basicRole));
        teamMemberRepository.saveAll(membersWithRole);
    }

    private void deleteCategoryPermissions(Long roleId){
        categoryRepository.deleteAllByRoleId(roleId);
    }
}
