package com.example.board.support;

import com.example.board.domain.member.Member;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamCategory;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.dto.category.CategoryRolePermissionDTO;
import com.example.board.dto.category.CreateCategoryRequest;
import com.example.board.dto.role.CreateRoleRequest;
import com.example.board.dto.team.AddMemberRequestDTO;
import com.example.board.dto.team.TeamCreateRequestDTO;
import com.example.board.permission.TeamPermission;
import com.example.board.service.CategoryService;
import com.example.board.service.MemberService;
import com.example.board.service.TeamRoleService;
import com.example.board.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.board.permission.TeamPermission.*;
import static com.example.board.permission.TeamPermission.VIEW_POST;

@Component
@RequiredArgsConstructor
public class TestDataBuilder {
    private final TeamService teamService;
    private final TeamRoleService teamRoleService;
    private final CategoryService categoryService;

    public Team createTeam(Member member1){
        var request = new TeamCreateRequestDTO("testTeam", "test");
        Team team = teamService.createTeam(member1, request);

        return  team;
    }

    public TeamMember addMemberToTeam(Member member2, Team team){
        AddMemberRequestDTO dto = new AddMemberRequestDTO(team.getId(), team.getBasicRoleId(), member2.getMemberId());
        return teamService.addMember(dto);
    }

    public TeamRole createNewRole(Team team){
        Set<TeamPermission> permissions = new HashSet<>(Arrays.asList(
                CREATE_POST, DELETE_POST, MANAGE_ROLES, EDIT_POST, MANAGE_MEMBERS,
                VIEW_POST
        ));
        var roleRequest = new CreateRoleRequest("test role", permissions, "role for test");
        return teamRoleService.createRole(team.getId(), roleRequest);
    }

    public TeamCategory createCategory(TeamRole teamRole, Team team){
        CategoryRolePermissionDTO rolePermissionDTO = new CategoryRolePermissionDTO(teamRole.getId(), "VIEW_POST");
        CreateCategoryRequest categoryRequest = new CreateCategoryRequest("testCategory", "create category test", List.of(rolePermissionDTO));
        return categoryService.createCategory(team.getId(), categoryRequest);
    }

}
