package com.example.board.comment;

import com.example.board.category.TeamCategory;
import com.example.board.comment.dto.CommentResponse;
import com.example.board.comment.dto.CreateCommentRequest;
import com.example.board.permission.CategoryPermission;
import com.example.board.post.Post;
import com.example.board.role.TeamRole;
import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

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
        CommentResponse response = commentService.createComment(member2, testPost.getId(), request);

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
        CommentResponse parentResponse = commentService.createComment(member1, testPost.getId(), parentRequest);

        CreateCommentRequest replyRequest = new CreateCommentRequest("대댓글입니다", parentResponse.id());
        // DB에 즉시 반영
        entityManager.flush();
        entityManager.clear(); // 영속성 컨텍스트 초기화
        // when
        CommentResponse replyResponse = commentService.createComment(member2, testPost.getId(), replyRequest);

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

}
