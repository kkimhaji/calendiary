package com.example.board.domain.team;

import com.example.board.domain.role.TeamCategoryRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<TeamCategory, Long> {
    @Query("SELECT crp FROM CategoryRolePermission crp " +
            "WHERE crp.category.id = :categoryId " +
            "AND crp.role.id = :roleId")
    Optional<TeamCategoryRole> findCategoryRolePermission(
            @Param("categoryId") Long categoryId,
            @Param("roleId") Long roleId
    );

    @Query("SELECT tc FROM TeamCategory tc " +
            "LEFT JOIN FETCH tc.rolePermissions rp " +
            "LEFT JOIN FETCH rp.role " +
            "WHERE tc.team.id = :teamId")
    List<TeamCategory> findAllByTeamWithPermissions(@Param("teamId") Long teamId);
}
