package com.example.board.domain.role;

import com.example.board.domain.team.TeamCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryPermissionRepository extends JpaRepository<CategoryRolePermission, Long> {
    List<CategoryRolePermission> findAllByRole(TeamRole role);
    List<CategoryRolePermission> findAllByCategory(TeamCategory category);

    void deleteAllByCategoryId(Long categoryId);
    Optional<CategoryRolePermission> findByCategoryAndRole(TeamCategory category, TeamRole role);
    @Modifying
    @Query("DELETE FROM CategoryRolePermission tcr WHERE tcr.role.id = :roleId")
    void deleteAllByRoleId(@Param("roleId") Long roleId);

    @Query("SELECT crp FROM CategoryRolePermission crp " +
            "WHERE crp.category.id = :categoryId")
    List<CategoryRolePermission> findAllByCategoryId(@Param("categoryId") Long categoryId);

    @Query("SELECT crp FROM CategoryRolePermission crp " +
            "JOIN FETCH crp.role " +
            "WHERE crp.category.id = :categoryId")
    List<CategoryRolePermission> findAllWithRoleByCategoryId(@Param("categoryId") Long categoryId);
}
