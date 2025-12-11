package com.example.board.teamMember;

import com.example.board.category.TeamCategory;
import com.example.board.comment.Comment;
import com.example.board.comment.CommentRepository;
import com.example.board.common.exception.TeamNotFoundException;
import com.example.board.member.Member;
import com.example.board.post.Post;
import com.example.board.post.PostRepository;
import com.example.board.role.TeamRole;
import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.Team;
import com.example.board.team.TeamRepository;
import com.example.board.team.dto.TeamListDTO;
import com.example.board.teamMember.dto.MemberProfileResponse;
import com.example.board.teamMember.dto.RemoveMemberRequestDTO;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class TeamMemberServiceTest extends AbstractTestSupport {
    @Autowired
    private TeamMemberService teamMemberService;
    @Autowired
    private TestDataBuilder testDataBuilder;
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private CommentRepository commentRepository;
    private Team testTeam;
    private Member member3;
    private TeamRole adminRole;
    private TeamRole memberRole;
    private TeamMember teamMember1; // admin
    private TeamMember teamMember2; // member
    private TeamMember teamMember3; // member
    private TeamCategory testCategory;

    @BeforeEach
    void init(){
        member3 = testDataBuilder.createMember("thirdmember", "third");
        testTeam = testDataBuilder.createTeam(member1);
        Long teamId = testTeam.getId();
        teamMember1 = testDataBuilder.getTeamMember(teamId, member1.getMemberId());
        teamMember2 = testDataBuilder.addMemberToTeam(member2, teamId);
        teamMember3 = testDataBuilder.addMemberToTeam(member3, teamId);

        adminRole = testDataBuilder.getAdminRoleByTeam(testTeam);
        memberRole = testDataBuilder.getBasicRoleByTeam(testTeam);

        teamMember1.updateTeamNickname("관리자");
        teamMember2.updateTeamNickname("멤버2");
        teamMember3.updateTeamNickname("멤버3");

        testCategory = testDataBuilder.createCategory(
                adminRole.getId(),
                testTeam.getId(),
                "테스트 카테고리",
                new HashSet<>()
        );
    }

    @Test
    @DisplayName("사용자 역할 조회 성공")
    void getCurrentUserRole_success() {
        // when
        TeamRole result = teamMemberService.getCurrentUserRole(testTeam.getId(), member1);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(adminRole.getId());
        assertThat(result.getRoleName()).isEqualTo(adminRole.getRoleName());
    }

    @Test
    @DisplayName("역할 받아오기 - 팀 멤버가 아닌 경우 예외 발생")
    void getCurrentUserRole_notTeamMember_throwsException() {
        // given
        Member outsider = testDataBuilder.createMember("outsider@test.com", "outsider");

        // when & then
        assertThatThrownBy(() -> teamMemberService.getCurrentUserRole(testTeam.getId(), outsider))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not a member of this team!!");
    }

    @Test
    @DisplayName("존재하지 않는 팀의 경우")
    void getCurrentUserRole_teamNotFound_throwsException() {
        // when & then
        assertThatThrownBy(() -> teamMemberService.getCurrentUserRole(999L, member1))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage("team not found");
    }

    @Test
    @DisplayName("팀 닉네임 업데이트 성공")
    void updateTeamNickname_success() {
        // given
        String newNickname = "새로운닉네임";

        // when
        String result = teamMemberService.updateTeamNickname(testTeam.getId(), member2, newNickname);

        // then
        assertThat(result).isEqualTo(newNickname);

        // DB 확인
        TeamMember updatedMember = teamMemberRepository.findByTeamIdAndMember(testTeam.getId(), member2)
                .orElseThrow();
        assertThat(updatedMember.getTeamNickname()).isEqualTo(newNickname);
    }

    @Test
    @DisplayName("팀 멤버가 아닌 경우 예외 발생")
    void updateTeamNickname_notTeamMember_throwsException() {
        // given
        Member outsider = testDataBuilder.createMember("outsider@test.com", "outsider");

        // when & then
        assertThatThrownBy(() -> teamMemberService.updateTeamNickname(testTeam.getId(), outsider, "새닉네임"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("cannot find team member");
    }

    @Test
    @DisplayName("앞뒤 공백이 있는 닉네임 업데이트 - 공백 제거됨")
    void updateTeamNickname_withSpaces_trimmed() {
        // given
        String nicknameWithSpaces = "  새닉네임  ";
        String expectedNickname = "새닉네임";

        // when
        String result = teamMemberService.updateTeamNickname(testTeam.getId(), member2, nicknameWithSpaces);

        // then
        assertThat(result).isEqualTo(expectedNickname);

        TeamMember updatedMember = teamMemberRepository.findByTeamIdAndMember(testTeam.getId(), member2)
                .orElseThrow();
        assertThat(updatedMember.getTeamNickname()).isEqualTo(expectedNickname);
    }

    @Test
    @DisplayName("null 닉네임 업데이트 실패")
    void updateTeamNickname_nullNickname_throwsException() {
        // when & then
        assertThatThrownBy(() -> teamMemberService.updateTeamNickname(testTeam.getId(), member2, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 필수입니다.");
    }

    @Test
    @DisplayName("빈 문자열 닉네임 업데이트 실패")
    void updateTeamNickname_emptyNickname_throwsException() {
        // when & then
        assertThatThrownBy(() -> teamMemberService.updateTeamNickname(testTeam.getId(), member2, ""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 공백일 수 없습니다.");
    }

    @Test
    @DisplayName("공백만 있는 닉네임 업데이트 실패")
    void updateTeamNickname_whitespaceOnlyNickname_throwsException() {
        // when & then
        assertThatThrownBy(() -> teamMemberService.updateTeamNickname(testTeam.getId(), member2, "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 공백일 수 없습니다.");

        assertThatThrownBy(() -> teamMemberService.updateTeamNickname(testTeam.getId(), member2, "\t\n  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 공백일 수 없습니다.");
    }

    @Test
    @DisplayName("너무 긴 닉네임 업데이트 실패")
    void updateTeamNickname_tooLongNickname_throwsException() {
        // given
        String longNickname = "a".repeat(21); // 21자

        // when & then
        assertThatThrownBy(() -> teamMemberService.updateTeamNickname(testTeam.getId(), member2, longNickname))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("닉네임은 20자를 초과할 수 없습니다.");
    }

    @Test
    @DisplayName("최대 길이 닉네임 업데이트 성공")
    void updateTeamNickname_maxLengthNickname_success() {
        // given
        String maxLengthNickname = "a".repeat(20); // 20자

        // when
        String result = teamMemberService.updateTeamNickname(testTeam.getId(), member2, maxLengthNickname);

        // then
        assertThat(result).isEqualTo(maxLengthNickname);
    }

    @Test
    @DisplayName("멤버가 속한 팀 목록 조회 성공")
    void getTeams_success() {
        // given - 추가 팀 생성
        Team team2 = testDataBuilder.createTeam(member2);
        testDataBuilder.addMemberToTeam(member1, team2.getId());

        // when
        List<TeamListDTO> result = teamMemberService.getTeams(member1);

        // then
        assertThat(result).hasSizeGreaterThanOrEqualTo(2);

        List<Long> teamIds = result.stream().map(TeamListDTO::id).toList();
        assertThat(teamIds).contains(testTeam.getId(), team2.getId());
    }

    @Test
    @DisplayName("팀에 속하지 않은 멤버의 경우 빈 목록 반환")
    void getTeams_noTeams_returnsEmptyList() {
        // given
        Member loner = testDataBuilder.createMember("loner@test.com", "loner");

        // when
        List<TeamListDTO> result = teamMemberService.getTeams(loner);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("일반적인 이메일 마스킹")
    void emailMasking_normalEmail() {
        // given
        Member testMember = testDataBuilder.createMember("testuser@example.com", "test");
        TeamMember teamMember = testDataBuilder.addMemberToTeam(testMember, testTeam.getId());

        // when
        MemberProfileResponse result = teamMemberService.getTeamMemberProfile(teamMember.getId());

        // then
        assertThat(result.email()).isEqualTo("tes*****@example.com");
    }

    @Test
    @DisplayName("짧은 이메일 마스킹 (2글자)")
    void emailMasking_shortEmail() {
        // given
        Member testMember = testDataBuilder.createMember("ab@test.com", "test");
        TeamMember teamMember = testDataBuilder.addMemberToTeam(testMember, testTeam.getId());

        // when
        MemberProfileResponse result = teamMemberService.getTeamMemberProfile(teamMember.getId());

        // then
        assertThat(result.email()).isEqualTo("a*@test.com");
    }

    @Test
    @DisplayName("1글자 이메일 마스킹")
    void emailMasking_singleCharEmail() {
        // given
        Member testMember = testDataBuilder.createMember("a@test.com", "test");
        TeamMember teamMember = testDataBuilder.addMemberToTeam(testMember, testTeam.getId());

        // when
        MemberProfileResponse result = teamMemberService.getTeamMemberProfile(teamMember.getId());

        // then
        assertThat(result.email()).isEqualTo("a@test.com"); // 1글자는 마스킹 안됨
    }

    @Test
    @DisplayName("팀 멤버 강제 탈퇴 성공 - 게시글/댓글 삭제")
    void removeMember_deleteContent_success() {
        // given
        Post post = testDataBuilder.createPost(
                "테스트 게시글",
                "내용",
                member2,
                testCategory,
                testTeam,
                teamMember2
        );
        Comment comment = testDataBuilder.createComment(
                "테스트 댓글", post, member2, teamMember2
        );

        Long postId = post.getId();
        Long commentId = comment.getId();
        Long teamMemberId = teamMember2.getId();
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when
        teamMemberService.removeMember(testTeam.getId(), teamMemberId, request);

        // then
        assertThat(teamMemberRepository.findById(teamMemberId)).isEmpty();
        assertThat(postRepository.findById(postId)).isEmpty();
        assertThat(commentRepository.findById(commentId)).isEmpty();
    }

    @Test
    @DisplayName("팀 멤버 강제 탈퇴 성공 - 게시글/댓글 익명 처리")
    void removeMember_anonymizeContent_success() {
        // given
        Post post = testDataBuilder.createPost(
                "테스트 게시글",
                "내용",
                member2,
                testCategory,
                testTeam,
                teamMember2
        );
        Comment comment = testDataBuilder.createComment("테스트 댓글", post, member2, teamMember2);

        Long postId = post.getId();
        Long commentId = comment.getId();
        Long teamMemberId = teamMember2.getId();
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(false);

        // when
        teamMemberService.removeMember(testTeam.getId(), teamMemberId, request);

        // then
        assertThat(teamMemberRepository.findById(teamMemberId)).isEmpty();

        // 게시글과 댓글은 유지되고 작성자만 null
        Post updatedPost = postRepository.findById(postId).orElseThrow();
        assertThat(updatedPost.getTeamMember()).isNull();

        Comment updatedComment = commentRepository.findById(commentId).orElseThrow();
        assertThat(updatedComment.getTeamMember()).isNull();
    }

    @Test
    @DisplayName("팀 멤버 강제 탈퇴 실패 - 팀 소유자는 탈퇴 불가")
    void removeMember_ownerCannotBeRemoved_throwsException() {
        // given
        Long ownerTeamMemberId = teamMember1.getId();
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when & then
        assertThatThrownBy(() ->
                teamMemberService.removeMember(testTeam.getId(), ownerTeamMemberId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("팀 소유자는 강제 탈퇴시킬 수 없습니다.");

        // 소유자는 여전히 존재
        assertThat(teamMemberRepository.findById(ownerTeamMemberId)).isPresent();
    }

    @Test
    @DisplayName("팀 멤버 강제 탈퇴 실패 - 존재하지 않는 팀")
    void removeMember_teamNotFound_throwsException() {
        // given
        Long nonExistentTeamId = 999L;
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when & then
        assertThatThrownBy(() ->
                teamMemberService.removeMember(nonExistentTeamId, teamMember2.getId(), request))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage("team not found");
    }

    @Test
    @DisplayName("팀 멤버 강제 탈퇴 실패 - 존재하지 않는 멤버")
    void removeMember_memberNotFound_throwsException() {
        // given
        Long nonExistentMemberId = 999L;
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when & then
        assertThatThrownBy(() ->
                teamMemberService.removeMember(testTeam.getId(), nonExistentMemberId, request))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("team member not found");
    }

    @Test
    @DisplayName("팀 멤버 강제 탈퇴 실패 - 다른 팀의 멤버")
    void removeMember_memberFromDifferentTeam_throwsException() {
        // given
        Team anotherTeam = testDataBuilder.createTeam(member2);
        TeamMember anotherTeamMember = testDataBuilder.addMemberToTeam(member3, anotherTeam.getId());
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when & then
        assertThatThrownBy(() ->
                teamMemberService.removeMember(testTeam.getId(), anotherTeamMember.getId(), request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 멤버는 이 팀에 속하지 않습니다.");
    }

    @Test
    @DisplayName("팀 멤버 강제 탈퇴 - 여러 게시글/댓글 삭제")
    void removeMember_multipleContents_deleteAll_success() {
        // given
        Post post1 = testDataBuilder.createPost(
                "게시글1", "내용1", member2, testCategory, testTeam, teamMember2
        );
        Post post2 = testDataBuilder.createPost(
                "게시글2", "내용2", member2, testCategory, testTeam, teamMember2
        );
        Post post3 = testDataBuilder.createPost(
                "게시글3", "내용3", member2, testCategory, testTeam, teamMember2
        );

        Comment comment1 = testDataBuilder.createComment("댓글1", post1, member2, teamMember2);
        Comment comment2 = testDataBuilder.createComment("댓글2", post2, member2, teamMember2);
        Comment comment3 = testDataBuilder.createComment("댓글3", post3, member2, teamMember2);

        Long teamMemberId = teamMember2.getId();
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when
        teamMemberService.removeMember(testTeam.getId(), teamMemberId, request);

        // then
        assertThat(teamMemberRepository.findById(teamMemberId)).isEmpty();

        // 모든 게시글과 댓글 삭제 확인
        assertThat(postRepository.findById(post1.getId())).isEmpty();
        assertThat(postRepository.findById(post2.getId())).isEmpty();
        assertThat(postRepository.findById(post3.getId())).isEmpty();
        assertThat(commentRepository.findById(comment1.getId())).isEmpty();
        assertThat(commentRepository.findById(comment2.getId())).isEmpty();
        assertThat(commentRepository.findById(comment3.getId())).isEmpty();
    }

    @Test
    @DisplayName("팀 멤버 강제 탈퇴 - 여러 게시글/댓글 익명 처리")
    void removeMember_multipleContents_anonymizeAll_success() {
        // given
        Post post1 = testDataBuilder.createPost(
                "게시글1", "내용1", member2, testCategory, testTeam, teamMember2
        );
        Post post2 = testDataBuilder.createPost(
                "게시글2", "내용2", member2, testCategory, testTeam, teamMember2
        );

        Comment comment1 = testDataBuilder.createComment("댓글1", post1, member2, teamMember2);
        Comment comment2 = testDataBuilder.createComment("댓글2", post2, member2, teamMember2);

        Long teamMemberId = teamMember2.getId();
        Long post1Id = post1.getId();
        Long post2Id = post2.getId();
        Long comment1Id = comment1.getId();
        Long comment2Id = comment2.getId();

        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(false);

        // when
        teamMemberService.removeMember(testTeam.getId(), teamMemberId, request);

        // then
        assertThat(teamMemberRepository.findById(teamMemberId)).isEmpty();

        // 모든 게시글과 댓글의 작성자가 null인지 확인
        Post updatedPost1 = postRepository.findById(post1Id).orElseThrow();
        assertThat(updatedPost1.getTeamMember()).isNull();

        Post updatedPost2 = postRepository.findById(post2Id).orElseThrow();
        assertThat(updatedPost2.getTeamMember()).isNull();

        Comment updatedComment1 = commentRepository.findById(comment1Id).orElseThrow();
        assertThat(updatedComment1.getTeamMember()).isNull();

        Comment updatedComment2 = commentRepository.findById(comment2Id).orElseThrow();
        assertThat(updatedComment2.getTeamMember()).isNull();
    }

    @Test
    @DisplayName("팀 멤버 강제 탈퇴 - 게시글 없이 댓글만 있는 경우")
    void removeMember_onlyComments_deleteAll_success() {
        // given
        // 다른 사람이 작성한 게시글
        Post otherPost = testDataBuilder.createPost(
                "다른 게시글",
                "내용",
                member3,
                testCategory,
                testTeam,
                teamMember3
        );

        // teamMember2가 댓글만 작성
        Comment comment = testDataBuilder.createComment("댓글", otherPost, member2, teamMember2);

        Long commentId = comment.getId();
        Long teamMemberId = teamMember2.getId();
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when
        teamMemberService.removeMember(testTeam.getId(), teamMemberId, request);

        // then
        assertThat(teamMemberRepository.findById(teamMemberId)).isEmpty();
        assertThat(commentRepository.findById(commentId)).isEmpty();

        // 다른 사람의 게시글은 유지
        assertThat(postRepository.findById(otherPost.getId())).isPresent();
    }

    @Test
    @DisplayName("팀 멤버 강제 탈퇴 - 게시글만 있고 댓글 없는 경우")
    void removeMember_onlyPosts_deleteAll_success() {
        // given
        Post post = testDataBuilder.createPost(
                "게시글",
                "내용",
                member2,
                testCategory,
                testTeam,
                teamMember2
        );

        Long postId = post.getId();
        Long teamMemberId = teamMember2.getId();
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when
        teamMemberService.removeMember(testTeam.getId(), teamMemberId, request);

        // then
        assertThat(teamMemberRepository.findById(teamMemberId)).isEmpty();
        assertThat(postRepository.findById(postId)).isEmpty();
    }

    @Test
    @DisplayName("팀 멤버 강제 탈퇴 - 게시글/댓글이 없는 경우")
    void removeMember_noContent_success() {
        // given
        Long teamMemberId = teamMember2.getId();
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when & then
        assertDoesNotThrow(() ->
                teamMemberService.removeMember(testTeam.getId(), teamMemberId, request)
        );

        assertThat(teamMemberRepository.findById(teamMemberId)).isEmpty();
    }

    @Test
    @DisplayName("팀 멤버 강제 탈퇴 - request가 null인 경우 기본값(false) 적용")
    void removeMember_nullRequest_usesDefaultValue() {
        // given
        Post post = testDataBuilder.createPost(
                "게시글",
                "내용",
                member2,
                testCategory,
                testTeam,
                teamMember2
        );

        Long postId = post.getId();
        Long teamMemberId = teamMember2.getId();
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(null); // null -> false로 변환

        // when
        teamMemberService.removeMember(testTeam.getId(), teamMemberId, request);

        // then
        assertThat(teamMemberRepository.findById(teamMemberId)).isEmpty();

        // 게시글은 유지되고 작성자만 null (익명 처리)
        Post updatedPost = postRepository.findById(postId).orElseThrow();
        assertThat(updatedPost.getTeamMember()).isNull();
    }

    @Test
    @DisplayName("팀 멤버 강제 탈퇴 - 대댓글이 있는 경우도 함께 삭제")
    void removeMember_withNestedComments_deleteAll_success() {
        // given
        Post post = testDataBuilder.createPost(
                "게시글",
                "내용",
                member3,
                testCategory,
                testTeam,
                teamMember3
        );

        // teamMember2가 댓글 작성
        Comment parentComment = testDataBuilder.createComment(
                "부모 댓글", post, member2, teamMember2
        );

        // teamMember2가 대댓글도 작성
        Comment childComment = testDataBuilder.createComment(
                "대댓글", post, member2, teamMember2
        );
        parentComment.addReply(childComment);
        commentRepository.save(parentComment);  // cascade로 자식도 저장됨

        Long parentCommentId = parentComment.getId();
        Long childCommentId = childComment.getId();
        Long teamMemberId = teamMember2.getId();
        RemoveMemberRequestDTO request = new RemoveMemberRequestDTO(true);

        // when
        teamMemberService.removeMember(testTeam.getId(), teamMemberId, request);

        // then
        assertThat(teamMemberRepository.findById(teamMemberId)).isEmpty();
        assertThat(commentRepository.findById(parentCommentId)).isEmpty();
        assertThat(commentRepository.findById(childCommentId)).isEmpty();
    }

    @Test
    @DisplayName("팀 멤버 강제 탈퇴 - 트랜잭션 롤백 테스트")
    void removeMember_transactionRollback_onError() {
        // given
        Post post = testDataBuilder.createPost(
                "게시글",
                "내용",
                member2,
                testCategory,
                testTeam,
                teamMember2
        );

        Long teamMemberId = teamMember2.getId();
        Long postId = post.getId();

        // 잘못된 요청 (존재하지 않는 팀)
        Long invalidTeamId = 999L;
        RemoveMemberRequestDTO request = new  RemoveMemberRequestDTO(true);

        // when & then
        assertThatThrownBy(() ->
                teamMemberService.removeMember(invalidTeamId, teamMemberId, request))
                .isInstanceOf(TeamNotFoundException.class);

        // 롤백되어 멤버와 게시글이 여전히 존재
        assertThat(teamMemberRepository.findById(teamMemberId)).isPresent();
        assertThat(postRepository.findById(postId)).isPresent();
    }
}
