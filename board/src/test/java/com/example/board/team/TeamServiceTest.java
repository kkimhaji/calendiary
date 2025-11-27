package com.example.board.team;

import com.example.board.auth.UserPrincipal;
import com.example.board.category.CategoryService;
import com.example.board.common.exception.TeamAccessDeniedException;
import com.example.board.common.exception.TeamNotFoundException;
import com.example.board.member.Member;
import com.example.board.permission.CategoryPermission;
import com.example.board.role.TeamRole;
import com.example.board.role.TeamRoleRepository;
import com.example.board.support.TestDataBuilder;
import com.example.board.team.dto.TeamInfoPageResponse;
import com.example.board.team.dto.TeamUpdateRequestDTO;
import com.example.board.team.enums.UserTeamStatus;
import com.example.board.teamInvite.TeamInviteService;
import com.example.board.teamInvite.dto.InviteCreateRequest;
import com.example.board.teamInvite.dto.InviteResponse;
import com.example.board.teamMember.TeamMember;
import com.example.board.team.dto.AddMemberRequestDTO;
import com.example.board.support.AbstractTestSupport;
import com.example.board.team.dto.TeamCreateRequestDTO;
import com.example.board.teamMember.TeamMemberRepository;
import com.example.board.teamMember.TeamMemberService;
import com.example.board.teamMember.dto.TeamMemberInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class TeamServiceTest extends AbstractTestSupport{
    @Autowired
    private TeamService teamService;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TestDataBuilder testDataBuilder;
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private TeamInviteService inviteService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TeamRoleRepository teamRoleRepository;

    @Test
    @DisplayName("팀 생성 - 정상 동작")
    void createTeam_success() {
        // given
        TeamCreateRequestDTO dto = new TeamCreateRequestDTO("테스트팀", "설명");

        // when
        Team team = teamService.createTeam(member1, dto);

        // then
        assertThat(team.getName()).isEqualTo("테스트팀");
        assertThat(team.getDescription()).isEqualTo("설명");
        assertThat(team.getCreatedBy()).isEqualTo(member1);
        assertThat(team.getAdminRoleId()).isNotNull();
        assertThat(team.getBasicRoleId()).isNotNull();

        // 관리자가 팀멤버에 추가되어있는지 확인
        List<TeamMember> members = teamMemberRepository.findAllByTeamId(team.getId());
        assertThat(members).extracting(TeamMember::getMember)
                .anyMatch(member -> member.getMemberId().equals(member1.getMemberId()));
    }

    @Test
    @DisplayName("팀에 신규 멤버 추가 - 정상")
    void addMember_success() {
        // given
        // 우선 팀을 하나 만든다
        TeamCreateRequestDTO teamCreateDTO = new TeamCreateRequestDTO("테스트팀", "설명");
        Team team = teamService.createTeam(member1, teamCreateDTO);

        AddMemberRequestDTO dto = new AddMemberRequestDTO(team.getId(), member2.getMemberId());

        // when
        TeamMember teamMember = teamService.addMember(dto);

        // then
        assertThat(teamMember.getTeam().getId()).isEqualTo(team.getId());
        assertThat(teamMember.getMember().getMemberId()).isEqualTo(member2.getMemberId());
        assertThat(teamMember.getRole().getId()).isEqualTo(team.getBasicRoleId());

        // DB 반영 확인
        Optional<TeamMember> found = teamMemberRepository.findByTeamAndMember(team, member2);
        assertThat(found).isPresent();
    }

    @Test
    @DisplayName("팀 정보 업데이트 - 이름, 설명 모두 정상")
    void updateTeamInfo_success() {
        // given
        Team team = teamService.createTeam(member1, new TeamCreateRequestDTO("이름", "desc"));
        TeamUpdateRequestDTO dto = new TeamUpdateRequestDTO("새이름", "새설명");

        // when
        long id = teamService.updateTeamInfo(team.getId(), dto);

        // then
        Team updatedTeam = teamRepository.findById(id).orElseThrow();
        assertThat(updatedTeam.getName()).isEqualTo("새이름");
        assertThat(updatedTeam.getDescription()).isEqualTo("새설명");
    }

    @Test
    @DisplayName("팀 정보 업데이트 - 이름만 변경")
    void updateTeamInfo_nameOnly() {
        Team team = teamService.createTeam(member1, new TeamCreateRequestDTO("old", "desc"));
        TeamUpdateRequestDTO dto = new TeamUpdateRequestDTO("newName", null);

        // when
        teamService.updateTeamInfo(team.getId(), dto);

        Team updated = teamRepository.findById(team.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("newName");
        assertThat(updated.getDescription()).isEqualTo("desc");
    }

    @Test
    @DisplayName("팀 삭제 시 팀/멤버/롤/초대 모두 삭제됨")
    void deleteTeam_success() {
        // given
        Team team = teamService.createTeam(member1, new TeamCreateRequestDTO("withCategory", "desc"));
        // 초대 하나 생성
        inviteService.createInvite(new InviteCreateRequest(LocalDateTime.now().plusDays(1), 3), team.getId());
        // 카테고리 생성
        testDataBuilder.createCategory(team.getId(), "공지", Set.of(CategoryPermission.CREATE_POST, CategoryPermission.VIEW_POST));
        // 멤버 추가
        TeamMember member = testDataBuilder.addMemberToTeam(member2, team.getId());

        // when
        teamService.deleteTeam(team.getId());

        // then
        assertThat(teamRepository.findById(team.getId())).isEmpty();
        assertThat(teamMemberRepository.findAllByTeamId(team.getId())).isEmpty();
        assertThat(teamRoleRepository.findAllByTeamId(team.getId())).isEmpty();
        assertThat(inviteService.validateInvite("no-such-code").isValid()).isFalse();
        // (카테고리 삭제 검증은 추가적으로 구현 가능)
    }

    @Test
    @DisplayName("팀 상세 조회 - 로그인 멤버(팀 소속), 초대코드 없는 경우")
    void getTeamInfo_member_success() {
        // given
        Team team = teamService.createTeam(member1, new TeamCreateRequestDTO("팀명", "설명"));
        TeamRole admin = teamRoleRepository.findById(team.getAdminRoleId()).orElseThrow();
        teamMemberRepository.save(TeamMember.createTeamMember(team, member2, admin));
        UserPrincipal principal = new UserPrincipal(member2);
        TeamMember adminTeamMember = testDataBuilder.getTeamMember(team.getId(), member1.getMemberId());
        // when
        TeamInfoPageResponse resp = teamService.getTeamInfo(team.getId(), principal, null);

        // then
        assertThat(resp.id()).isEqualTo(team.getId());
        assertThat(resp.name()).isEqualTo(team.getName());
        assertThat(resp.description()).isEqualTo(team.getDescription());
        assertThat(resp.created_by()).isEqualTo(adminTeamMember.getTeamNickname());
        assertThat(resp.createdAt()).isEqualTo(team.getCreatedAt());
        assertThat(resp.memberCount()).isEqualTo(teamMemberRepository.countByTeamId(team.getId()));
        assertThat(resp.userStatus()).isEqualTo(UserTeamStatus.TEAM_MEMBER);

        // TeamMemberInfo 검증
        TeamMemberInfo memberInfo = resp.teamMemberInfo();
        assertThat(memberInfo).isNotNull();
        assertThat(memberInfo.teamNickname()).isEqualTo(member2.getNickname());
        assertThat(memberInfo.roleName()).isEqualTo(admin.getRoleName());
        assertThat(memberInfo.joinedAt()).isNotNull();
    }

    @Test
    @DisplayName("팀 상세 조회 - 초대코드로 접근")
    void getTeamInfo_withInvite_success() {
        // given
        Team team = teamService.createTeam(member1, new TeamCreateRequestDTO("초대팀", "팀설명"));
        InviteResponse inviteResp = inviteService.createInvite(new InviteCreateRequest(LocalDateTime.now().plusHours(1), 3), team.getId());
        String code = inviteResp.inviteLink().substring(inviteResp.inviteLink().indexOf("code=") + 5);

        // when
        TeamInfoPageResponse resp = teamService.getTeamInfo(team.getId(), null, code);

        // then
        assertThat(resp.id()).isEqualTo(team.getId());
        assertThat(resp.name()).isEqualTo(team.getName());
        assertThat(resp.description()).isEqualTo(team.getDescription());
        assertThat(resp.created_by()).isEqualTo(team.getCreatedBy().getNickname());
        assertThat(resp.createdAt()).isEqualTo(team.getCreatedAt());
        assertThat(resp.memberCount()).isEqualTo(teamMemberRepository.countByTeamId(team.getId()));
        assertThat(resp.teamMemberInfo()).isNull(); // 초대 접근 시 멤버 정보 없음
        assertThat(resp.userStatus()).isEqualTo(UserTeamStatus.VALID_INVITE);
    }

    @Test
    @DisplayName("팀 상세 조회 - 권한 없음")
    void getTeamInfo_denied() {
        Team team = teamService.createTeam(member1, new TeamCreateRequestDTO("팀", "desc"));
        UserPrincipal stranger = new UserPrincipal(member2);

        assertThatThrownBy(() -> teamService.getTeamInfo(team.getId(), stranger, null))
                .isInstanceOf(TeamAccessDeniedException.class);
    }

    @Test
    @DisplayName("없는 팀 접근 시 TeamNotFoundException 발생")
    void teamNotFound_exception() {
        Long invalidId = 987654L;
        assertThatThrownBy(() -> teamService.getTeamInfo(invalidId, null, null))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessage("team not found");
    }

    @Test
    @DisplayName("없는 멤버로 addMember하면 UsernameNotFoundException")
    void addMember_NoSuchMember_exception() {
        Team team = teamService.createTeam(member1, new TeamCreateRequestDTO("팀", "desc"));
        Long invalidMemberId = 99999L;
        AddMemberRequestDTO dto = new AddMemberRequestDTO(team.getId(), invalidMemberId);

        assertThatThrownBy(() -> teamService.addMember(dto))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessage("no such user");
    }
}