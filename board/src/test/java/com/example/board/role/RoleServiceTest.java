package com.example.board.role;

import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import com.example.board.role.dto.AddMembersToRoleRequest;
import com.example.board.role.dto.CreateRoleRequest;
import com.example.board.permission.TeamPermission;
import com.example.board.category.CategoryService;
import com.example.board.team.TeamService;
import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
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
import static org.assertj.core.api.Assertions.assertThat;

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
    private TeamService teamService;
    @Autowired
    private TestDataBuilder testDataBuilder;
    @Autowired
    private CategoryService categoryService;


    @BeforeEach
    void init(){
        team = testDataBuilder.createTeam(member1);
        teamMember = testDataBuilder.addMemberToTeam(member2, team.getId());
        teamRole = testDataBuilder.createNewRole(team.getId(), "test role");
    }

    @Test
    void addMembersToRole(){
        //member2를 새로 만든 testRole에 넣기
        var addRequest = new AddMembersToRoleRequest(teamRole.getId(), Collections.singletonList(member2.getMemberId()));
        var response = teamRoleService.addMemberToRole(team.getId(), addRequest);

        assertThat(response.roleName()).isEqualTo(teamRole.getRoleName());
        assertThat(response.membersName().size()).isEqualTo(1);
        assertThat(response.membersName().get(0)).isEqualTo(teamMember.getTeamNickname());
    }

    @Test
    @DisplayName("역할 권한 변경")
    void updateRolePermission(){
        Set<TeamPermission> permissions = new HashSet<>(Arrays.asList(
                MANAGE_MEMBERS
        ));
        var updatedRole = teamRoleService.updateRolePermissions(teamRole.getId(), permissions);

        assertThat(updatedRole.getPermissionSet()).doesNotContain(MANAGE_ROLES);
    }

    @Test
    @DisplayName("역할 권한 확인")
    void checkPermissionOfRole(){
        assertThat(teamRoleService.checkPermission(teamRole.getId(), MANAGE_MEMBERS)).isTrue();
        assertThat(teamRoleService.checkPermission(teamRole.getId(), MANAGE_CATEGORIES)).isFalse();
    }

    @Test
    void deleteMemberFromRole_defaultRole(){
        var addRequest = new AddMembersToRoleRequest(teamRole.getId(), Collections.singletonList(member2.getMemberId()));
        teamRoleService.addMemberToRole(team.getId(), addRequest);

        //멤버를 역할에서 삭제하기 - 기본 역할
        teamRoleService.removeMemberFromRole(team.getId(), member2.getMemberId(), null);
        assertThat(teamMember.getRole().getRoleName()).isEqualTo("Member");
        assertThat(teamMember.getRole().getId()).isEqualTo(team.getBasicRoleId());
    }

    @Test
    void deleteMemberFromRole_changeRole(){
        Long teamId = team.getId();
        var addRequest = new AddMembersToRoleRequest(teamRole.getId(), Collections.singletonList(member2.getMemberId()));
        teamRoleService.addMemberToRole(teamId, addRequest);

        TeamRole newRole = testDataBuilder.createNewRole(teamId, "test role2");
        teamRoleService.removeMemberFromRole(teamId, member2.getMemberId(), newRole.getId());

        assertThat(teamMember.getRole().getId()).isEqualTo(newRole.getId());
    }
}
