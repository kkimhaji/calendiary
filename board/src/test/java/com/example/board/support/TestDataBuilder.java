package com.example.board.support;

import com.example.board.category.CategoryService;
import com.example.board.category.TeamCategory;
import com.example.board.category.dto.CategoryRolePermissionDTO;
import com.example.board.category.dto.CreateCategoryRequest;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.permission.CategoryPermission;
import com.example.board.permission.TeamPermission;
import com.example.board.role.TeamRole;
import com.example.board.role.TeamRoleService;
import com.example.board.role.dto.AddMembersToRoleRequest;
import com.example.board.role.dto.CreateRoleRequest;
import com.example.board.team.Team;
import com.example.board.team.TeamRepository;
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
import java.util.stream.Collectors;

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
    private final TeamRepository teamRepository;
    private final TestDataFactory factory;

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

    public TeamRole createNewRoleWithPermissions(Team team, String roleName, Set<TeamPermission> permissions) {
        return teamRoleService.createRole(team.getId(), new CreateRoleRequest(roleName, permissions, "new role with permissions"));
    }

    public void addMemberToRole(Member member, TeamRole teamRole) {
        var addRequest = new AddMembersToRoleRequest(teamRole.getId(), Collections.singletonList(member.getMemberId()));
        teamRoleService.addMemberToRole(teamRole.getTeam().getId(), addRequest);
    }

    public TeamCategory createCategory(Long roleId, Long teamId, Set<CategoryPermission> categoryPermissions) {
        return categoryService.createCategory(teamId, forCreateCategoryRequest(teamId, roleId, categoryPermissions));
    }

    public void updateRolePermission(Long roleId, Set<TeamPermission> permissions) {
        teamRoleService.updateRolePermissions(roleId, permissions);
    }

    public CreateCategoryRequest forCreateCategoryRequest(Long teamId, Long roleId, Set<CategoryPermission> permissions){
        Team team = teamRepository.findById(teamId).orElseThrow();
        if (roleId==null){
            roleId = team.getBasicRoleId();
        }
        //admin 설정 - 모든 권한 허용
        CategoryRolePermissionDTO adminDTO = new CategoryRolePermissionDTO(team.getAdminRoleId(), new HashSet<>(Arrays.asList(CategoryPermission.values())));
        CategoryRolePermissionDTO basicDTO = new CategoryRolePermissionDTO(roleId, permissions);
        return new CreateCategoryRequest("TestCategory", "category for test", List.of(adminDTO, basicDTO));
    }
}