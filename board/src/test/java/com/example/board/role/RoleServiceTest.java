package com.example.board.role;

import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.Team;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.dto.role.AddMembersToRoleRequest;
import com.example.board.dto.role.CreateRoleRequest;
import com.example.board.dto.team.AddMemberRequestDTO;
import com.example.board.dto.team.TeamCreateRequestDTO;
import com.example.board.permission.TeamPermission;
import com.example.board.service.TeamRoleService;
import com.example.board.service.TeamService;
import com.example.board.support.AbstractTestSupport;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;

import java.util.*;

import static com.example.board.permission.TeamPermission.*;
import static com.example.board.permission.TeamPermission.VIEW_POST;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ComponentScan("com.example.board")
@ExtendWith(MockitoExtension.class)
public class RoleServiceTest extends AbstractTestSupport {

    @Autowired
    private TeamRoleService teamRoleService;
    private Team team;
    private TeamMember teamMember;
    private CreateRoleRequest roleRequest;
    private TeamRole teamRole;
    @Autowired
    private TeamService teamService;


    @BeforeEach
    void init(){
        var request = new TeamCreateRequestDTO("testTeam", "test");
        team = teamService.createTeam(member1, request);
        AddMemberRequestDTO dto = new AddMemberRequestDTO(team.getId(), team.getBasicRoleId(), member2.getMemberId());
        teamMember = teamService.addMember(dto);
        Set<TeamPermission> permissions = new HashSet<>(Arrays.asList(
                CREATE_POST, DELETE_POST, MANAGE_ROLES, EDIT_POST, MANAGE_MEMBERS,
                VIEW_POST
        ));
        roleRequest = new CreateRoleRequest("test role", permissions, "role for test");
        teamRole = teamRoleService.createRole(team.getId(), roleRequest);
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
                CREATE_POST, DELETE_POST, EDIT_POST, MANAGE_MEMBERS,
                VIEW_POST
        ));
        var updatedRole = teamRoleService.updateRolePermissions(teamRole.getId(), permissions);

        assertThat(updatedRole.getPermissionSet()).doesNotContain(MANAGE_ROLES);
    }

    @Test
    @DisplayName("역할 권한 확인")
    void checkPermissionOfRole(){
        assertThat(teamRoleService.checkPermission(teamRole.getId(), VIEW_POST)).isTrue();
        assertThat(teamRoleService.checkPermission(teamRole.getId(), MANAGE_CATEGORIES)).isFalse();
    }
}
