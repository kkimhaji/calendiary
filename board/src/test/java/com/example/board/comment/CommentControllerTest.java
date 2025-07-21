package com.example.board.comment;

import com.example.board.auth.UserPrincipal;
import com.example.board.category.TeamCategory;
import com.example.board.comment.dto.CommentResponse;
import com.example.board.comment.dto.CreateCommentRequest;
import com.example.board.config.security.WithMockCategoryPermission;
import com.example.board.permission.CategoryPermission;
import com.example.board.post.Post;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CommentControllerTest extends AbstractControllerTestSupport {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private TestDataBuilder testDataBuilder;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CommentService commentService;

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"CREATE_COMMENT"})
    @DisplayName("댓글 생성 성공 - 일반 댓글")
    void createComment_success() throws Exception {
        // given
        CreateCommentRequest request = new CreateCommentRequest("새로운 댓글 내용", null);
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();

        Long categoryId = testDataBuilder.getCurrentCategoryId();
        Long teamId = testDataBuilder.getCurrentTestTeamId();
        Post testPost = testDataBuilder.createPost("test post", "test content", categoryId, teamId);

        // when & then
        mockMvc.perform(post("/category/{categoryId}/posts/{postId}/comments",
                        categoryId, testPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("새로운 댓글 내용"))
                .andExpect(jsonPath("$.authorName").exists())
                .andExpect(jsonPath("$.createdDate").exists())
                .andExpect(jsonPath("$.isDeleted").value(false))
                .andExpect(jsonPath("$.replies").isArray())
                .andExpect(jsonPath("$.replies").isEmpty());

        // DB에 실제로 저장되었는지 확인
        List<Comment> savedComments = commentRepository.findByPostIdAndParentIsNull(testPost.getId());
        assertThat(savedComments).hasSize(1);
        assertThat(savedComments.get(0).getContent()).isEqualTo("새로운 댓글 내용");
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"CREATE_COMMENT"})
    @DisplayName("댓글 생성 성공 - 대댓글")
    void createComment_reply_success() throws Exception {
        // given - 부모 댓글 먼저 생성
        CreateCommentRequest parentRequest = new CreateCommentRequest("부모 댓글", null);
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        Long categoryId = testDataBuilder.getCurrentCategoryId();
        Long teamId = testDataBuilder.getCurrentTestTeamId();
        Post testPost = testDataBuilder.createPost("test post", "test content", categoryId, teamId);

        MvcResult parentResult = mockMvc.perform(post("/category/{categoryId}/posts/{postId}/comments",
                        teamId, categoryId, testPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(parentRequest))
                        .with(user(userPrincipal))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        CommentResponse parentComment = objectMapper.readValue(
                parentResult.getResponse().getContentAsString(), CommentResponse.class);

        // 대댓글 생성
        CreateCommentRequest replyRequest = new CreateCommentRequest("대댓글입니다", parentComment.id());

        // when & then
        mockMvc.perform(post("/category/{categoryId}/posts/{postId}/comments",
                        teamId, categoryId, testPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(replyRequest))
                        .with(user(userPrincipal))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("대댓글입니다"));
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {}) // CREATE_COMMENT 권한 없음
    @DisplayName("댓글 생성 실패 - 권한 없음")
    void createComment_noPermission_forbidden() throws Exception {
        // given
        CreateCommentRequest request = new CreateCommentRequest("권한 없는 댓글", null);
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        Long categoryId = testDataBuilder.getCurrentCategoryId();
        Long teamId = testDataBuilder.getCurrentTestTeamId();
        Post testPost = testDataBuilder.createPost("test post", "test content", categoryId, teamId);

        // when & then
        mockMvc.perform(post("/category/{categoryId}/posts/{postId}/comments",
                        teamId, categoryId, testPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal))
                        .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("댓글 생성 실패 - 인증되지 않은 사용자")
    void createComment_unauthenticated_unauthorized() throws Exception {
        // given
        CreateCommentRequest request = new CreateCommentRequest("인증 없는 댓글", null);
        Team testTeam = testDataBuilder.createTeam(member1);
        Long teamId = testTeam.getId();
        TeamMember teamMember = testDataBuilder.addMemberToTeam(member2, teamId);

        TeamCategory testCategory = testDataBuilder.createCategory(teamId, "test category", new HashSet<>(List.of(CategoryPermission.CREATE_COMMENT)));
        Long categoryId = testCategory.getId();

        Post testPost = testDataBuilder.createPost("test post", "test content", member2, categoryId, teamId, teamMember);
        // when & then
        mockMvc.perform(post("/category/{categoryId}/posts/{postId}/comments",
                        teamId, categoryId, testPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"CREATE_COMMENT"})
    @DisplayName("댓글 삭제 성공 - 작성자가 삭제")
    void deleteComment_byAuthor_success() throws Exception {
        // given - 댓글 생성
        CreateCommentRequest request = new CreateCommentRequest("삭제할 댓글", null);
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        Long categoryId = testDataBuilder.getCurrentCategoryId();
        Long teamId = testDataBuilder.getCurrentTestTeamId();
        Post testPost = testDataBuilder.createPost("test post", "test content", categoryId, teamId);

        MvcResult createResult = mockMvc.perform(post("/category/{categoryId}/posts/{postId}/comments",
                        teamId, categoryId, testPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userPrincipal))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        CommentResponse createdComment = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), CommentResponse.class);

        // when & then
        mockMvc.perform(delete("/category/{categoryId}/posts/{postId}/comments/{commentId}",
                        teamId, categoryId, testPost.getId(), createdComment.id())
                        .with(user(userPrincipal))
                        .with(csrf()))
                .andExpect(status().isOk());

        // DB에서 삭제 확인
        Comment deletedComment = commentRepository.findById(createdComment.id()).orElseThrow();
        assertThat(deletedComment.isDeleted()).isTrue();
    }

    @Test
    @WithMockCategoryPermission(categoryPermissions = {"CREATE_COMMENT"})
    @DisplayName("게시글 댓글 조회 성공")
    void getCommentsInPost_success() throws Exception {
        // given - 댓글들 생성
        CreateCommentRequest request1 = new CreateCommentRequest("첫 번째 댓글", null);
        CreateCommentRequest request2 = new CreateCommentRequest("두 번째 댓글", null);
        UserPrincipal userPrincipal = testDataBuilder.getCurrentUserPrincipal();
        Long categoryId = testDataBuilder.getCurrentCategoryId();
        Long teamId = testDataBuilder.getCurrentTestTeamId();
        Post testPost = testDataBuilder.createPost("test post", "test content", categoryId, teamId);

        mockMvc.perform(post("/category/{categoryId}/posts/{postId}/comments",
                        teamId, categoryId, testPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request1))
                        .with(user(userPrincipal))
                        .with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/category/{categoryId}/posts/{postId}/comments",
                        teamId, categoryId, testPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request2))
                        .with(user(userPrincipal))
                        .with(csrf()))
                .andExpect(status().isOk());

        // when & then
        mockMvc.perform(get("/category/{categoryId}/posts/{postId}/comments",
                        teamId, categoryId, testPost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].content").value(containsInAnyOrder("첫 번째 댓글", "두 번째 댓글")));
    }

    @Test
    @DisplayName("게시글 댓글 조회 성공 - 댓글 없음")
    @WithMockCategoryPermission
    void getCommentsInPost_noComments_success() throws Exception {
        Long categoryId = testDataBuilder.getCurrentCategoryId();
        Long teamId = testDataBuilder.getCurrentTestTeamId();
        Post testPost = testDataBuilder.createPost("test post", "test content", categoryId, teamId);

        // when & then
        mockMvc.perform(get("/category/{categoryId}/posts/{postId}/comments",
                        teamId, categoryId, testPost.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}