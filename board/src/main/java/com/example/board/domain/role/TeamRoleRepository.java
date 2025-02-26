package com.example.board.domain.role;

import com.example.board.domain.member.Member;
import com.example.board.domain.team.Team;
import com.example.board.dto.role.TeamRoleDetailDto;
import com.example.board.dto.role.TeamRoleInfoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

public interface TeamRoleRepository extends JpaRepository<TeamRole, Long> {
    boolean existsByTeamAndRoleName(Team team, String roleName);
    Optional<TeamRole> findByIdAndTeamId(Long roleId, Long teamId);
    @Query("SELECT tr FROM TeamRole tr WHERE tr.team.id = :teamId AND tr.id = :roleId")
    Optional<TeamRole> findByTeamIdAndRoleId(
            @Param("teamId") Long teamId,
            @Param("roleId") Long roleId
    );

    List<TeamRole> findAllByTeam(Team team);
    List<TeamRole> findAllByTeamId(Long teamId);

    @Query("SELECT new com.example.board.dto.role.TeamRoleDetailDto(" +
            "tr.id, tr.roleName, tr.permissions, " +
            "(SELECT COUNT(tm) FROM TeamMember tm WHERE tm.role.id = tr.id)) " +
            "FROM TeamRole tr " +
            "WHERE tr.team.id = :teamId")
    List<TeamRoleDetailDto> findTeamRoleDetailsWithMemberCount(@Param("teamId") Long teamId);

    @Query("SELECT new com.example.board.dto.role.TeamRoleInfoDTO(" +
            "tr.id, tr.roleName)" +
            "FROM TeamRole tr " +
            "WHERE tr.team.id = :teamId")
    List<TeamRoleInfoDTO> findTeamRoleInfo(@Param("teamId") Long teamId);

    // 성능 최적화를 위한 fetch join 버전
    @Query("SELECT tr FROM TeamRole tr " +
            "LEFT JOIN FETCH tr.members tm " +
            "WHERE tr.team = :team AND tr.id = " +
            "(SELECT tm2.role.id FROM TeamMember tm2 WHERE tm2.team = :team AND tm2.member = :member)")
    Optional<TeamRole> findByTeamAndMember(@Param("team") Team team, @Param("member") Member member);
}
