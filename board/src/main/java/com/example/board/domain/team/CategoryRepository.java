package com.example.board.domain.team;

import com.example.board.domain.role.CategoryRolePermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<TeamCategory, Long> {
    @Query("SELECT crp FROM CategoryRolePermission crp " +
            "WHERE crp.category.id = :categoryId " +
            "AND crp.role.id = :roleId")
    Optional<CategoryRolePermission> findCategoryRolePermission(
            @Param("categoryId") Long categoryId,
            @Param("roleId") Long roleId
    );

    @Query("SELECT tc FROM TeamCategory tc " +
            "LEFT JOIN FETCH tc.rolePermissions rp " +
            "LEFT JOIN FETCH rp.role " +
            "WHERE tc.team.id = :teamId")
    List<TeamCategory> findAllByTeamWithPermissions(@Param("teamId") Long teamId);


    Team findTeamById(Long id);

    List<TeamCategory> findAllByTeam(Team team);
}
