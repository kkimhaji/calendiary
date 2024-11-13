package com.example.board.domain.team;

import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRoleRepository extends JpaRepository<TeamRole, Long> {
    boolean existsByTeamAndRoleName();
}
