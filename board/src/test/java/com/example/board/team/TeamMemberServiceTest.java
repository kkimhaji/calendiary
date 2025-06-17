package com.example.board.team;

import com.example.board.role.TeamRole;
import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.teamMember.TeamMember;
import com.example.board.teamMember.TeamMemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

public class TeamMemberServiceTest extends AbstractTestSupport {
    @Autowired
    private TeamMemberService teamMemberService;
    @Autowired
    private TeamService teamService;
    @Autowired
    private TestDataBuilder testDataBuilder;
    private Team team;
    private TeamMember teamMember;

    @BeforeEach
    void init() {
        team = testDataBuilder.createTeam(member1);
        teamMember = testDataBuilder.addMemberToTeam(member2, team);
    }

    @Test
    void geRoleTest() {
        TeamRole teamRole1 = teamMemberService.getCurrentUserRole(team.getId(), member1);
        TeamRole teamRole2 = teamMemberService.getCurrentUserRole(team.getId(), member2);

        assertThat(teamRole1.getRoleName()).isEqualTo("ADMIN");
        assertThat(teamRole2.getRoleName()).isEqualTo("Member");
    }
}
