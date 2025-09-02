package com.example.board.post;

import com.example.board.category.TeamCategory;
import com.example.board.common.exception.PostNotFoundException;
import com.example.board.common.exception.TeamNotFoundException;
import com.example.board.config.security.WithMockCategoryPermission;
import com.example.board.image.ImageService;
import com.example.board.member.Member;
import com.example.board.post.dto.*;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.Team;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
    @MockBean
    private PostService postService;
    @MockBean
    private ImageService imageService;
    @Autowired
    private PostRepository postRepository;


    @Test
    @WithMockCategoryPermission(categoryPermissions = {"CREATE_POST"})
    @DisplayName("게시글 생성 성공 - 이미지 없이")
    void createPost_withoutImages_success() throws Exception {
        Team team = testDataBuilder.getCurrentTeam();
        TeamCategory category = testDataBuilder.getCurrentCategory();
        Long teamId = team.getId();
        Member member = testDataBuilder.getCurrentUserPrincipal().getMember();

        CreatePostRequest request = new CreatePostRequest(
                "Test Post Title",
                "Test Post Content"
        );

        Post expectedPost = Post.create("Test Post Title", "Test Post Content", member, category, team, testDataBuilder.getTeamMember(teamId, member.getMemberId()));
        ReflectionTestUtils.setField(expectedPost, "id", 1L);
        given(postService.createPost(
                eq(teamId),
                eq(category.getId()),
                any(CreatePostRequest.class), any()// 빈 리스트로 명시적 설정
        )).willReturn(expectedPost);


        mockMvc.perform(post("/teams/{teamId}/category/{categoryId}/posts", teamId, category.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(testDataBuilder.getCurrentUserPrincipal()))
                        .with(csrf()))
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("Test Post Title"))
                .andExpect(jsonPath("$.content").value("Test Post Content"));
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {})
    @DisplayName("이미지 없는 게시글 생성 - 권한 없음")
    void createPost_withoutImages_forBidden() throws Exception {
        Long teamId = testDataBuilder.getCurrentTestTeamId();
        Long categoryId = testDataBuilder.getCurrentCategoryId();

        CreatePostRequest request = new CreatePostRequest(
                "Test Post Title",
                "Test Post Content"
        );

        mockMvc.perform(post("/teams/{teamId}/category/{categoryId}/posts", teamId, categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(testDataBuilder.getCurrentUserPrincipal()))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"CREATE_POST"})
    @DisplayName("게시글 생성 - 제목 누락")
    void createPost_noTitle() throws Exception {
        Long teamId = testDataBuilder.getCurrentTestTeamId();
        Long categoryId = testDataBuilder.getCurrentCategoryId();

        CreatePostRequest request = new CreatePostRequest(null, "테스트 내용");

        mockMvc.perform(post("/teams/{teamId}/category/{categoryId}/posts", teamId, categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(testDataBuilder.getCurrentUserPrincipal()))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"CREATE_POST"})
    @DisplayName("게시글 생성 - 내용 누락")
    void createPost_noContent() throws Exception {
        Long teamId = testDataBuilder.getCurrentTestTeamId();
        Long categoryId = testDataBuilder.getCurrentCategoryId();

        CreatePostRequest request = new CreatePostRequest("테스트 제목", null);

        mockMvc.perform(post("/teams/{teamId}/category/{categoryId}/posts", teamId, categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(testDataBuilder.getCurrentUserPrincipal()))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"CREATE_POST"})
    @DisplayName("이미지 URL이 포함된 게시글 생성")
    void createPost_withImageUrls_success() throws Exception {
        // Given
        Team team = testDataBuilder.getCurrentTeam();
        TeamCategory category = testDataBuilder.getCurrentCategory();
        Long teamId = team.getId();
        Long categoryId = category.getId();
        Member member = testDataBuilder.getCurrentUserPrincipal().getMember();

        CreatePostRequest request = new CreatePostRequest(
                "테스트 게시글",
                "<p>내용입니다.</p><img src='https://example.com/image1.jpg'><p>더 많은 내용</p>"
        );

        Post expectedPost = Post.create(request.title(), request.content(), member, category, team, testDataBuilder.getTeamMember(teamId, member.getMemberId()));

        ReflectionTestUtils.setField(expectedPost, "id", 1L);

        given(postService.createPost(eq(teamId), eq(categoryId), any(CreatePostRequest.class), any(Member.class)))
                .willReturn(expectedPost);

        // When & Then
        mockMvc.perform(post("/teams/{teamId}/category/{categoryId}/posts", teamId, categoryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(testDataBuilder.getCurrentUserPrincipal()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("테스트 게시글"));

        // Service 호출 검증
        verify(postService).createPost(eq(teamId), eq(categoryId), any(CreatePostRequest.class), any(Member.class));
    }

    @Test
    @WithMockCategoryPermission
    @DisplayName("게시글 수정 - 이미지 URL 변경")
    void updatePost_changeImageUrls_success() throws Exception {
        // Given
        Long teamId = testDataBuilder.getCurrentTestTeamId();
        Long categoryId = testDataBuilder.getCurrentCategoryId();
        Long postId = 1L;

        UpdatePostRequestDTO request = new UpdatePostRequestDTO(
                "수정된 게시글",
                "<p>수정된 내용</p><img src='https://example.com/new-image.jpg'>",
                Arrays.asList(1L, 2L));// 삭제할 기존 이미지

        PostResponse expectedResponse = new PostResponse(
                postId,
                "수정된 게시글",
                "<p>수정된 내용</p><img src='https://example.com/new-image.jpg'>",
                "test1",
                teamId,
                categoryId,
                "개발팀",
                5,
                LocalDateTime.now(),
                3L
        );

        given(postService.updatePost(eq(categoryId), eq(postId), any(UpdatePostRequestDTO.class)))
                .willReturn(expectedResponse);

        // When & Then
        mockMvc.perform(put("/teams/{teamId}/category/{categoryId}/posts/{postId}", teamId, categoryId, postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(testDataBuilder.getCurrentUserPrincipal()))
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 게시글"));

        verify(postService).updatePost(eq(categoryId), eq(postId), any(UpdatePostRequestDTO.class));
    }

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

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"VIEW_POST"})
    @DisplayName("카테고리 최근 게시글 조회 성공")
    void getPosts_success() throws Exception {
        // Given
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();

        // PostListResponse 생성 (실제 Post 객체 없이)
        PostListResponse postResponse = new PostListResponse(
                1L,
                "카테고리 최근 게시글",
                "테스트사용자",
                testTeamId,
                "Security context principal category",
                testCategoryId,
                0,
                LocalDateTime.now(),
                0L
        );

        // Page 객체 생성
        Pageable pageable = PageRequest.of(0, 20);
        List<PostListResponse> postList = List.of(postResponse);
        Page<PostListResponse> postPage = new PageImpl<>(postList, pageable, postList.size());

        // CategoryRecentPostsResponse 생성
        CategoryRecentPostsResponse mockResponse = new CategoryRecentPostsResponse(
                "Security context principal category",
                postPage
        );

        // PostService Mock 설정
        given(postService.getPostsByCategory(eq(testTeamId), eq(testCategoryId), any(Pageable.class)))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/teams/{teamId}/category/{categoryId}/recent", testTeamId, testCategoryId)
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())  // ✅ 응답 출력으로 디버깅
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.categoryName").value("Security context principal category"))
                .andExpect(jsonPath("$.posts.content[0].title").value("카테고리 최근 게시글"))
                .andExpect(jsonPath("$.posts.content[0].authorName").value("테스트사용자"));

        // Mock 호출 검증
        verify(postService).getPostsByCategory(eq(testTeamId), eq(testCategoryId), any(Pageable.class));
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"CREATE_POST"})
    @DisplayName("카테고리 최근 게시글 조회 - 권한 없음")
    void getPosts_without_permission() throws Exception {
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();

        mockMvc.perform(get("/teams/{teamId}/category/{categoryId}/recent", testTeamId, testCategoryId)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isForbidden());

        // 권한 실패 시에는 서비스 호출이 안 될 수 있음
        verify(postService, never()).getPostsByCategory(any(), any(), any());
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"VIEW_POST"})
    @DisplayName("카테고리 최근 게시글 조회 - 팀 아이디 오류")
    void getPosts_teamNotFound() throws Exception {
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();
        Long invalidTeamId = 999L;

        // 예외 발생 Mock 설정
        given(postService.getPostsByCategory(eq(invalidTeamId), eq(testCategoryId), any(Pageable.class)))
                .willThrow(new TeamNotFoundException("team not found"));

        mockMvc.perform(get("/teams/{teamId}/category/{categoryId}/recent", invalidTeamId, testCategoryId)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"VIEW_POST"})
    @DisplayName("카테고리 최근 게시글 조회 - 카테고리 아이디 오류: @PreAuthorize가 먼저 동작하므로 권한 없음")
    void getPosts_categoryIdError_isForbidden() throws Exception {
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long invalidCategoryId = 999L;  // 존재하지 않는 카테고리 ID

        // @PreAuthorize에 의해 서비스 호출 전에 권한 검사에서 실패
        mockMvc.perform(get("/teams/{teamId}/category/{categoryId}/recent", testTeamId, invalidCategoryId)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isForbidden());

        // 권한 검사에서 실패했으므로 서비스는 호출되지 않음
        verify(postService, never()).getPostsByCategory(any(), any(), any());
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"VIEW_POST"})
    @DisplayName("게시글 상세 조회 성공")
    void getPost_success() throws Exception {
        // Given
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();
        Long postId = 1L;

        // Mock 응답 데이터 생성
        AuthorDTO authorDTO = new AuthorDTO(1L, "테스트사용자");
        PostDetailDTO mockResponse = new PostDetailDTO(
                postId,
                "test post",
                "test content",
                authorDTO,
                "Security context principal category",
                10L,  // viewCount
                LocalDateTime.now(),
                List.of("https://example.com/image1.jpg")
        );

        // PostService Mock 설정
        given(postService.getPostDetail(eq(testTeamId), eq(testCategoryId), eq(postId)))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/teams/{teamId}/category/{categoryId}/posts/{postId}",
                        testTeamId, testCategoryId, postId))
                .andExpect(status().isOk())  // ✅ andExpect 사용
                .andExpect(jsonPath("$.id").value(postId))
                .andExpect(jsonPath("$.title").value("test post"))
                .andExpect(jsonPath("$.content").value("test content"))
                .andExpect(jsonPath("$.author.username").value("테스트사용자"));

        // Mock 호출 검증
        verify(postService).getPostDetail(eq(testTeamId), eq(testCategoryId), eq(postId));
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {})
    @DisplayName("게시글 삭제 성공(권한이 없어도 작성자 본인일 경우)")
    void deletePost_byAuthor_success() throws Exception {
        // Given
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();
        Long postId = 1L;

        // 작성자 본인이므로 삭제 성공 - Mock 설정
        doNothing().when(postService).deletePost(eq(testTeamId), eq(postId), eq(testCategoryId));

        // When & Then
        mockMvc.perform(delete("/teams/{teamId}/category/{categoryId}/posts/delete/{postId}",
                        testTeamId, testCategoryId, postId)
                        .with(user(testDataBuilder.getCurrentUserPrincipal()))
                        .with(csrf()))
                .andExpect(status().isOk());

        // Mock 호출 검증
        verify(postService).deletePost(eq(testTeamId), eq(postId), eq(testCategoryId));
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"DELETE_POST"})
    @DisplayName("게시글 삭제 성공(작성자 본인이 아니어도 권한이 있는 경우)")
    void deletePost_withPermission_success() throws Exception {
        // Given
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();
        Long postId = 1L;

        // 권한이 있으므로 삭제 성공 - Mock 설정
        doNothing().when(postService).deletePost(eq(testTeamId), eq(postId), eq(testCategoryId));

        // When & Then
        mockMvc.perform(delete("/teams/{teamId}/category/{categoryId}/posts/delete/{postId}",
                        testTeamId, testCategoryId, postId)
                        .with(user(testDataBuilder.getCurrentUserPrincipal()))
                        .with(csrf()))
                .andExpect(status().isOk());

        // Mock 호출 검증
        verify(postService).deletePost(eq(testTeamId), eq(postId), eq(testCategoryId));
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {})
    @DisplayName("게시글 삭제 실패(권한도 없고 작성자 본인도 아닌 경우)")
    void deletePost_forbidden() throws Exception {
        // Given
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();
        Long postId = 1L;

        // 권한 없음 예외 발생 Mock 설정
        doThrow(new AccessDeniedException("게시글을 삭제할 권한이 없습니다."))
                .when(postService).deletePost(eq(testTeamId), eq(postId), eq(testCategoryId));

        // When & Then
        mockMvc.perform(delete("/teams/{teamId}/category/{categoryId}/posts/delete/{postId}",
                        testTeamId, testCategoryId, postId)
                        .with(user(testDataBuilder.getCurrentUserPrincipal()))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        // Mock 호출 검증
        verify(postService).deletePost(eq(testTeamId), eq(postId), eq(testCategoryId));
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {})
    @DisplayName("게시글 삭제 실패 - 게시글 존재하지 않음")
    void deletePost_postNotFound() throws Exception {
        // Given
        Long testTeamId = testDataBuilder.getCurrentTestTeamId();
        Long testCategoryId = testDataBuilder.getCurrentCategoryId();
        Long nonExistentPostId = 999L;

        // 게시글 없음 예외 발생 Mock 설정
        doThrow(new PostNotFoundException())
                .when(postService).deletePost(eq(testTeamId), eq(nonExistentPostId), eq(testCategoryId));

        // When & Then
        mockMvc.perform(delete("/teams/{teamId}/category/{categoryId}/posts/delete/{postId}",
                        testTeamId, testCategoryId, nonExistentPostId)
                        .with(user(testDataBuilder.getCurrentUserPrincipal()))
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(postService).deletePost(eq(testTeamId), eq(nonExistentPostId), eq(testCategoryId));
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"VIEW_POST"})
    @DisplayName("팀 최근 게시글 조회 성공")
    void getRecentPosts_success() throws Exception {
        // Given
        Long teamId = testDataBuilder.getCurrentTestTeamId();

        // Mock 응답 데이터 생성
        PostListResponse post1 = new PostListResponse(
                1L,
                "첫 번째 게시글",
                "테스트사용자1",
                teamId,
                "공지사항",
                1L,
                10,
                LocalDateTime.now().minusHours(1),
                3L
        );

        PostListResponse post2 = new PostListResponse(
                2L,
                "두 번째 게시글",
                "테스트사용자2",
                teamId,
                "자유게시판",
                2L,
                5,
                LocalDateTime.now().minusHours(2),
                1L
        );

        // Page 객체 생성
        Pageable pageable = PageRequest.of(0, 20, Sort.by(Sort.Direction.DESC, "createdDate"));
        List<PostListResponse> postList = Arrays.asList(post1, post2);
        Page<PostListResponse> postPage = new PageImpl<>(postList, pageable, postList.size());

        // TeamRecentPostsResponse 생성
        TeamRecentPostsResponse mockResponse = new TeamRecentPostsResponse("테스트 팀", postPage);

        // PostService Mock 설정
        given(postService.getRecentPosts(eq(teamId), any(Pageable.class)))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/teams/{teamId}/recent", teamId)
                        .param("page", "0")
                        .param("size", "20"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.teamName").value("테스트 팀"))
                .andExpect(jsonPath("$.posts.content").isArray())
                .andExpect(jsonPath("$.posts.content", hasSize(2)))
                .andExpect(jsonPath("$.posts.content[0].title").value("첫 번째 게시글"))
                .andExpect(jsonPath("$.posts.content[0].authorName").value("테스트사용자1"))
                .andExpect(jsonPath("$.posts.content[1].title").value("두 번째 게시글"))
                .andExpect(jsonPath("$.posts.totalElements").value(2))
                .andExpect(jsonPath("$.posts.size").value(20))
                .andExpect(jsonPath("$.posts.number").value(0));

        // Mock 호출 검증
        verify(postService).getRecentPosts(eq(teamId), any(Pageable.class));
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"VIEW_POST"})
    @DisplayName("팀 최근 게시글 조회 성공 - 커스텀 Pageable")
    void getRecentPosts_withCustomPageable_success() throws Exception {
        // Given
        Long teamId = testDataBuilder.getCurrentTestTeamId();

        PostListResponse post = new PostListResponse(
                1L, "게시글", "작성자", teamId, "카테고리", 1L, 0, LocalDateTime.now(), 0L
        );

        // 커스텀 페이지 설정 (page=1, size=5)
        Pageable customPageable = PageRequest.of(1, 5, Sort.by(Sort.Direction.DESC, "createdDate"));
        Page<PostListResponse> postPage = new PageImpl<>(List.of(post), customPageable, 10);

        TeamRecentPostsResponse mockResponse = new TeamRecentPostsResponse("테스트 팀", postPage);

        given(postService.getRecentPosts(eq(teamId), any(Pageable.class)))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/teams/{teamId}/recent", teamId)
                        .param("page", "1")    // 2페이지
                        .param("size", "5")    // 페이지당 5개
                        .param("sort", "createdDate,desc"))  // 정렬
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value("테스트 팀"))
                .andExpect(jsonPath("$.posts.number").value(1))  // 현재 페이지
                .andExpect(jsonPath("$.posts.size").value(5))    // 페이지 크기
                .andExpect(jsonPath("$.posts.totalElements").value(10));  // 전체 요소 수
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"VIEW_POST"})
    @DisplayName("팀 최근 게시글 조회 실패 - 팀 존재하지 않음")
    void getRecentPosts_teamNotFound() throws Exception {
        // Given
        Long invalidTeamId = 999L;

        // 팀이 존재하지 않을 때 예외 발생
        given(postService.getRecentPosts(eq(invalidTeamId), any(Pageable.class)))
                .willThrow(new TeamNotFoundException("team not found"));

        // When & Then
        mockMvc.perform(get("/teams/{teamId}/recent", invalidTeamId))
                .andExpect(status().isNotFound());

        verify(postService).getRecentPosts(eq(invalidTeamId), any(Pageable.class));
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"VIEW_POST"})
    @DisplayName("팀 최근 게시글 조회 - 잘못된 페이지 파라미터")
    void getRecentPosts_invalidPageParams() throws Exception {
        // Given
        Long teamId = testDataBuilder.getCurrentTestTeamId();

        // 음수 페이지도 처리 가능한 Mock 설정
        Page<PostListResponse> emptyPage = new PageImpl<>(Collections.emptyList(),
                PageRequest.of(0, 20), 0);
        TeamRecentPostsResponse mockResponse = new TeamRecentPostsResponse("테스트 팀", emptyPage);

        given(postService.getRecentPosts(eq(teamId), any(Pageable.class)))
                .willReturn(mockResponse);

        // When & Then - 음수 페이지는 0으로 처리됨
        mockMvc.perform(get("/teams/{teamId}/recent", teamId)
                        .param("page", "-1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.teamName").value("테스트 팀"));
    }
}
