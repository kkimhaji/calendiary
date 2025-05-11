package com.example.board.domain.team;

import com.example.board.domain.member.Member;
import com.example.board.dto.team.TeamInfoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    @Query("SELECT new com.example.board.dto.team.TeamInfoDTO(" +
            "t.id, t.name, t.description, t.created_by.nickname, t.createdAt, " +
            "(SELECT COUNT(tm) FROM TeamMember tm WHERE tm.team = t)) " +
            "FROM Team t " +
            "WHERE t.id = :teamId")
    Optional<TeamInfoDTO> findTeamDetailsById(@Param("teamId") Long teamId);
}
