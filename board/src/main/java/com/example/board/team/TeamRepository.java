package com.example.board.team;

import com.example.board.team.dto.TeamInfoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {

    //created_by를 teamNickname으로 가져오도록
    @Query("SELECT new com.example.board.team.dto.TeamInfoDTO(" +
            "t.id, t.name, t.description, " +
            "COALESCE(tm.teamNickname, t.createdBy.nickname), " + // createdBy로 수정
            "t.createdAt, " +
            "(SELECT COUNT(tm2) FROM TeamMember tm2 WHERE tm2.team = t)) " +
            "FROM Team t " +
            "LEFT JOIN TeamMember tm ON tm.team = t AND tm.member = t.createdBy " + // createdBy 참조
            "WHERE t.id = :teamId")
    Optional<TeamInfoDTO> findTeamDetailsById(@Param("teamId") Long teamId);


}
