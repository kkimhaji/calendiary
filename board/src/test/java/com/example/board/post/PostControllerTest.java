package com.example.board.post;

import com.example.board.category.TeamCategory;
import com.example.board.config.security.WithMockCategoryPermission;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
    @Autowired
    private PostRepository postRepository;

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

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"VIEW_POST"})
    @DisplayName("카테고리 최근 게시글 조회 성공")
    void getPosts_success() throws Exception {
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();
        Post savedPost = testDataBuilder.createPost("카테고리 최근 게시글", "test content", testCategoryId, testTeamId);
        mockMvc.perform(get("/teams/{teamId}/category/{categoryId}/recent",
                        testTeamId, testCategoryId)
                        .param("page", "0")          // Pageable
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categoryName")
                        .value(savedPost.getCategory().getName()))
                .andExpect(jsonPath("$.posts.content[0].title")
                        .value(savedPost.getTitle()));

    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"CREATE_POST"})
    @DisplayName("카테고리 최근 게시글 조회 - 권한 없음")
    void getPosts_without_permission() throws Exception {
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();
        testDataBuilder.createPost("카테고리 최근 게시글", "test content", testCategoryId, testTeamId);
        mockMvc.perform(get("/teams/{teamId}/category/{categoryId}/recent",
                        testTeamId, testCategoryId)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"VIEW_POST"})
    @DisplayName("카테고리 최근 게시글 조회 - 팀 아이디 오류")
    void getPosts_teamNotFound() throws Exception {
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();
        testDataBuilder.createPost("카테고리 최근 게시글", "test content", testCategoryId, testTeamId);
        mockMvc.perform(get("/teams/{teamId}/category/{categoryId}/recent",
                        999L, testCategoryId)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"VIEW_POST"})
    @DisplayName("카테고리 최근 게시글 조회 - 카테고리 아이디 오류: @PreAuthorize가 먼저 동작하므로 권한 없음")
    void getPosts_categoryIdError_isForbidden() throws Exception {
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();
        testDataBuilder.createPost("카테고리 최근 게시글", "test content", testCategoryId, testTeamId);
        mockMvc.perform(get("/teams/{teamId}/category/{categoryId}/recent",
                        testTeamId, 999L)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"VIEW_POST"})
    @DisplayName("게시글 상세 조회 성공")
    void getPost_success() throws Exception {
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();
        Post savedPost = testDataBuilder.createPost("test post", "test content", testCategoryId, testTeamId);

        mockMvc.perform(get("/teams/{teamId}/category/{categoryId}/posts/{postId}",
                        testTeamId, testCategoryId, savedPost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPost.getId()))
                .andExpect(jsonPath("$.title").value(savedPost.getTitle()));
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {})
    @DisplayName("게시글 삭제 성공(권한이 없어도 작성자 본인일 경우)")
    void deletePost_byAuthor_success() throws Exception {
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();
        Post savedPost = testDataBuilder.createPost("test post", "test content", testCategoryId, testTeamId);

        mockMvc.perform(delete("/teams/{teamId}/category/{categoryId}/posts/delete/{postId}",
                        testTeamId, testCategoryId, savedPost.getId())
                        .with(user(testDataBuilder.getCurrentUserPrincipal()))
                        .with(csrf()))
                .andExpect(status().isOk());

        // 실제 DB-삭제 확인
        assertThat(postRepository.findById(savedPost.getId())).isEmpty();
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"DELETE_POST"})
    @DisplayName("게시글 삭제 성공(작성자 본인이 아니어도 권한이 있는 경우)")
    void deletePost_withPermission_success() throws Exception {
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();
        //현재 로그인한 사용자가 아닌 다른 멤버를 팀에 넣기
        TeamMember teamMember = testDataBuilder.addMemberToTeam(member1, testTeamId);
        //다른 멤버가 만든 게시글
        Post savedPost = testDataBuilder.createPost("test post", "test content", member1, testCategoryId, testTeamId, teamMember);

        mockMvc.perform(delete("/teams/{teamId}/category/{categoryId}/posts/delete/{postId}",
                        testTeamId, testCategoryId, savedPost.getId())
                        //삭제하는 주체는 로그인한 사용자
                        .with(user(testDataBuilder.getCurrentUserPrincipal()))
                        .with(csrf()))
                .andExpect(status().isOk());

        // 실제 DB-삭제 확인
        assertThat(postRepository.findById(savedPost.getId())).isEmpty();
    }

}
