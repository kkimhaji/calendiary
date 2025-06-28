package com.example.board.category;

import com.example.board.auth.UserPrincipal;
import com.example.board.category.dto.CategoryRolePermissionDTO;
import com.example.board.category.dto.CreateCategoryRequest;
import com.example.board.config.security.WithMockCategoryPermission;
import com.example.board.config.security.WithMockTeamPermission;
import com.example.board.permission.CategoryPermission;
import com.example.board.role.TeamRole;
import com.example.board.role.TeamRoleRepository;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.support.TestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CategoryControllerTest extends AbstractControllerTestSupport {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestDataBuilder builder;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private TeamRoleRepository teamRoleRepository;

    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_CATEGORIES"})
    void createCategoryTest() throws Exception {
        UserPrincipal userPrincipal = (UserPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        List<TeamRole> teamRoles = teamRoleRepository.findAllByTeamId(userPrincipal.getTestTeamId());
        List<CategoryRolePermissionDTO> rolePermissions = teamRoles.stream()
                .map(role -> new CategoryRolePermissionDTO(
                        role.getId(),
                        Set.of(CategoryPermission.VIEW_POST, CategoryPermission.CREATE_POST)
                ))
                .collect(Collectors.toList());

        var request = objectMapper.writeValueAsString(new CreateCategoryRequest("category", "test category", rolePermissions));

        mockMvc.perform(post("/teams/{teamId}/categories/create", userPrincipal.getTestTeamId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk());
    }
}
