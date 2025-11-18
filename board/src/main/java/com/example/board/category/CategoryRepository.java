package com.example.board.category;

import com.example.board.role.CategoryRolePermission;
import com.example.board.team.Team;
import com.example.board.category.dto.CategoryListDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<TeamCategory, Long> {
    @Query("SELECT crp FROM CategoryRolePermission crp " +
            "WHERE crp.category.id = :categoryId " +
            "AND crp.role.id = :roleId")
    Optional<CategoryRolePermission> findCategoryRolePermission(
            @Param("categoryId") Long categoryId,
            @Param("roleId") Long roleId
    );

    @Query("SELECT c FROM TeamCategory c JOIN FETCH c.team WHERE c.id = :categoryId")
    Optional<TeamCategory> findWithTeamById(@Param("categoryId") Long categoryId);

    List<TeamCategory> findAllByTeam(Team team);

    // 순서대로 조회하도록 수정
    @Query("SELECT c FROM TeamCategory c WHERE c.team = :team ORDER BY c.displayOrder ASC, c.id ASC")
    List<TeamCategory> findAllByTeamOrderByDisplayOrder(@Param("team") Team team);

    @Query("SELECT new com.example.board.category.dto.CategoryListDTO(c.id, c.name, c.displayOrder) " +
            "FROM TeamCategory c WHERE c.team.id = :teamId ORDER BY c.displayOrder ASC, c.id ASC")
    List<CategoryListDTO> findCategoryListByTeamId(@Param("teamId") Long teamId);

    boolean existsByTeamAndName(Team team, String name);
    boolean existsByTeamAndNameAndIdNot(Team team, String name, Long id);

    @Query("SELECT c FROM TeamCategory c " +
            "LEFT JOIN FETCH c.rolePermissions rp " +
            "LEFT JOIN FETCH rp.role " +
            "WHERE c.id = :categoryId")
    Optional<TeamCategory> findByIdWithPermissions(@Param("categoryId") Long categoryId);

    // 순서 관련 쿼리 추가
    @Query("SELECT COALESCE(MAX(c.displayOrder), 0) FROM TeamCategory c WHERE c.team.id = :teamId")
    Integer findMaxDisplayOrderByTeamId(@Param("teamId") Long teamId);

    @Query("SELECT c FROM TeamCategory c WHERE c.team.id = :teamId " +
            "AND c.displayOrder BETWEEN :start AND :end ORDER BY c.displayOrder ASC")
    List<TeamCategory> findByTeamIdAndDisplayOrderBetween(
            @Param("teamId") Long teamId,
            @Param("start") Integer start,
            @Param("end") Integer end
    );
}
