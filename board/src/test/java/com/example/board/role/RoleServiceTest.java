package com.example.board.role;

import com.example.board.common.exception.RoleDeletionException;
import com.example.board.member.Member;
import com.example.board.permission.utils.PermissionConverter;
import com.example.board.permission.utils.PermissionUtils;
import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import com.example.board.role.dto.AddMembersToRoleRequest;
import com.example.board.role.dto.CreateRoleRequest;
import com.example.board.permission.TeamPermission;
import com.example.board.category.CategoryService;
import com.example.board.team.TeamService;
import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.teamMember.TeamMemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import java.util.*;

import static com.example.board.permission.TeamPermission.*;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ComponentScan("com.example.board")
@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RoleServiceTest extends AbstractTestSupport {

    @Autowired
    private TeamRoleService teamRoleService;
    private Team team;
    private TeamMember teamMember;
    private CreateRoleRequest roleRequest;
    private TeamRole teamRole;
    @Autowired
    private TestDataBuilder testDataBuilder;
    @Autowired
    private TeamRoleRepository teamRoleRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;

    @BeforeEach
    void init(){
        team = testDataBuilder.createTeam(member1);
        teamMember = testDataBuilder.addMemberToTeam(member2, team.getId());
        teamRole = testDataBuilder.createNewRole(team.getId(), "test role");
    }

    @Test
    @DisplayName("역할 추가 - 정상 동작")
    void createRole_success() {
        // given
        CreateRoleRequest req = new CreateRoleRequest(
                "subLeader", Set.of(MANAGE_TEAM), "부팀장 롤");

        // when
        TeamRole newRole = teamRoleService.createRole(team.getId(), req);

        // then
        assertThat(newRole).isNotNull();
        assertThat(newRole.getTeam().getId()).isEqualTo(team.getId());
        assertThat(newRole.getRoleName()).isEqualTo("subLeader");
        assertThat(newRole.getDescription()).isEqualTo("부팀장 롤");
        assertThat(newRole.getPermissionBytes()).isEqualTo(PermissionUtils.createPermissionBytes(Set.of(MANAGE_TEAM)));

        // 카테고리 기본 권한 자동 생성 검증
        assertThat(teamRoleRepository.findById(newRole.getId()))
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    @DisplayName("역할 추가 - 중복 이름 예외")
    void createRole_duplicateRoleName_fail() {
        // when
        teamRoleService.createRole(team.getId(), new CreateRoleRequest("부팀장", Set.of(), "Test role"));

        CreateRoleRequest req = new CreateRoleRequest("부팀장", Set.of(), "중복 역할명");
        assertThatThrownBy(() -> teamRoleService.createRole(team.getId(), req))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Role name already exists in this team");
    }

    @Test
    @DisplayName("권한 변경 - 정상 동작")
    void updateRolePermissions_success() {
        //기본 TeamRole은 MANAGE_MEMBERS, MANAGE_ROLES로 권한 설정됨
        // given
        Set<TeamPermission> newPerms = Set.of(MANAGE_ROLES);

        // when
        TeamRole updated = teamRoleService.updateRolePermissions(teamRole.getId(), newPerms);

        // then
        assertThat(updated.getPermissionSet()).containsAll(newPerms);
    }

    @Test
    @DisplayName("역할 상세 조회 - 정상")
    void getRoleById_success() {
        TeamRole found = teamRoleService.getRoleById(teamRole.getId());
        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(teamRole.getId());
        assertThat(found.getRoleName()).isEqualTo(teamRole.getRoleName());
    }

    @Test
    @DisplayName("관리자 역할 확인")
    void createAdmin_success() {
        TeamRole admin = teamRoleService.getRoleById(team.getAdminRoleId());

        assertThat(admin.getTeam().getId()).isEqualTo(team.getId());
        assertThat(admin.getRoleName()).isEqualTo("ADMIN");
        assertThat(admin.getPermissionSet().size()).isEqualTo(TeamPermission.values().length);
    }

    @Test
    @DisplayName("기본 역할 확인")
    void createBasic_success() {
        TeamRole basic = teamRoleService.getRoleById(team.getBasicRoleId());

        assertThat(basic.getTeam().getId()).isEqualTo(team.getId());
        assertThat(basic.getRoleName()).isEqualTo("Member");
        assertThat(basic.getPermissionSet()).isEmpty();
    }

    @Test
    @DisplayName("역할 삭제 - 정상 (기존 멤버는 기본 롤로 이동)")
    void deleteRole_success() {
        // given
        Member member = memberRepository.save(Member.createMember("m@e.com", "m", "nick", true, null, null));
        TeamMember tm = testDataBuilder.addMemberToTeam(member, team.getId());

        testDataBuilder.addMemberToRole(member, teamRole);

        // when
        teamRoleService.deleteRole(team.getId(), teamRole.getId());

        // then
        TeamMember changed = teamMemberRepository.findById(tm.getId()).orElseThrow();
        assertThat(changed.getRole().getId()).isEqualTo(team.getBasicRoleId()); // Default role로
    }

    @Test
    @DisplayName("역할 삭제 실패 - 기본 역할")
    void deleteRole_fail_basicRole() {
        assertThatThrownBy(() -> teamRoleService.deleteRole(team.getId(), team.getBasicRoleId()))
                .isInstanceOf(RoleDeletionException.class)
                .hasMessage("기본 역할은 삭제할 수 없습니다.");
    }

}
