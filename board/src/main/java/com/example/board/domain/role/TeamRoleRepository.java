package com.example.board.domain.role;

import com.example.board.domain.team.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRoleRepository extends JpaRepository<TeamRole, Long> {
    boolean existsByTeamAndRoleName(Team team, String roleName);
}
