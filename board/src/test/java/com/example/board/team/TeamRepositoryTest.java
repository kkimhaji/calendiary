package com.example.board.team;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class TeamRepositoryTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void createTeamTest() {
        Member member = Member.createMember("test@test.com", "test", "1234", true, null, null);

        Team team = Team.create("testTeam", "test", member);

        Team resultTeam = teamRepository.save(team);

        assertThat(team.getName()).isEqualTo(resultTeam.getName());
        assertThat(team.getCreatedBy()).isEqualTo(resultTeam.getCreatedBy());
    }
}
