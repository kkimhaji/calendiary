package com.example.board.support;

import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamCategory;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.domain.teamMember.TeamMemberRepository;
import com.example.board.dto.category.CategoryRolePermissionDTO;
import com.example.board.dto.category.CreateCategoryRequest;
import com.example.board.dto.role.AddMembersToRoleRequest;
import com.example.board.dto.role.CreateRoleRequest;
import com.example.board.dto.team.AddMemberRequestDTO;
import com.example.board.dto.team.TeamCreateRequestDTO;
import com.example.board.permission.TeamPermission;
import com.example.board.service.CategoryService;
import com.example.board.service.TeamRoleService;
import com.example.board.service.TeamService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.example.board.permission.TeamPermission.*;
import static com.example.board.permission.CategoryPermission.*;


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
                Member.builder()
                        .email(email)
                        .nickname(nickname)
                        .password(passwordEncoder.encode(password))
                        .enabled(true)
                        .build()
        );
    }

    public Team createTeam(Member member1){
        var request = new TeamCreateRequestDTO("testTeam", "test");
        return teamService.createTeam(member1, request);
    }

    public TeamMember addMemberToTeam(Member member2, Team team){
        AddMemberRequestDTO dto = new AddMemberRequestDTO(team.getId(), member2.getMemberId());
        return teamService.addMember(dto);
    }

    public TeamRole createNewRole(Team team, String roleName){
        Set<TeamPermission> permissions = new HashSet<>(Arrays.asList(
                MANAGE_ROLES,MANAGE_MEMBERS
        ));
        var roleRequest = new CreateRoleRequest(roleName, permissions, "role for test");
        return teamRoleService.createRole(team.getId(), roleRequest);
    }

    public void addMemberToRole(Member member, TeamRole teamRole){
        var addRequest = new AddMembersToRoleRequest(teamRole.getId(), Collections.singletonList(member.getMemberId()));
        teamRoleService.addMemberToRole(teamRole.getTeam().getId(), addRequest);
    }

    public TeamCategory createCategory(TeamRole teamRole, Team team, Member admin){
        TeamMember adminMember = teamMemberRepository.findByTeamAndMember(team, admin).orElseThrow(()-> new EntityNotFoundException("teamMember not found"));
        CategoryRolePermissionDTO dto1 = new CategoryRolePermissionDTO(teamRole.getId(), new HashSet<>(Arrays.asList(VIEW_POST, DELETE_POST)));
        //admin 권한 추가
        CategoryRolePermissionDTO dto2 = new CategoryRolePermissionDTO(adminMember.getRole().getId(), new HashSet<>(Arrays.asList(VIEW_POST, DELETE_POST, CREATE_POST, CREATE_COMMENT, DELETE_COMMENT)));
        CreateCategoryRequest categoryRequest = new CreateCategoryRequest("testCategory", "create category test", List.of(dto1, dto2));
        return categoryService.createCategory(team.getId(), categoryRequest);
    }

    public TeamMember getAdminMember(Team team, Member admin){
        return teamMemberRepository.findByTeamAndMember(team, admin).orElseThrow(() -> new EntityNotFoundException("admin member not found"));
    }

}
