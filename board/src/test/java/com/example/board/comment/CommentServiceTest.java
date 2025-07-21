package com.example.board.comment;

import com.example.board.category.TeamCategory;
import com.example.board.comment.dto.CommentResponse;
import com.example.board.comment.dto.CreateCommentRequest;
import com.example.board.comment.dto.MemberCommentResponse;
import com.example.board.member.Member;
import com.example.board.permission.CategoryPermission;
import com.example.board.post.Post;
import com.example.board.role.TeamRole;
import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class CommentServiceTest extends AbstractTestSupport {
    @Autowired
    private CommentService commentService;
    @Autowired
    private TestDataBuilder testDataBuilder;
    private Team testTeam;
    private TeamMember teamMember;
    private TeamCategory testCategory;
    private TeamRole role;
    private Post testPost;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void init() {
        testTeam = testDataBuilder.createTeam(member1);
        teamMember = testDataBuilder.addMemberToTeam(member2, testTeam.getId());
        testCategory = testDataBuilder.createCategory(testTeam.getBasicRoleId(), testTeam.getId(), "Test Category", new HashSet<>(List.of(CategoryPermission.CREATE_COMMENT)));
        testPost = testDataBuilder.createPost("Test Post", "Test Content", member2, testCategory, testTeam, teamMember);
        role = testDataBuilder.createNewRole(testTeam.getId(), "test role");
    }

    @Test
    @DisplayName("댓글 생성 성공 - 일반 댓글")
    void createComment_success() {
        // given
        CreateCommentRequest request = new CreateCommentRequest("새로운 댓글 내용", null);

        // when
        CommentResponse response = commentService.createComment(member2, testCategory.getId(), testPost.getId(), request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.content()).isEqualTo("새로운 댓글 내용");
        assertThat(response.authorName()).isEqualTo(teamMember.getTeamNickname());
        assertThat(response.replies()).isEmpty();

        // 실제 DB에 저장되었는지 확인
        Optional<Comment> savedComment = commentRepository.findById(response.id());
        assertThat(savedComment).isPresent();
        assertThat(savedComment.get().getContent()).isEqualTo("새로운 댓글 내용");
        assertThat(savedComment.get().getAuthor()).isEqualTo(member2);
        assertThat(savedComment.get().getPost()).isEqualTo(testPost);
    }
    
    @Test
    @DisplayName("댓글 생성 성공 - 대댓글")
    void createComment_withParent_success() {
        // given - 부모 댓글 먼저 생성
        CreateCommentRequest parentRequest = new CreateCommentRequest("부모 댓글", null);
        CommentResponse parentResponse = commentService.createComment(member1, testCategory.getId(), testPost.getId(), parentRequest);

        CreateCommentRequest replyRequest = new CreateCommentRequest("대댓글입니다", parentResponse.id());
        // DB에 즉시 반영
        entityManager.flush();
        entityManager.clear(); // 영속성 컨텍스트 초기화
        // when
        CommentResponse replyResponse = commentService.createComment(member2,testCategory.getId(), testPost.getId(), replyRequest);

        // then
        assertThat(replyResponse).isNotNull();
        assertThat(replyResponse.content()).isEqualTo("대댓글입니다");
        assertThat(replyResponse.authorId()).isEqualTo(member2.getMemberId());
        assertThat(replyResponse.authorName()).isEqualTo(teamMember.getTeamNickname());
        assertThat(replyResponse.isDeleted()).isFalse();

        // 실제 DB에서 부모-자식 관계 확인
        Comment savedReply = commentRepository.findById(replyResponse.id()).orElseThrow();
        assertThat(savedReply.getParent()).isNotNull();
        assertThat(savedReply.getParent().getId()).isEqualTo(parentResponse.id());

        // 부모 댓글에서 replies 확인
        Comment savedParent = commentRepository.findById(parentResponse.id()).orElseThrow();
        assertThat(savedParent.getReplies()).hasSize(1);
        assertThat(savedParent.getReplies().get(0).getId()).isEqualTo(replyResponse.id());
    }

    @Test
    @DisplayName("댓글 생성 성공 - 여러 대댓글")
    void createComment_multipleReplies_success() {
        // given - 부모 댓글 생성
        CreateCommentRequest parentRequest = new CreateCommentRequest("부모 댓글", null);
        CommentResponse parentResponse = commentService.createComment(member2, testCategory.getId(), testPost.getId(), parentRequest);

        // when - 여러 대댓글 생성
        CreateCommentRequest reply1Request = new CreateCommentRequest("첫 번째 대댓글", parentResponse.id());
        CreateCommentRequest reply2Request = new CreateCommentRequest("두 번째 대댓글", parentResponse.id());
        // DB에 즉시 반영
        entityManager.flush();
        entityManager.clear(); // 영속성 컨텍스트 초기화

        CommentResponse reply1 = commentService.createComment(member1, testCategory.getId(), testPost.getId(), reply1Request);
        CommentResponse reply2 = commentService.createComment(member2, testCategory.getId(), testPost.getId(), reply2Request);

        // then - 부모 댓글의 replies 확인
        CommentResponse updatedParent = CommentResponse.from(
                commentRepository.findById(parentResponse.id()).orElseThrow());

        assertThat(updatedParent.replies()).hasSize(2);
        assertThat(updatedParent.replies())
                .extracting(CommentResponse::content)
                .containsExactlyInAnyOrder("첫 번째 대댓글", "두 번째 대댓글");
        assertThat(updatedParent.replies())
                .extracting(CommentResponse::authorId)
                .containsExactlyInAnyOrder(member1.getMemberId(), member2.getMemberId());
    }

    @Test
    @DisplayName("댓글 생성 실패 - 존재하지 않는 게시글")
    void createComment_postNotFound_throwsException() {
        // given
        CreateCommentRequest request = new CreateCommentRequest("댓글 내용", null);
        Long nonExistentPostId = 999L;

        // when & then
        assertThatThrownBy(() -> commentService.createComment(member2, testCategory.getId(), nonExistentPostId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("post not found");
    }

    @Test
    @DisplayName("댓글 생성 실패 - 댓글 작성 권한 없음")
    void createComment_noPermission_throwsException() {
        // given - 권한이 없는 카테고리 생성
        TeamCategory noPermissionCategory = testDataBuilder.createCategory(
                testTeam.getId(),
                "No Permission Category",
                Set.of() // CREATE_COMMENT 권한 없음
        );

        Post noPermissionPost = testDataBuilder.createPost(
                "No Permission Post",
                "Content",
                member1,
                noPermissionCategory,
                testTeam,
                teamMember
        );

        CreateCommentRequest request = new CreateCommentRequest("댓글 내용", null);

        // when & then
        assertThatThrownBy(() -> commentService.createComment(member2, testCategory.getId(), noPermissionPost.getId(), request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("댓글을 작성할 권한이 없습니다.");
    }

    @Test
    @DisplayName("댓글 생성 실패 - 존재하지 않는 부모 댓글")
    void createComment_parentNotFound_throwsException() {
        // given
        CreateCommentRequest request = new CreateCommentRequest("대댓글 내용", 999L);

        // when & then
        assertThatThrownBy(() -> commentService.createComment(member2, testCategory.getId(), testPost.getId(), request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Parent comment not found");
    }

    @Test
    @DisplayName("댓글 생성 실패 - 팀 멤버가 아님")
    void createComment_notTeamMember_throwsException() {
        // given - 다른 팀에 속하지 않은 멤버
        Member nonTeamMember = testDataBuilder.createMember("nonteam@example.com", "nonteamuser", "password");
        CreateCommentRequest request = new CreateCommentRequest("댓글 내용", null);

        // when & then
        assertThatThrownBy(() -> commentService.createComment(nonTeamMember, testCategory.getId(), testPost.getId(), request))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not a member of this team!!");
    }

    @Test
    @DisplayName("댓글 삭제 성공 - 작성자가 삭제")
    void deleteComment_byAuthor_success() {
        // given - 댓글 생성
        CreateCommentRequest request = new CreateCommentRequest("삭제할 댓글", null);
        CommentResponse createdComment = commentService.createComment(member2, testCategory.getId(), testPost.getId(), request);

        // when
        commentService.deleteComment(createdComment.id(), member2);

        // then - 댓글이 삭제 처리되었는지 확인
        Comment deletedComment = commentRepository.findById(createdComment.id()).orElseThrow();

        // CommentResponse로 변환하여 isDeleted 확인
        CommentResponse deletedResponse = CommentResponse.from(deletedComment);
        assertThat(deletedResponse.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("댓글 삭제 성공 - 관리자 권한으로 삭제")
    void deleteComment_byAdmin_success() {
        // given
        //member2의 댓글을 member1(팀의 관리자)이 삭제
        CreateCommentRequest request = new CreateCommentRequest("다른 사용자 댓글", null);
        CommentResponse otherUserComment = commentService.createComment(member2, testCategory.getId(), testPost.getId(), request);

        // when - member2가 member1의 댓글 삭제
        commentService.deleteComment(otherUserComment.id(), member1);

        // then
        Comment deletedComment = commentRepository.findById(otherUserComment.id()).orElseThrow();
        CommentResponse deletedResponse = CommentResponse.from(deletedComment);
        assertThat(deletedResponse.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 존재하지 않는 댓글")
    void deleteComment_commentNotFound_throwsException() {
        // given
        Long nonExistentCommentId = 999L;

        // when & then
        assertThatThrownBy(() -> commentService.deleteComment(nonExistentCommentId, member2))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Comment not found");
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 권한 없음")
    void deleteComment_noPermission_throwsException() {
        // given - 권한이 없는 사용자
        Member noPermissionMember = testDataBuilder.createMember("noperm@example.com", "noperm", "password");
        testDataBuilder.addMemberToTeam(noPermissionMember, testTeam.getId()); // 기본 권한만

        CreateCommentRequest request = new CreateCommentRequest("삭제할 수 없는 댓글", null);
        CommentResponse createdComment = commentService.createComment(member2, testCategory.getId(), testPost.getId(), request);

        // when & then - 다른 사용자가 삭제 시도 (작성자도 아니고 권한도 없음)
        assertThatThrownBy(() -> commentService.deleteComment(createdComment.id(), noPermissionMember))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("댓글을 삭제할 권한이 없습니다.");
    }

    @Test
    @DisplayName("댓글 삭제 - 대댓글이 있는 댓글 삭제")
    void deleteComment_withReplies_success() {
        // given - 부모 댓글과 대댓글 생성
        CreateCommentRequest parentRequest = new CreateCommentRequest("부모 댓글", null);
        CommentResponse parentComment = commentService.createComment(member2, testCategory.getId(), testPost.getId(), parentRequest);

        CreateCommentRequest replyRequest = new CreateCommentRequest("대댓글", parentComment.id());
        CommentResponse replyComment = commentService.createComment(member1, testCategory.getId(), testPost.getId(), replyRequest);

        // when - 부모 댓글 삭제
        commentService.deleteComment(parentComment.id(), member2);

        // then - 부모 댓글이 삭제 처리되었는지 확인
        Comment deletedParent = commentRepository.findById(parentComment.id()).orElseThrow();
        CommentResponse deletedResponse = CommentResponse.from(deletedParent);
        assertThat(deletedResponse.isDeleted()).isTrue();

        // 대댓글은 영향받지 않음
        Comment unaffectedReply = commentRepository.findById(replyComment.id()).orElseThrow();
        CommentResponse replyResponse = CommentResponse.from(unaffectedReply);
        assertThat(replyResponse.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("게시글 댓글 조회 성공")
    void getCommentsInPost_success() {
        // given - 여러 댓글 생성
        CreateCommentRequest request1 = new CreateCommentRequest("첫 번째 댓글", null);
        CreateCommentRequest request2 = new CreateCommentRequest("두 번째 댓글", null);

        CommentResponse comment1 = commentService.createComment(member1, testCategory.getId(), testPost.getId(), request1);
        CommentResponse comment2 = commentService.createComment(member2, testCategory.getId(), testPost.getId(), request2);

        // when
        List<CommentResponse> comments = commentService.getCommentsInPost(testPost.getId());

        // then
        assertThat(comments).hasSize(2);
        assertThat(comments)
                .extracting(CommentResponse::content)
                .containsExactlyInAnyOrder("첫 번째 댓글", "두 번째 댓글");
        assertThat(comments)
                .extracting(CommentResponse::id)
                .containsExactlyInAnyOrder(comment1.id(), comment2.id());
        assertThat(comments)
                .extracting(CommentResponse::authorId)
                .containsExactlyInAnyOrder(member1.getMemberId(), member2.getMemberId());
        assertThat(comments)
                .extracting(CommentResponse::isDeleted)
                .containsOnly(false);
    }

    @Test
    @DisplayName("게시글 댓글 조회 성공 - 대댓글 포함")
    void getCommentsInPost_withReplies_success() {
        // given - 부모 댓글과 대댓글 생성
        CreateCommentRequest parentRequest = new CreateCommentRequest("부모 댓글", null);
        CommentResponse parentComment = commentService.createComment(member1, testCategory.getId(), testPost.getId(), parentRequest);

        CreateCommentRequest replyRequest1 = new CreateCommentRequest("첫 번째 대댓글", parentComment.id());
        CreateCommentRequest replyRequest2 = new CreateCommentRequest("두 번째 대댓글", parentComment.id());

        CommentResponse reply1 = commentService.createComment(member2, testCategory.getId(), testPost.getId(), replyRequest1);
        CommentResponse reply2 = commentService.createComment(member1, testCategory.getId(), testPost.getId(), replyRequest2);
        // DB에 즉시 반영
        entityManager.flush();
        entityManager.clear(); // 영속성 컨텍스트 초기화
        // when - 최상위 댓글만 조회 (부모가 null인 댓글들)
        List<CommentResponse> comments = commentService.getCommentsInPost(testPost.getId());

        // then - 부모 댓글만 조회되어야 함
        assertThat(comments).hasSize(1);
        CommentResponse parentResponse = comments.get(0);
        assertThat(parentResponse.content()).isEqualTo("부모 댓글");
        assertThat(parentResponse.authorId()).isEqualTo(member1.getMemberId());

        // 대댓글들이 replies에 포함되어 있는지 확인
        assertThat(parentResponse.replies()).hasSize(2);
        assertThat(parentResponse.replies())
                .extracting(CommentResponse::content)
                .containsExactlyInAnyOrder("첫 번째 대댓글", "두 번째 대댓글");
        assertThat(parentResponse.replies())
                .extracting(CommentResponse::authorId)
                .containsExactlyInAnyOrder(member1.getMemberId(), member2.getMemberId());
    }

    @Test
    @DisplayName("게시글 댓글 조회 성공 - 댓글 없음")
    void getCommentsInPost_noComments_success() {
        // given - 댓글이 없는 새 게시글 생성
        Post emptyPost = testDataBuilder.createPost(
                "Empty Post",
                "No Comments",
                member1,
                testCategory,
                testTeam,
                teamMember
        );

        // when
        List<CommentResponse> comments = commentService.getCommentsInPost(emptyPost.getId());

        // then
        assertThat(comments).isEmpty();
    }

    @Test
    @DisplayName("게시글 댓글 조회 실패 - 존재하지 않는 게시글")
    void getCommentsInPost_postNotFound_throwsException() {
        // given
        Long nonExistentPostId = 999L;

        // when & then
        assertThatThrownBy(() -> commentService.getCommentsInPost(nonExistentPostId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("post not found");
    }

    @Test
    @DisplayName("게시글 댓글 조회 - 삭제된 댓글 포함")
    void getCommentsInPost_withDeletedComments_success() {
        // given - 댓글 생성 후 하나 삭제
        CreateCommentRequest request1 = new CreateCommentRequest("삭제될 댓글", null);
        CreateCommentRequest request2 = new CreateCommentRequest("남을 댓글", null);

        CommentResponse comment1 = commentService.createComment(member1, testCategory.getId(), testPost.getId(), request1);
        CommentResponse comment2 = commentService.createComment(member2, testCategory.getId(), testPost.getId(), request2);

        commentService.deleteComment(comment1.id(), member1);

        // when
        List<CommentResponse> comments = commentService.getCommentsInPost(testPost.getId());

        // then - 삭제된 댓글도 포함하여 반환 (isDeleted = true)
        assertThat(comments).hasSize(2);

        CommentResponse deletedComment = comments.stream()
                .filter(c -> c.id().equals(comment1.id()))
                .findFirst()
                .orElseThrow();
        assertThat(deletedComment.isDeleted()).isTrue();

        CommentResponse activeComment = comments.stream()
                .filter(c -> c.id().equals(comment2.id()))
                .findFirst()
                .orElseThrow();
        assertThat(activeComment.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("팀 멤버 댓글 조회 성공")
    void findCommentsByTeamAndMember_success() {
        // given - 여러 게시글에 댓글 생성
        Post post1 = testDataBuilder.createPost("Post 1", "Content 1", member2, testCategory, testTeam, teamMember);
        Post post2 = testDataBuilder.createPost("Post 2", "Content 2", member2, testCategory, testTeam, teamMember);

        CreateCommentRequest request1 = new CreateCommentRequest("첫 번째 댓글", null);
        CreateCommentRequest request2 = new CreateCommentRequest("두 번째 댓글", null);
        CreateCommentRequest request3 = new CreateCommentRequest("세 번째 댓글", null);

        commentService.createComment(member2, testCategory.getId(), post1.getId(), request1);
        commentService.createComment(member2, testCategory.getId(), post1.getId(), request2);
        commentService.createComment(member2, testCategory.getId(), post2.getId(), request3);

        // 다른 사용자의 댓글도 생성 (결과에 포함되지 않아야 함)
        CreateCommentRequest otherRequest = new CreateCommentRequest("다른 사용자 댓글", null);
        commentService.createComment(member1, testCategory.getId(), post1.getId(), otherRequest);

        // when
        Page<MemberCommentResponse> result = commentService.findCommentsByTeamAndMember(
                teamMember.getId(), 0, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3L);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);

        // 댓글 내용 확인
        List<String> commentContents = result.getContent().stream()
                .map(MemberCommentResponse::content)
                .toList();
        assertThat(commentContents).containsExactlyInAnyOrder("첫 번째 댓글", "두 번째 댓글", "세 번째 댓글");
    }

    @Test
    @DisplayName("팀 멤버 댓글 조회 성공 - 댓글 없음")
    void findCommentsByTeamAndMember_noComments_success() {
        // given - 새로운 팀 멤버 (댓글 없음)
        Member newMember = testDataBuilder.createMember("new@example.com", "newuser", "password");
        TeamMember newTeamMember = testDataBuilder.addMemberToTeam(newMember, testTeam.getId());

        // when
        Page<MemberCommentResponse> result = commentService.findCommentsByTeamAndMember(
                newTeamMember.getId(), 0, 10);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0L);
        assertThat(result.getTotalPages()).isEqualTo(0);
    }
}
