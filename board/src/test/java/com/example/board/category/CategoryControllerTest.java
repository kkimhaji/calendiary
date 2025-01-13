package com.example.board.category;

import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamCategory;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.dto.category.CategoryRolePermissionDTO;
import com.example.board.dto.category.CreateCategoryRequest;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.support.TestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class CategoryControllerTest extends AbstractControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TestDataBuilder testDataBuilder;
    private Team team;
    private TeamMember teamMember;
    private TeamRole teamRole;
    private TeamCategory category;

    @BeforeEach
    void init(){
        team = testDataBuilder.createTeam(member1);
        teamMember = testDataBuilder.addMemberToTeam(member2, team);
        teamRole = testDataBuilder.createNewRole(team, "test role");
        category = testDataBuilder.createCategory(teamRole, team, member1);
    }

    @Test
    public void createCategory_without_permission_403() throws Exception {
//        TeamMember adminMember = teamMemberRepository.findByTeamAndMember(team, admin).orElseThrow(()-> new EntityNotFoundException("teamMember not found"));
//        CategoryRolePermissionDTO dto1 = new CategoryRolePermissionDTO(teamRole.getId(), new HashSet<>(Arrays.asList(VIEW_POST, DELETE_POST)));
//        //admin 권한 추가
//        CategoryRolePermissionDTO dto2 = new CategoryRolePermissionDTO(adminMember.getRole().getId(), new HashSet<>(Arrays.asList(VIEW_POST, DELETE_POST, CREATE_POST, CREATE_COMMENT, EDIT_POST, DELETE_COMMENT)));
//        CreateCategoryRequest categoryRequest = new CreateCategoryRequest("testCategory", "create category test", List.of(dto1, dto2));
//        return categoryService.createCategory(team.getId(), categoryRequest);
    }
}
