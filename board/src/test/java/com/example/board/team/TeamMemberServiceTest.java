package com.example.board.team;

import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.Team;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.dto.team.AddMemberRequestDTO;
import com.example.board.dto.team.TeamCreateRequestDTO;
import com.example.board.service.TeamMemberService;
import com.example.board.service.TeamService;
import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ComponentScan("com.example.board")
@ExtendWith(MockitoExtension.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional
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
    void init(){
        team = testDataBuilder.createTeam(member1);
        teamMember = testDataBuilder.addMemberToTeam(member2, team);
    }

    @Test
    void geRoleTest(){
        TeamRole teamRole1 = teamMemberService.getCurrentUserRole(team.getId(), member1);
        TeamRole teamRole2 = teamMemberService.getCurrentUserRole(team.getId(), member2);

        assertThat(teamRole1.getRoleName()).isEqualTo("ADMIN");
        assertThat(teamRole2.getRoleName()).isEqualTo("Member");
    }

    @Test
    void updateTeamNicknameTest(){
        String newNickname = "new nickname";
        String updatedName = teamMemberService.updateTeamNickname(teamMember.getId(), newNickname);

        assertThat(updatedName).isEqualTo(newNickname);
    }
}
