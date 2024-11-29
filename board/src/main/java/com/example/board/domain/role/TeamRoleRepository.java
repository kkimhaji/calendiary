package com.example.board.domain.role;

import com.example.board.domain.team.Team;
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
}
