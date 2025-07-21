package com.example.board.post;

import com.example.board.category.TeamCategory;
import com.example.board.comment.Comment;
import com.example.board.config.HtmlSanitizer;
import com.example.board.config.security.WithMockCategoryPermission;
import com.example.board.permission.PermissionService;
import com.example.board.post.dto.*;
import com.example.board.role.TeamRole;
import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.support.TestDataFactory;
import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

public class PostServiceTest extends AbstractTestSupport {
    @Autowired
    private PostService postService;
    @Autowired
    private TestDataBuilder builder;
    @Autowired
    private TestDataFactory factory;
    private Team testTeam;
    private TeamMember teamMember;
    private TeamCategory testCategory;
    private TeamRole role;
    private Post testPost;
    private Pageable testPageable;
    @Autowired
    private HtmlSanitizer htmlSanitizer;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private ImageService imageService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private PermissionService permissionService;

    @BeforeEach
    void init() {
        testTeam = builder.createTeam(member1);
        teamMember = builder.addMemberToTeam(member2, testTeam.getId());
        role = builder.createNewRole(testTeam.getId(), "test role");
        testCategory = builder.createCategory(role.getId(), testTeam.getId(), "Test Category", new HashSet<>());
        testPost = builder.createPost("Test Post", "Test Content", member2, testCategory, testTeam, teamMember);
        testPageable = PageRequest.of(0, 10, Sort.by("createdDate").descending());
        postService.clearViewCountCache();
    }

    @Test
    void getPostDetailTest_success() {
        // given
        Long postId = testPost.getId();

        // when
        PostDetailDTO result = postService.getPostDetail(postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(postId);
        assertThat(result.title()).isEqualTo("Test Post");
        assertThat(result.author().id()).isEqualTo(member2.getMemberId());

        Optional<Post> foundPost = postRepository.findById(postId);
        assertThat(foundPost).isPresent();
        assertThat(foundPost.get().getTitle()).isEqualTo("Test Post");
    }

    @Test
    void getPostDetail_notFound() {
        // given
        Long nonExistentPostId = 999L;

        // 실제 DB에 해당 ID가 없는지 확인
        assertThat(postRepository.findById(nonExistentPostId)).isEmpty();

        // when & then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                postService.getPostDetail(nonExistentPostId)
        );

        assertThat(exception.getMessage()).isEqualTo("Post not found");
    }

    @Test
    @DisplayName("게시글 상세 조회 성공 - 조회수 증가 확인")
    void getPostDetailTest_viewCountIncrease() {
        // given
        Long postId = testPost.getId();
        int initialViewCount = testPost.getViewCount();

        // when
        PostDetailDTO result = postService.getPostDetail(postId);

        // then
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(postId);

        // 조회수 증가는 비동기 처리이므로 캐시에서 확인
        // (실제 구현에 따라 조정 필요)
        assertThat(postService.getCachedViewCount(postId)).isEqualTo(1);
    }

    @Test
    @DisplayName("여러 게시글 동시 조회 테스트")
    void getPostDetail_multiplePost() {
        // given
        Post anotherPost = builder.createPost(
                "Another Post",
                "Another Content",
                member2,
                testCategory,
                testTeam,
                teamMember
        );

        // when
        PostDetailDTO result1 = postService.getPostDetail(testPost.getId());
        PostDetailDTO result2 = postService.getPostDetail(anotherPost.getId());

        // then
        assertThat(result1.title()).isEqualTo("Test Post");
        assertThat(result2.title()).isEqualTo("Another Post");

        // 실제 DB에서 두 게시글 모두 존재하는지 확인
        assertThat(postRepository.findById(testPost.getId())).isPresent();
        assertThat(postRepository.findById(anotherPost.getId())).isPresent();
        assertThat(postRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("게시글 조회 후 DB 상태 일관성 확인")
    void getPostDetail_databaseConsistency() {
        // given
        Long postId = testPost.getId();

        // when
        PostDetailDTO result = postService.getPostDetail(postId);

        // then - DTO와 실제 DB 데이터 일치 확인
        Post actualPost = postRepository.findById(postId).orElseThrow();

        assertThat(result.id()).isEqualTo(actualPost.getId());
        assertThat(result.title()).isEqualTo(actualPost.getTitle());
        assertThat(result.content()).isEqualTo(actualPost.getContent());
        assertThat(result.author().id()).isEqualTo(actualPost.getAuthor().getMemberId());
        assertThat(result.categoryName()).isEqualTo(actualPost.getCategory().getName());
    }

    @Test
    @DisplayName("팀 최근 게시글 조회 성공")
    void getRecentPosts_success() {
        // given
        Post additionalPost = builder.createPost(
                "Additional Post",
                "Additional Content",
                member2,
                testCategory,
                testTeam,
                teamMember
        );

        // when
        TeamRecentPostsResponse result = postService.getRecentPosts(testTeam.getId(), testPageable);

        // then
        assertThat(result.teamName()).isEqualTo("testTeam");
        assertThat(result.posts().getContent()).hasSize(2);
        assertThat(result.posts().getContent())
                .extracting("title")
                .containsExactlyInAnyOrder("Test Post", "Additional Post");

        // 실제 DB 상태 검증
        List<Post> savedPosts = postRepository.findAll();
        assertThat(savedPosts).hasSize(2);
    }

    @Test
    @DisplayName("팀 최근 게시글 조회 실패 - 팀 없음")
    void getRecentPosts_teamNotFound() {
        // given
        Long nonExistentTeamId = 999L;

        // when & then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                postService.getRecentPosts(nonExistentTeamId, testPageable)
        );

        assertThat(exception.getMessage()).isEqualTo("team not found");
    }

    @Test
    @DisplayName("카테고리별 게시글 조회 성공")
    void getPostsByCategory_success() {
        // given
//        TeamCategory anotherCategory = builder.createCategory(testTeam, "Another Category");
        TeamCategory anotherCategory = builder.createCategory(role.getId(), testTeam.getId(), "Another Category", new HashSet<>());

        Post categoryPost = builder.createPost(
                "Category Post",
                "Category Content",
                member1,
                anotherCategory,
                testTeam,
                teamMember
        );

        // when
        CategoryRecentPostsResponse result = postService.getPostsByCategory(
                testTeam.getId(), testCategory.getId(), testPageable
        );

        // then
        assertThat(result.categoryName()).isEqualTo("Test Category");
        assertThat(result.posts().getContent()).hasSize(1);
        assertThat(result.posts().getContent().get(0).title()).isEqualTo("Test Post");

        // 실제 DB에서 카테고리별 분리 확인
        CategoryRecentPostsResponse anotherResult = postService.getPostsByCategory(
                testTeam.getId(), anotherCategory.getId(), testPageable
        );
        assertThat(anotherResult.posts().getContent()).hasSize(1);
        assertThat(anotherResult.posts().getContent().get(0).title()).isEqualTo("Category Post");

        // 실제 DB 상태 검증
        assertThat(postRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("카테고리별 게시글 조회 실패 - 카테고리 없음")
    void getPostsByCategory_categoryNotFound() {
        // given
        Long nonExistentCategoryId = 999L;

        // 실제 DB에 해당 카테고리가 없는지 확인
//        assertThat(categoryRepository.findById(nonExistentCategoryId)).isEmpty();

        // when & then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                postService.getPostsByCategory(testTeam.getId(), nonExistentCategoryId, testPageable)
        );

        assertThat(exception.getMessage()).isEqualTo("category not found");
    }

    @Test
    @DisplayName("카테고리별 게시글 조회 실패 - 팀 없음")
    void getPostsByCategory_teamNotFound() {
        // given
        Long nonExistentTeamId = 999L;

        // when & then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                postService.getPostsByCategory(nonExistentTeamId, testCategory.getId(), testPageable)
        );

        assertThat(exception.getMessage()).isEqualTo("team not found");
    }

    @Test
    @DisplayName("게시글 삭제 성공")
    @WithMockCategoryPermission(categoryPermissions = {"DELETE_POST"})
    void deletePost_success() throws IOException {
        // given
        Long teamId = builder.getCurrentTestTeamId();
        Long categoryId = builder.getCurrentCategoryId();
        testPost = builder.createPost("Test Post", "test", categoryId, teamId);
        Comment testComment = builder.createComment("Test Comment", testPost, member2, teamMember);

        Long postId = testPost.getId();
        assertDoesNotThrow(() -> postService.deletePost(postId, categoryId));
//        Long commentId = testComment.getId();

        // then - 실제 DB에서 삭제 확인
        assertThat(postRepository.findById(postId)).isEmpty();
//        assertThat(commentRepository.findById(commentId)).isEmpty();

        // 외부 서비스 호출 검증
//        verify(imageService).deleteAllPostImages(any(Post.class));
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 권한 없음")
    @WithMockCategoryPermission
    void deletePost_accessDenied() {
        Long postId = testPost.getId();

        // when & then
        AccessDeniedException exception = assertThrows(AccessDeniedException.class, () ->
                postService.deletePost(postId, testCategory.getId())
        );

        assertThat(exception.getMessage()).isEqualTo("게시글을 삭제할 권한이 없습니다.");

        // 실제로 DB에서 삭제되지 않았는지 확인
        assertThat(postRepository.findById(postId)).isPresent();
    }

    @Test
    @DisplayName("게시글 삭제 실패 - 게시글 없음")
    void deletePost_postNotFound() {
        // given
        Long nonExistentPostId = 999L;

        // 실제 DB에 해당 게시글이 없는지 확인
        assertThat(postRepository.findById(nonExistentPostId)).isEmpty();

        // when & then
        EntityNotFoundException exception = assertThrows(EntityNotFoundException.class, () ->
                postService.deletePost(nonExistentPostId, testCategory.getId())
        );

        assertThat(exception.getMessage()).isEqualTo("there is no such post");
    }

    @Test
    @DisplayName("제목·내용 변경 + 이미지 삭제 없는 경우")
    void updatePost_basic_success() throws Exception {
        // given
        UpdatePostRequestDTO dto = new UpdatePostRequestDTO(
                "수정된 제목", "수정된 내용", List.of());

        // when
        PostResponse response =
                postService.updatePost(testCategory.getId(), testPost.getId(), dto);

        // then
        Post updated = postRepository.findById(testPost.getId()).orElseThrow();

        assertThat(updated.getTitle()).isEqualTo("수정된 제목");
        assertThat(updated.getContent()).isEqualTo("수정된 내용");
        assertThat(response.title()).isEqualTo("수정된 제목");
    }
}
