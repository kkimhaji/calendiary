package com.example.board.category;

import com.example.board.category.dto.UpdateCategoryRequest;
import com.example.board.config.security.WithMockTeamPermission;
import com.example.board.permission.CategoryPermission;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.support.TestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;

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
    private Long teamId;

    @BeforeEach
    void init() {
        teamId = builder.getCurrentTestTeamId();
    }

    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_CATEGORIES"})
    void createCategoryTest_withPermission() throws Exception {
        var DTO = builder.forCreateCategoryRequest(teamId, null, new HashSet<>(List.of(CategoryPermission.VIEW_POST)));
        var request = objectMapper.writeValueAsString(DTO);

        mockMvc.perform(post("/teams/{teamId}/categories/create", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockTeamPermission()
    void createCategoryTest_withoutPermission() throws Exception {
        var DTO = builder.forCreateCategoryRequest(teamId, null, new HashSet<>(List.of(CategoryPermission.VIEW_POST)));
        var request = objectMapper.writeValueAsString(DTO);

        mockMvc.perform(post("/teams/{teamId}/categories/create", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_CATEGORIES"})
    void updateCategoryTest_withPermission() throws Exception {
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