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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.multipart;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
    @MockBean
    private ImageService imageService;

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

    @Test
    @WithMockCategoryPermission(categoryPermissions = {})
    @DisplayName("이미지 없는 게시글 생성 - 권한 없음")
    void createPost_withoutImages_forBidden() throws Exception {
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();

        mockMvc.perform(multipart("/teams/{teamId}/category/{categoryId}/posts", testTeamId, testCategoryId)
                        .param("title", "Test Post Title")
                        .param("content", "Test Post Content")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(user(testDataBuilder.getCurrentUserPrincipal())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"CREATE_POST"})
    @DisplayName("게시글 생성 성공 - 이미지 포함")
    void createPost_withImage_success() throws Exception {
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();

        MockMultipartFile imageFile = new MockMultipartFile(
                "images",                      // multipart name
                "test-image.jpg",              // 원본 파일명
                "image/jpeg",                  // MIME type
                "dummy image bytes".getBytes() // 내용 – 아무 바이트 가능
        );

        mockMvc.perform(multipart("/teams/{teamId}/category/{categoryId}/posts", testTeamId, testCategoryId)
                        .file(imageFile)
                        .param("title", "테스트 제목")    // title 필드에 바인딩
                        .param("content", "테스트 내용")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"VIEW_POST"})
    @DisplayName("이미지 포함 게시글 - 권한 없음")
    void createPost_withImage_isForbidden() throws Exception {
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();

        MockMultipartFile imageFile = new MockMultipartFile(
                "images",                      // multipart name
                "test-image.jpg",              // 원본 파일명
                "image/jpeg",                  // MIME type
                "dummy image bytes".getBytes() // 내용 – 아무 바이트 가능
        );

        mockMvc.perform(multipart("/teams/{teamId}/category/{categoryId}/posts", testTeamId, testCategoryId)
                        .file(imageFile)
                        .param("title", "테스트 제목")    // title 필드에 바인딩
                        .param("content", "테스트 내용")
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"CREATE_POST"})
    @DisplayName("게시글 생성 - 제목 누락")
    void createPost_noTitle() throws Exception {
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();

        MockMultipartFile imageFile = new MockMultipartFile(
                "images",                      // multipart name
                "test-image.jpg",              // 원본 파일명
                "image/jpeg",                  // MIME type
                "dummy image bytes".getBytes() // 내용 – 아무 바이트 가능
        );

        mockMvc.perform(multipart("/teams/{teamId}/category/{categoryId}/posts", testTeamId, testCategoryId)
                        .file(imageFile)
                        .param("content", "테스트 내용")
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"CREATE_POST"})
    @DisplayName("게시글 생성 - 내용 누락")
    void createPost_noContent() throws Exception {
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();

        MockMultipartFile imageFile = new MockMultipartFile(
                "images",                      // multipart name
                "test-image.jpg",              // 원본 파일명
                "image/jpeg",                  // MIME type
                "dummy image bytes".getBytes() // 내용 – 아무 바이트 가능
        );

        mockMvc.perform(multipart("/teams/{teamId}/category/{categoryId}/posts", testTeamId, testCategoryId)
                        .file(imageFile)
                        .param("title", "테스트 제목")    // title 필드에 바인딩
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }


}
