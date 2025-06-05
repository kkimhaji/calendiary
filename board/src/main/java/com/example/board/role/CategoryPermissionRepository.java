package com.example.board.role;

import com.example.board.category.TeamCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

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

    @Modifying
    @Query(value =
            "INSERT INTO category_role_permission (category_id, role_id, permissions) " +
                    "SELECT c.id, :roleId, '0' FROM team_category c WHERE c.team_id = :teamId",
            nativeQuery = true)
    @Transactional
    int createDefaultPermissionsForNewRole(
            @Param("teamId") Long teamId,
            @Param("roleId") Long roleId
    );

    // 카테고리별 권한 존재 여부 확인
    boolean existsByCategoryAndRole(TeamCategory category, TeamRole role);

    List<CategoryRolePermission> findAllByCategoryIdAndRoleId(Long categoryId, Long roleId);
}
