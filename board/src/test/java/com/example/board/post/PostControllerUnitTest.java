package com.example.board.post;

import com.example.board.config.security.WithMockCategoryPermission;
import com.example.board.post.dto.PostResponse;
import com.example.board.post.dto.UpdatePostRequestDTO;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.support.TestDataBuilder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PostControllerUnitTest extends AbstractControllerTestSupport {

    @MockBean
    private PostService postService;
    @Autowired
    private TestDataBuilder testDataBuilder;

    @Test
    @DisplayName("게시글 수정 - 성공")
    @WithMockCategoryPermission
    void updatePost_Success() throws Exception {
        // Given
        Long teamId = testDataBuilder.getCurrentTestTeamId();
        Long categoryId = testDataBuilder.getCurrentCategoryId();
        Long postId = testDataBuilder.createPost("테스트", "테스트용 게시글", categoryId, teamId).getId();

        UpdatePostRequestDTO request = new UpdatePostRequestDTO(
                "수정된 제목",
                "수정된 내용",
                new ArrayList<>()
        );

        PostResponse mockResponse = new PostResponse(
                postId, "수정된 제목", "수정된 내용", "test1",
                teamId, categoryId, "개발팀", 5, LocalDateTime.now(), 3L
        );

        given(postService.updatePost(eq(categoryId), eq(postId), any(UpdatePostRequestDTO.class)))
                .willReturn(mockResponse);

        String jsonBody = objectMapper.writeValueAsString(request);

        mockMvc.perform(put("/teams/{teamId}/category/{categoryId}/posts/{postId}", teamId, categoryId, postId)
                        .with(user(testDataBuilder.getCurrentUserPrincipal()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"));
    }
}
