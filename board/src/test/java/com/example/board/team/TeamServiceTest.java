package com.example.board.team;

import com.example.board.domain.member.Member;
import com.example.board.domain.member.MemberRepository;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.dto.team.AddMemberRequestDTO;
import com.example.board.support.AbstractTestSupport;
import com.example.board.domain.team.Team;
import com.example.board.dto.team.TeamCreateRequestDTO;
import com.example.board.service.TeamMemberService;
import com.example.board.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ComponentScan("com.example.board")
@ExtendWith(MockitoExtension.class)
@Transactional
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class TeamServiceTest extends AbstractTestSupport{
    private Team team;
    private Member member;

    @Autowired
    private TeamService teamService;
    @Autowired
    private TeamMemberService teamMemberService;

    @BeforeEach
    public void setUp() {
        super.setUp();
        var request = new TeamCreateRequestDTO("testTeam", "test");
        team = teamService.createTeam(member1, request);
    }

    @Test
    void createTeam_adminPermission() {
        assertThat(team.getCreated_by()).isEqualTo(member1);
        TeamRole role = teamMemberService.getCurrentUserRole(team.getId(), member1);
        assertThat(role.getRoleName()).isEqualTo("ADMIN");

//        Set<TeamPermission> permissions = role.getPermissionSet();
//        System.out.println("result : " + permissions.stream().map(TeamPermission::name).collect(Collectors.joining(", ")));
    }

    @Test
    void addTeamMember_defaultRole() {
        AddMemberRequestDTO dto = new AddMemberRequestDTO(team.getId(), team.getBasicRoleId(), member2.getMemberId());
        TeamMember teamMember = teamService.addMember(dto);

        assertThat(teamMember.getRole().getId()).isEqualTo(team.getBasicRoleId());
    }

    @Test
    void addTeamMember_defaultNickname(){
        AddMemberRequestDTO dto = new AddMemberRequestDTO(team.getId(), team.getBasicRoleId(), member2.getMemberId());
        TeamMember teamMember = teamService.addMember(dto);

        assertThat(teamMember.getTeamNickname()).isEqualTo(member2.getNickname());
    }

}
