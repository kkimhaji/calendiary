package com.example.board.teamInvite;

import com.example.board.common.exception.TeamNicknameDuplicationException;
import com.example.board.common.exception.TeamNotFoundException;
import com.example.board.member.Member;
import com.example.board.role.TeamRole;
import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.Team;
import com.example.board.teamInvite.dto.InviteCreateRequest;
import com.example.board.teamInvite.dto.InviteResponse;
import com.example.board.teamInvite.dto.InviteValidationResponse;
import com.example.board.teamInvite.dto.TeamJoinRequest;
import com.example.board.teamMember.TeamMember;
import com.example.board.teamMember.TeamMemberRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TeamInviteServiceTest extends AbstractTestSupport {
    @Autowired
    private TeamInviteService teamInviteService;
    @Autowired
    private TestDataBuilder testDataBuilder;
    @Autowired
    private TeamInviteRepository inviteRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private EntityManager entityManager;

    private Team testTeam;
    private TeamMember teamMember;
    private Member newMember;

    @BeforeEach
    void init(){
        testTeam = testDataBuilder.createTeam(member1);
        teamMember = testDataBuilder.addMemberToTeam(member2, testTeam.getId());
        newMember = testDataBuilder.createMember("newmember@test.com", "newMember", "password");
    }

    @Test
    @DisplayName("초대 코드 생성 성공")
    void createInvite_success() {
        // given
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
        InviteCreateRequest request = new InviteCreateRequest(expiresAt, 10);

        // when
        InviteResponse response = teamInviteService.createInvite(request, testTeam.getId());

        // then
        assertThat(response).isNotNull();
        assertThat(response.inviteLink()).contains("http://localhost:3000/teams/" + testTeam.getId() + "/join?code=");

        // DB에 저장되었는지 확인
        List<TeamInvite> invites = inviteRepository.findAll();
        assertThat(invites).hasSize(1);

        TeamInvite savedInvite = invites.get(0);
        assertThat(savedInvite.getTeam().getId()).isEqualTo(testTeam.getId());
        assertThat(savedInvite.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(savedInvite.getMaxUses()).isEqualTo(10);
        assertThat(savedInvite.getUsedCount()).isEqualTo(0);
    }


    @Test
    @DisplayName("초대 코드 검증 성공 - 유효한 코드")
    void validateInvite_validCode_success() {
        // given
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);
        TeamInvite invite = TeamInvite.create("validcode123", testTeam, expiresAt, 5);
        entityManager.persist(invite);
        entityManager.flush();
        // when
        InviteValidationResponse response = teamInviteService.validateInvite("validcode123");

        // then
        assertThat(response.teamId()).isEqualTo(testTeam.getId());
        assertThat(response.teamName()).isEqualTo(testTeam.getName());
        assertThat(response.teamDescription()).isEqualTo(testTeam.getDescription());
        assertThat(response.isValid()).isTrue();
        assertThat(response.message()).isEqualTo("유효한 초대 코드입니다");
    }

    @Test
    @DisplayName("초대 코드 검증 실패 - 존재하지 않는 코드")
    void validateInvite_nonExistentCode_failure() {
        // when
        InviteValidationResponse response = teamInviteService.validateInvite("nonexistent");

        // then
        assertThat(response.teamId()).isNull();
        assertThat(response.teamName()).isNull();
        assertThat(response.teamDescription()).isNull();
        assertThat(response.isValid()).isFalse();
        assertThat(response.message()).isEqualTo("존재하지 않는 코드");
    }

    @Test
    @DisplayName("초대 코드 검증 실패 - 만료된 코드")
    void validateInvite_expiredCode_failure() {
        // given
        LocalDateTime expiredTime = LocalDateTime.now().minusDays(1);
        TeamInvite expiredInvite = TeamInvite.create("expiredcode", testTeam, expiredTime, 5);
        entityManager.persist(expiredInvite);
        entityManager.flush();

        // when
        InviteValidationResponse response = teamInviteService.validateInvite("expiredcode");

        // then
        assertThat(response.teamId()).isEqualTo(testTeam.getId());
        assertThat(response.teamName()).isEqualTo(testTeam.getName());
        assertThat(response.isValid()).isFalse();
        assertThat(response.message()).isEqualTo("만료된 코드입니다");
    }

    @Test
    @DisplayName("초대 코드 검증 실패 - 사용 횟수 초과")
    void validateInvite_overusedCode_failure() {
        // given
        TeamInvite overusedInvite = TeamInvite.create("overusedcode", testTeam,
                LocalDateTime.now().plusDays(7), 2);
        overusedInvite.incrementUsedCount();
        overusedInvite.incrementUsedCount(); // 2번 사용하여 최대 사용 횟수 도달

        entityManager.persist(overusedInvite);
        entityManager.flush();
        // when
        InviteValidationResponse response = teamInviteService.validateInvite("overusedcode");

        // then
        assertThat(response.teamId()).isEqualTo(testTeam.getId());
        assertThat(response.teamName()).isEqualTo(testTeam.getName());
        assertThat(response.isValid()).isFalse();
        assertThat(response.message()).isEqualTo("사용 횟수를 초과했습니다");
    }

    @Test
    @DisplayName("팀 가입 성공")
    void joinTeam_success() {
        // given
        TeamInvite invite = TeamInvite.create("joincode", testTeam,
                LocalDateTime.now().plusDays(7), 5);
        entityManager.persist(invite);
        entityManager.flush();

        TeamJoinRequest request = new TeamJoinRequest("joincode", "newMemberNickname");

        // when
        teamInviteService.joinTeam(testTeam.getId(), request, newMember);

        // then
        // 팀 멤버가 추가되었는지 확인
        boolean isMemberAdded = teamMemberRepository.existsByTeamAndMember(testTeam, newMember);
        assertThat(isMemberAdded).isTrue();

        // 초대 코드 사용 횟수 증가 확인
        entityManager.flush();
        entityManager.clear();
        TeamInvite updatedInvite = inviteRepository.findByCode("joincode").orElseThrow();
        assertThat(updatedInvite.getUsedCount()).isEqualTo(1);

        // 팀 멤버 정보 확인
        TeamMember savedTeamMember = teamMemberRepository.findByTeamAndMember(testTeam, newMember)
                .orElseThrow();
        assertThat(savedTeamMember.getTeamNickname()).isEqualTo("newMemberNickname");
        assertThat(savedTeamMember.getRole().getId()).isEqualTo(testTeam.getBasicRoleId());
    }

    @Test
    @DisplayName("팀 가입 실패 - 유효하지 않은 코드")
    void joinTeam_invalidCode_failure() {
        // given
        TeamJoinRequest request = new TeamJoinRequest("invalidcode", "nickname");

        // when & then
        assertThatThrownBy(() -> teamInviteService.joinTeam(testTeam.getId(), request, newMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 코드");
    }

    @Test
    @DisplayName("팀 가입 실패 - 만료된 초대 코드")
    void joinTeam_expiredInvite_failure() {
        // given
        TeamInvite expiredInvite = TeamInvite.create("expiredcode", testTeam,
                LocalDateTime.now().minusDays(1), 5);
        entityManager.persist(expiredInvite);
        entityManager.flush();

        TeamJoinRequest request = new TeamJoinRequest("expiredcode", "nickname");

        // when & then
        assertThatThrownBy(() -> teamInviteService.joinTeam(testTeam.getId(), request, newMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("만료된 초대 코드");
    }

    @Test
    @DisplayName("팀 가입 실패 - 사용 횟수 초과")
    void joinTeam_overusedInvite_failure() {
        // given
        TeamInvite overusedInvite = TeamInvite.create("overusedcode", testTeam,
                LocalDateTime.now().plusDays(7), 1);
        overusedInvite.incrementUsedCount(); // 최대 사용 횟수 도달
        entityManager.persist(overusedInvite);
        entityManager.flush();

        TeamJoinRequest request = new TeamJoinRequest("overusedcode", "nickname");

        // when & then
        assertThatThrownBy(() -> teamInviteService.joinTeam(testTeam.getId(), request, newMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용 횟수 초과");
    }

    @Test
    @DisplayName("팀 가입 실패 - 이미 팀 멤버")
    void joinTeam_alreadyMember_failure() {
        // given
        TeamInvite invite = TeamInvite.create("validcode", testTeam,
                LocalDateTime.now().plusDays(7), 5);
        entityManager.persist(invite);
        entityManager.flush();

        // 이미 팀 멤버로 추가
        testDataBuilder.addMemberToTeam(newMember, testTeam.getId());

        TeamJoinRequest request = new TeamJoinRequest("validcode", "nickname");

        // when & then
        assertThatThrownBy(() -> teamInviteService.joinTeam(testTeam.getId(), request, newMember))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 팀의 멤버입니다");
    }
    @Test
    @DisplayName("팀 가입 실패 - 닉네임 중복")
    void joinTeam_duplicateNickname_failure() {
        // given
        TeamInvite invite = TeamInvite.create("validcode", testTeam,
                LocalDateTime.now().plusDays(7), 5);
        entityManager.persist(invite);
        entityManager.flush();

        // 이미 사용 중인 닉네임으로 다른 멤버 추가
        Member existingMember = testDataBuilder.createMember("existing@example.com", "duplicateNickname", "1234");
        testDataBuilder.addMemberToTeam(existingMember, testTeam.getId());

        TeamJoinRequest request = new TeamJoinRequest("validcode", "duplicateNickname");

        // when & then
        assertThatThrownBy(() -> teamInviteService.joinTeam(testTeam.getId(), request, newMember))
                .isInstanceOf(TeamNicknameDuplicationException.class)
                .hasMessage("이미 사용 중인 팀 닉네임입니다");
    }

    @Test
    @DisplayName("팀 초대 코드 전체 삭제 성공")
    void deleteInvitesByTeamId_success() {
        // given
        TeamInvite invite1 = TeamInvite.create("code1", testTeam,
                LocalDateTime.now().plusDays(7), 5);
        TeamInvite invite2 = TeamInvite.create("code2", testTeam,
                LocalDateTime.now().plusDays(7), 5);

        entityManager.persist(invite1);
        entityManager.flush();
        entityManager.persist(invite2);
        entityManager.flush();

        // 다른 팀의 초대 코드도 생성
        Team otherTeam = testDataBuilder.createTeam(member2);
        TeamInvite otherInvite = TeamInvite.create("othercode", otherTeam,
                LocalDateTime.now().plusDays(7), 5);
        entityManager.persist(otherInvite);
        entityManager.flush();

        // 삭제 전 초대 코드 수 확인
        assertThat(inviteRepository.findAll()).hasSize(3);

        // when
        teamInviteService.deleteInvitesByTeamId(testTeam.getId());

        // then
        entityManager.flush();
        entityManager.clear();

        List<TeamInvite> remainingInvites = inviteRepository.findAll();
        assertThat(remainingInvites).hasSize(1);
        assertThat(remainingInvites.get(0).getCode()).isEqualTo("othercode");
        assertThat(remainingInvites.get(0).getTeam().getId()).isEqualTo(otherTeam.getId());
    }

    @Test
    @DisplayName("초대 링크 형식 검증")
    void createInvite_inviteLinkFormat_validation() {
        // given
        InviteCreateRequest request = new InviteCreateRequest(
                LocalDateTime.now().plusDays(7), 5);

        // when
        InviteResponse response = teamInviteService.createInvite(request, testTeam.getId());

        // then
        String expectedPrefix = "http://localhost:3000/teams/" + testTeam.getId() + "/join?code=";
        assertThat(response.inviteLink()).startsWith(expectedPrefix);

        // 코드 부분 추출하여 UUID 형식인지 확인 (하이픈 제거된 형태)
        String code = response.inviteLink().substring(expectedPrefix.length());
        assertThat(code).hasSize(32); // UUID에서 하이픈 제거하면 32자
        assertThat(code).matches("^[a-f0-9]{32}$"); // 16진수 32자리
    }

    @Test
    @DisplayName("초대 코드 생성 - 존재하지 않는 팀")
    void createInvite_teamNotFound_failure() {
        // given
        Long nonExistentTeamId = 999L;
        InviteCreateRequest request = new InviteCreateRequest(
                LocalDateTime.now().plusDays(7), 5);

        // when & then
        assertThatThrownBy(() -> teamInviteService.createInvite(request, nonExistentTeamId))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage("team not found");
    }

    @Test
    @DisplayName("연속적인 팀 가입으로 사용 횟수 정확히 증가")
    void joinTeam_multipleJoins_usedCountIncrement() {
        // given
        TeamInvite invite = TeamInvite.create("multicode", testTeam,
                LocalDateTime.now().plusDays(7), 3);
        entityManager.persist(invite);
        entityManager.flush();

        Member newMember2 = testDataBuilder.createMember("newmember2@test.com", "newMember2", "1234");
        // when
        teamInviteService.joinTeam(testTeam.getId(),
                new TeamJoinRequest("multicode", "nickname1"), newMember);
        teamInviteService.joinTeam(testTeam.getId(),
                new TeamJoinRequest("multicode", "nickname2"), newMember2);

        // then
        entityManager.flush();
        entityManager.clear();

        TeamInvite updatedInvite = inviteRepository.findByCode("multicode").orElseThrow();
        assertThat(updatedInvite.getUsedCount()).isEqualTo(2);

        // 두 멤버 모두 팀에 가입되었는지 확인
        assertThat(teamMemberRepository.existsByTeamAndMember(testTeam, newMember)).isTrue();
        assertThat(teamMemberRepository.existsByTeamAndMember(testTeam, newMember2)).isTrue();
    }
}
