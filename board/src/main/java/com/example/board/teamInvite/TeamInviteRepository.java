package com.example.board.teamInvite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface TeamInviteRepository extends JpaRepository<TeamInvite, Long> {
    Optional<TeamInvite> findByCode(String code);

    @Modifying
    @Transactional
    @Query("DELETE FROM TeamInvite ti WHERE ti.team.id = :teamId")
    void deleteByTeamId(@Param("teamId") Long teamId);
}
