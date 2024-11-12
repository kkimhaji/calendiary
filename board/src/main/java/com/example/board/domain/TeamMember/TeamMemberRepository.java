package com.example.board.domain.teamMember;

import com.example.board.domain.member.Member;
import com.example.board.domain.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TeamMemberRepository extends JpaRepository<Long, TeamMember> {
    List<Team> findAllTeamByMember(Member member);
    List<Member> findAllMemberByTeam(Team team);

    Optional<TeamMember> findByTeamAndMember(Team team, Member member);

}
