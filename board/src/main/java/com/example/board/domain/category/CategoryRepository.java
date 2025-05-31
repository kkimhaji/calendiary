package com.example.board.domain.category;

import com.example.board.domain.role.CategoryRolePermission;
import com.example.board.domain.team.Team;
import com.example.board.dto.category.CategoryListDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<TeamCategory, Long> {
    @Query("SELECT crp FROM CategoryRolePermission crp " +
            "WHERE crp.category.id = :categoryId " +
            "AND crp.role.id = :roleId")
    Optional<CategoryRolePermission> findCategoryRolePermission(
            @Param("categoryId") Long categoryId,
            @Param("roleId") Long roleId
    );

    // 카테고리와 팀을 함께 조회 (N+1 문제 방지)
    @Query("SELECT c FROM TeamCategory c JOIN FETCH c.team WHERE c.id = :categoryId")
    Optional<TeamCategory> findWithTeamById(@Param("categoryId") Long categoryId);

    List<TeamCategory> findAllByTeam(Team team);

    @Query("SELECT new com.example.board.dto.category.CategoryListDTO(c.id, c.name) " +
            "FROM TeamCategory c WHERE c.team.id = :teamId")
    List<CategoryListDTO> findCategoryListByTeamId(@Param("teamId") Long teamId);

    boolean existsByTeamAndName(Team team, String name);
    boolean existsByTeamAndNameAndIdNot(Team team, String name, Long id);
}
