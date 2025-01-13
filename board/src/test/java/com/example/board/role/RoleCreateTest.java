package com.example.board.role;

import com.example.board.domain.role.TeamRole;
import com.example.board.domain.role.TeamRoleRepository;
import com.example.board.domain.team.Team;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.dto.role.CreateRoleRequest;
import com.example.board.permission.PermissionUtils;
import com.example.board.permission.TeamPermission;
import com.example.board.service.TeamRoleService;
import com.example.board.service.TeamService;
import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.web.authentication.www.NonceExpiredException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.example.board.permission.TeamPermission.*;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ComponentScan("com.example.board")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
public class RoleCreateTest extends AbstractTestSupport {
    @Autowired
    private TeamRoleService teamRoleService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private TeamRoleRepository teamRoleRepository;
    private Team team;
    private TeamMember teamMember;
    @Autowired
    private TestDataBuilder testDataBuilder;

    @BeforeEach
    void init(){
        team = testDataBuilder.createTeam(member1);
        teamMember = testDataBuilder.addMemberToTeam(member2, team);
    }

    @Test
    void createRoleTest(){
        Set<TeamPermission> permissions = new HashSet<>(Arrays.asList(
                MANAGE_ROLES, MANAGE_MEMBERS
        ));
        var request = new CreateRoleRequest("testRole", permissions,"test create role");
        TeamRole createdRole = teamRoleService.createRole(team.getId(), request);

        assertThat(createdRole.getRoleName()).isEqualTo(request.roleName());
        assertThat(createdRole.getPermissions()).isEqualTo(PermissionUtils.createPermissionBits(request.permissions()));
        assertThat(createdRole.getPermissionSet()).isEqualTo(request.permissions());
    }

    @Test
    void deleteRoleTest(){
        Set<TeamPermission> permissions = new HashSet<>(Arrays.asList(
                MANAGE_ROLES, MANAGE_MEMBERS
        ));
        var request = new CreateRoleRequest("testRole", permissions,"test create role");
        TeamRole createdRole = teamRoleService.createRole(team.getId(), request);
        Long targetId = createdRole.getId();
        teamRoleService.deleteRole(team.getId(), targetId);

        assertThat(teamRoleRepository.findById(targetId)).isEmpty();
    }

}
