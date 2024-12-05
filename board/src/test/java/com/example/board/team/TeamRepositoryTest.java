package com.example.board.team;

import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class TeamRepositoryTest {

    @Autowired
    private TeamRepository teamRepository;

    @Test
    void createTeamTest(){
        Team team = Team.builder()
                .name("testTeam")
                .build();
    }
}
