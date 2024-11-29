package com.example.board.domain.role;

import com.example.board.domain.team.TeamCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryPermissionRepository extends JpaRepository<CategoryRolePermission, Long> {
    List<CategoryRolePermission> findAllByRole(TeamRole role);
    List<CategoryRolePermission> findAllByCategory(TeamCategory category);
}
