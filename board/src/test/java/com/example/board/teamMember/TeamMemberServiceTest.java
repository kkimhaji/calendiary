package com.example.board.teamMember;

import com.example.board.comment.CommentRepository;
import com.example.board.member.Member;
import com.example.board.post.PostRepository;
import com.example.board.role.TeamRole;
import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.Team;
import com.example.board.team.TeamRepository;
import com.example.board.team.dto.TeamListDTO;
import com.example.board.teamMember.dto.MemberProfileResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    @BeforeEach
    void init(){
        member3 = testDataBuilder.createMember("thirdmember", "third", "1234");
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
        Member outsider = testDataBuilder.createMember("outsider@test.com", "outsider", "password");

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
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("You are not a member of this team!!");
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
        Member outsider = testDataBuilder.createMember("outsider@test.com", "outsider", "password");

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
        Member loner = testDataBuilder.createMember("loner@test.com", "loner", "password");

        // when
        List<TeamListDTO> result = teamMemberService.getTeams(loner);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("일반적인 이메일 마스킹")
    void emailMasking_normalEmail() {
        // given
        Member testMember = testDataBuilder.createMember("testuser@example.com", "test", "password");
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
        Member testMember = testDataBuilder.createMember("ab@test.com", "test", "password");
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
        Member testMember = testDataBuilder.createMember("a@test.com", "test", "password");
        TeamMember teamMember = testDataBuilder.addMemberToTeam(testMember, testTeam.getId());

        // when
        MemberProfileResponse result = teamMemberService.getTeamMemberProfile(teamMember.getId());

        // then
        assertThat(result.email()).isEqualTo("a@test.com"); // 1글자는 마스킹 안됨
    }
}
