package com.example.board.category;

import com.example.board.category.dto.CategoryOrderUpdateRequest;
import com.example.board.category.dto.CategoryReorderRequest;
import com.example.board.category.dto.UpdateCategoryRequest;
import com.example.board.config.security.WithMockTeamPermission;
import com.example.board.permission.CategoryPermission;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.Team;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashSet;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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
        var DTO = builder.forCreateCategoryRequest(teamId, null, "test category", new HashSet<>(List.of(CategoryPermission.VIEW_POST)));
        var request = objectMapper.writeValueAsString(DTO);

        mockMvc.perform(post("/teams/{teamId}/categories/create", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockTeamPermission()
    void createCategoryTest_withoutPermission() throws Exception {
        var DTO = builder.forCreateCategoryRequest(teamId, null, "test category", new HashSet<>(List.of(CategoryPermission.VIEW_POST)));
        var request = objectMapper.writeValueAsString(DTO);

        mockMvc.perform(post("/teams/{teamId}/categories/create", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockTeamPermission(teamPermissions = {"MANAGE_CATEGORIES"})
    void updateCategoryTest_withPermission() throws Exception {
        TeamCategory category = builder.createCategory(null, teamId, "test category", new HashSet<>());
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
        TeamCategory category = builder.createCategory(null, teamId, "test category", new HashSet<>());
        var request = objectMapper.writeValueAsString(
                new UpdateCategoryRequest("test category", "1234", null)
        );

        mockMvc.perform(put("/teams/{teamId}/categories/{createId}/update", teamId, category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isForbidden());
    }


    /* ===== 카테고리 순서 변경 테스트 ===== */

    @Test
    @DisplayName("카테고리 순서 변경 성공 - 단일 이동")
    @WithMockTeamPermission(teamPermissions = {"MANAGE_CATEGORIES"})
    void updateCategoryOrder_success() throws Exception {
        // Given
        TeamCategory category1 = builder.createCategory(null, teamId, "카테고리1", new HashSet<>());
        TeamCategory category2 = builder.createCategory(null, teamId, "카테고리2", new HashSet<>());
        TeamCategory category3 = builder.createCategory(null, teamId, "카테고리3", new HashSet<>());

        CategoryOrderUpdateRequest request = new CategoryOrderUpdateRequest(0); // 3번을 0번 위치로

        // When & Then
        mockMvc.perform(put("/teams/{teamId}/categories/{categoryId}/order", teamId, category3.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("카테고리 순서 변경 실패 - 권한 없음")
    @WithMockTeamPermission()
    void updateCategoryOrder_withoutPermission() throws Exception {
        // Given
        TeamCategory category = builder.createCategory(null, teamId, "카테고리", new HashSet<>());
        CategoryOrderUpdateRequest request = new CategoryOrderUpdateRequest(0);

        // When & Then
        mockMvc.perform(put("/teams/{teamId}/categories/{categoryId}/order", teamId, category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("카테고리 순서 변경 실패 - 잘못된 순서 값 (음수)")
    @WithMockTeamPermission(teamPermissions = {"MANAGE_CATEGORIES"})
    void updateCategoryOrder_invalidNegativeOrder() throws Exception {
        // Given
        TeamCategory category = builder.createCategory(null, teamId, "카테고리", new HashSet<>());
        CategoryOrderUpdateRequest request = new CategoryOrderUpdateRequest(-1);

        // When & Then
        mockMvc.perform(put("/teams/{teamId}/categories/{categoryId}/order", teamId, category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("카테고리 순서 일괄 변경 성공")
    @WithMockTeamPermission(teamPermissions = {"MANAGE_CATEGORIES"})
    void reorderCategories_success() throws Exception {
        // Given
        TeamCategory category1 = builder.createCategory(null, teamId, "카테고리1", new HashSet<>());
        TeamCategory category2 = builder.createCategory(null, teamId, "카테고리2", new HashSet<>());
        TeamCategory category3 = builder.createCategory(null, teamId, "카테고리3", new HashSet<>());

        // 순서 변경: 3 -> 2 -> 1
        CategoryReorderRequest request = new CategoryReorderRequest(
                List.of(category3.getId(), category2.getId(), category1.getId())
        );

        // When & Then
        mockMvc.perform(put("/teams/{teamId}/categories/reorder", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("카테고리 순서 일괄 변경 실패 - 권한 없음")
    @WithMockTeamPermission()
    void reorderCategories_withoutPermission() throws Exception {
        // Given
        TeamCategory category1 = builder.createCategory(null, teamId, "카테고리1", new HashSet<>());
        TeamCategory category2 = builder.createCategory(null, teamId, "카테고리2", new HashSet<>());

        CategoryReorderRequest request = new CategoryReorderRequest(
                List.of(category2.getId(), category1.getId())
        );

        // When & Then
        mockMvc.perform(put("/teams/{teamId}/categories/reorder", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("카테고리 순서 일괄 변경 실패 - 빈 목록")
    @WithMockTeamPermission(teamPermissions = {"MANAGE_CATEGORIES"})
    void reorderCategories_emptyList_badRequest() throws Exception {
        // Given
        CategoryReorderRequest request = new CategoryReorderRequest(List.of());

        // When & Then
        mockMvc.perform(put("/teams/{teamId}/categories/reorder", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("카테고리 순서 일괄 변경 실패 - 다른 팀의 카테고리 포함")
    @WithMockTeamPermission(teamPermissions = {"MANAGE_CATEGORIES"})
    void reorderCategories_differentTeam_badRequest() throws Exception {
        // Given
        TeamCategory category1 = builder.createCategory(null, teamId, "카테고리1", new HashSet<>());

        // 다른 팀 생성
        Team otherTeam = builder.createTeam(builder.getCurrentUserPrincipal().getMember());
        TeamCategory otherCategory = builder.createCategory(null, otherTeam.getId(), "다른팀카테고리", new HashSet<>());

        CategoryReorderRequest request = new CategoryReorderRequest(
                List.of(category1.getId(), otherCategory.getId())
        );

        // When & Then
        mockMvc.perform(put("/teams/{teamId}/categories/reorder", teamId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("다른 팀의 카테고리는 재정렬할 수 없습니다."));
    }
}