package com.example.board.category;

import com.example.board.role.TeamRole;
import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import com.example.board.category.dto.CategoryRolePermissionDTO;
import com.example.board.category.dto.CreateCategoryRequest;
import com.example.board.team.TeamService;
import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static com.example.board.permission.CategoryPermission.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ComponentScan("com.example.board")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CategoryCreateTest extends AbstractTestSupport {
    @Autowired
    private TestDataBuilder testDataBuilder;

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TeamService teamService;
    private Team team;
    private TeamMember teamMember;
    private TeamRole teamRole;

    @BeforeEach
    void init(){
        team = testDataBuilder.createTeam(member1);
        teamMember = testDataBuilder.addMemberToTeam(member2, team);
        teamRole = testDataBuilder.createNewRole(team, "test role");
    }

    @Test
    void createCategoryTest(){
        CategoryRolePermissionDTO dto1 = new CategoryRolePermissionDTO(teamRole.getId(), new HashSet<>(Arrays.asList(VIEW_POST, DELETE_POST)));
        CreateCategoryRequest request = new CreateCategoryRequest("testCategory", "create category test", List.of(dto1));
        TeamCategory newCategory = categoryService.createCategory(team.getId(), request);

        assertThat(newCategory.getName()).isEqualTo("testCategory");
        assertThat(newCategory.getTeam()).isEqualTo(team);
        newCategory.getRolePermissions()
                .forEach(rolePermission -> System.out.println(rolePermission.getPermissions()));
    }
}
