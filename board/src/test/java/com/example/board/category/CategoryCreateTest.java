package com.example.board.category;

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
import com.example.board.service.TeamRoleService;
import com.example.board.service.TeamService;
import com.example.board.support.AbstractTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.example.board.permission.TeamPermission.*;
import static com.example.board.permission.TeamPermission.VIEW_POST;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ComponentScan("com.example.board")
public class CategoryCreateTest extends AbstractTestSupport {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TeamService teamService;
    private Team team;
    private TeamMember teamMember;
    private TeamRole teamRole;
    @Autowired
    private TeamRoleService teamRoleService;

    @BeforeEach
    void init(){
        var request = new TeamCreateRequestDTO("testTeam", "test");
        team = teamService.createTeam(member1, request);
        AddMemberRequestDTO dto = new AddMemberRequestDTO(team.getId(), team.getBasicRoleId(), member2.getMemberId());
        teamMember = teamService.addMember(dto);
        Set<TeamPermission> permissions = new HashSet<>(Arrays.asList(
                CREATE_POST, DELETE_POST, MANAGE_ROLES, EDIT_POST, MANAGE_MEMBERS,
                VIEW_POST
        ));
        var roleRequest = new CreateRoleRequest("test role", permissions, "role for test");
        teamRole = teamRoleService.createRole(team.getId(), roleRequest);
    }

    @Test
    void createCategoryTest(){
        CategoryRolePermissionDTO dto = new CategoryRolePermissionDTO(teamRole.getId(), "VIEW_POST");
        CreateCategoryRequest request = new CreateCategoryRequest("testCategory", "create category test", List.of(dto));
        TeamCategory newCategory = categoryService.createCategory(team.getId(), request);

        assertThat(newCategory.getName()).isEqualTo("testCategory");
        assertThat(newCategory.getTeam()).isEqualTo(team);
        newCategory.getRolePermissions()
                .forEach(rolePermission -> System.out.println(rolePermission.getPermissions()));
    }
}
