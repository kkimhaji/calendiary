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

    //created_by를 teamNickname으로 가져오도록
    @Query("SELECT new com.example.board.dto.team.TeamInfoDTO(" +
            "t.id, t.name, t.description, " +
            "COALESCE(tm.teamNickname, t.createdBy.nickname), t.createdAt, " +
            "(SELECT COUNT(tm2) FROM TeamMember tm2 WHERE tm2.team = t)) " +
            "FROM Team t " +
            "LEFT JOIN TeamMember tm ON tm.team = t AND tm.member = t.createdBy " +
            "WHERE t.id = :teamId")
    Optional<TeamInfoDTO> findTeamDetailsById(@Param("teamId") Long teamId);

}
