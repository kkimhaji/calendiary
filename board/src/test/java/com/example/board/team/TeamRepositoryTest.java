package com.example.board.team;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class TeamRepositoryTest {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void createTeamTest(){

        Member member = Member.builder()
                .email("test@test.com")
                .nickname("test")
                .password("1234")
                .build();
        Team team = Team.builder()
                .name("testTeam")
                .created_by(member)
                .description("test")
                .build();

        Team resultTeam = teamRepository.save(team);

        assertThat(team.getName()).isEqualTo(resultTeam.getName());
        assertThat(team.getCreatedBy()).isEqualTo(resultTeam.getCreatedBy());
    }
}
