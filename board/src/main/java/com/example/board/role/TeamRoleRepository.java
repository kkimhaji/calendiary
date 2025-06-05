package com.example.board.role;

import com.example.board.member.Member;
import com.example.board.team.Team;
import com.example.board.role.dto.TeamRoleDetailDto;
import com.example.board.role.dto.TeamRoleInfoDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TeamRoleRepository extends JpaRepository<TeamRole, Long> {
    boolean existsByTeamAndRoleName(Team team, String roleName);

    List<TeamRole> findAllByTeam(Team team);
    List<TeamRole> findAllByTeamId(Long teamId);

    @Query("SELECT new com.example.board.role.dto.TeamRoleDetailDto(" +
            "tr.id, tr.roleName, tr.permissionBytes, " +
            "(SELECT COUNT(tm) FROM TeamMember tm WHERE tm.role.id = tr.id)) " +
            "FROM TeamRole tr " +
            "WHERE tr.team.id = :teamId")
    List<TeamRoleDetailDto> findTeamRoleDetailsWithMemberCount(@Param("teamId") Long teamId);

    @Query("SELECT new com.example.board.role.dto.TeamRoleInfoDTO(" +
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

    @Query("SELECT r FROM TeamRole r " +
            "LEFT JOIN FETCH r.members tm " +
            "LEFT JOIN FETCH tm.member " +
            "WHERE r.team.id = :teamId AND r.id = :roleId")
    Optional<TeamRole> findWithDetails(@Param("teamId") Long teamId,
                                       @Param("roleId") Long roleId);

//    @Query("SELECT r FROM TeamRole r LEFT JOIN FETCH r.permissions WHERE r.team = :team")
    @Query("SELECT r FROM TeamRole r WHERE r.team = :team")
    List<TeamRole> findAllByTeamWithPermissions(@Param("team") Team team);

}
