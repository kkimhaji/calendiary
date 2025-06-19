package com.example.board.support;

import com.example.board.category.CategoryService;
import com.example.board.category.TeamCategory;
import com.example.board.category.dto.CategoryRolePermissionDTO;
import com.example.board.category.dto.CreateCategoryRequest;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.permission.TeamPermission;
import com.example.board.role.TeamRole;
import com.example.board.role.TeamRoleService;
import com.example.board.role.dto.AddMembersToRoleRequest;
import com.example.board.role.dto.CreateRoleRequest;
import com.example.board.team.Team;
import com.example.board.team.TeamService;
import com.example.board.team.dto.AddMemberRequestDTO;
import com.example.board.team.dto.TeamCreateRequestDTO;
import com.example.board.teamMember.TeamMember;
import com.example.board.teamMember.TeamMemberRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.example.board.permission.CategoryPermission.*;
import static com.example.board.permission.TeamPermission.MANAGE_MEMBERS;
import static com.example.board.permission.TeamPermission.MANAGE_ROLES;

@Component
@RequiredArgsConstructor
@Transactional
public class TestDataBuilder {
    private final TeamService teamService;
    private final TeamRoleService teamRoleService;
    private final CategoryService categoryService;
    private final TeamMemberRepository teamMemberRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public Member createMember(String email, String nickname, String password) {
        return memberRepository.save(
                Member.createMember(
                        email, nickname, passwordEncoder.encode(password), true, null, null
                ));
    }

    public Team createTeam(Member member1) {
        var request = new TeamCreateRequestDTO("testTeam", "test");
        return teamService.createTeam(member1, request);
    }

    public TeamMember addMemberToTeam(Member member2, Team team) {
        AddMemberRequestDTO dto = new AddMemberRequestDTO(team.getId(), member2.getMemberId());
        return teamService.addMember(dto);
    }

    public TeamRole createNewRole(Team team, String roleName) {
        Set<TeamPermission> permissions = new HashSet<>(Arrays.asList(
                MANAGE_ROLES, MANAGE_MEMBERS
        ));
        var roleRequest = new CreateRoleRequest(roleName, permissions, "role for test");
        return teamRoleService.createRole(team.getId(), roleRequest);
    }

    public TeamRole createNewRoleWithPermissions(Team team, String roleName, Set<TeamPermission> permissions){
        return teamRoleService.createRole(team.getId(), new CreateRoleRequest(roleName, permissions, "new role with permissions"));
    }

    public void addMemberToRole(Member member, TeamRole teamRole) {
        var addRequest = new AddMembersToRoleRequest(teamRole.getId(), Collections.singletonList(member.getMemberId()));
        teamRoleService.addMemberToRole(teamRole.getTeam().getId(), addRequest);
    }

    public TeamCategory createCategory(TeamRole teamRole, Team team, Member admin) {
        TeamMember adminMember = teamMemberRepository.findByTeamAndMember(team, admin).orElseThrow(() -> new EntityNotFoundException("teamMember not found"));
        CategoryRolePermissionDTO dto1 = new CategoryRolePermissionDTO(teamRole.getId(), new HashSet<>(Arrays.asList(VIEW_POST, DELETE_POST)));
        //admin 권한 추가
        CategoryRolePermissionDTO dto2 = new CategoryRolePermissionDTO(adminMember.getRole().getId(), new HashSet<>(Arrays.asList(VIEW_POST, DELETE_POST, CREATE_POST, CREATE_COMMENT, DELETE_COMMENT)));
        CreateCategoryRequest categoryRequest = new CreateCategoryRequest("testCategory", "create category test", List.of(dto1, dto2));
        return categoryService.createCategory(team.getId(), categoryRequest);
    }

    public TeamMember getAdminMember(Team team, Member admin) {
        return teamMemberRepository.findByTeamAndMember(team, admin).orElseThrow(() -> new EntityNotFoundException("admin member not found"));
    }
}