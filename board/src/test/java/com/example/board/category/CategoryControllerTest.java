package com.example.board.category;

import com.example.board.auth.UserPrincipal;
import com.example.board.category.dto.CategoryRolePermissionDTO;
import com.example.board.category.dto.CreateCategoryRequest;
import com.example.board.category.dto.UpdateCategoryRequest;
import com.example.board.config.security.WithMockCategoryPermission;
import com.example.board.config.security.WithMockTeamPermission;
import com.example.board.permission.CategoryPermission;
import com.example.board.role.TeamRole;
import com.example.board.role.TeamRoleRepository;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.Team;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

    private Long getTeamIdFromUserPrincipal(){
        Authentication authentication =  SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userPrincipal.getTestTeamId();
    }

    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_CATEGORIES"})
    void createCategoryTest_withPermission() throws Exception {
        Long teamId = getTeamIdFromUserPrincipal();
        var DTO = builder.forCreateCategoryRequest(teamId, null, new HashSet<>(Arrays.asList(CategoryPermission.VIEW_POST)));
        var request = objectMapper.writeValueAsString(DTO);

        mockMvc.perform(post("/teams/{teamId}/categories/create", teamId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(request))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockTeamPermission()
    void createCategoryTest_withoutPermission() throws Exception {
        Long teamId = getTeamIdFromUserPrincipal();
        var DTO = builder.forCreateCategoryRequest(teamId, null, new HashSet<>(Arrays.asList(CategoryPermission.VIEW_POST)));
        var request = objectMapper.writeValueAsString(DTO);

        mockMvc.perform(post("/teams/{teamId}/categories/create", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_CATEGORIES"})
    void updateCategoryTest_withPermission() throws Exception {
        Long teamId = getTeamIdFromUserPrincipal();
        TeamCategory category = builder.createCategory(null, teamId, new HashSet<>());
        var request = objectMapper.writeValueAsString(
                new UpdateCategoryRequest("test category", "1234", null)
        );

        mockMvc.perform(put("/teams/{teamId}/categories/{createId}/update", teamId, category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockTeamPermission()
    void updateCategoryTest_withoutPermission() throws Exception {
        Long teamId = getTeamIdFromUserPrincipal();
        TeamCategory category = builder.createCategory(null, teamId, new HashSet<>());
        var request = objectMapper.writeValueAsString(
                new UpdateCategoryRequest("test category", "1234", null)
        );

        mockMvc.perform(put("/teams/{teamId}/categories/{createId}/update", teamId, category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());
    }
}