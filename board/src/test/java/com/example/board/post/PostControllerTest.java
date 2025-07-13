package com.example.board.post;

import com.example.board.category.TeamCategory;
import com.example.board.config.security.WithMockCategoryPermission;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.Team;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PostControllerTest extends AbstractControllerTestSupport {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestDataBuilder testDataBuilder;
    @Autowired
    private ObjectMapper objectMapper;
    private Team testTeam;
    private TeamCategory testCategory;
    private Post testPost;
    @Autowired
    private PostService postService;

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"CREATE_POST"})
    @DisplayName("게시글 생성 성공 - 이미지 없이")
    void createPost_withoutImages_success() throws Exception {
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();

        mockMvc.perform(multipart("/teams/{teamId}/category/{categoryId}/posts", testTeamId, testCategoryId)
                        .param("title", "Test Post Title")
                        .param("content", "Test Post Content")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(user(testDataBuilder.getCurrentUserPrincipal())))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

}
