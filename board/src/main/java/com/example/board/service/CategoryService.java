package com.example.board.service;

import com.example.board.domain.role.CategoryRolePermission;
import com.example.board.domain.team.CategoryRepository;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamCategory;
import com.example.board.domain.team.TeamRepository;
import com.example.board.dto.category.CreateCategoryRequest;
import com.example.board.permission.TeamPermission;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.role.TeamRoleRepository;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final TeamRepository teamRepository;
    private final TeamRoleRepository roleRepository;
    private final CategoryRepository categoryRepository;

    public TeamCategory createCategory(Long teamId, CreateCategoryRequest request){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
        TeamCategory category = new TeamCategory();
        category.setTeam(team);
        category.setName(request.name());
        category.setDescription(request.description());

        request.rolePermissions().forEach((roleId, permissions) -> {
            TeamRole role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new EntityNotFoundException("Role not found"));

            CategoryRolePermission categoryRole = new CategoryRolePermission();
            categoryRole.setCategory(category);
            categoryRole.setRole(role);

            //String -> TeamPermission
            Set<TeamPermission> teamPermissions = permissions.stream()
                            .map(TeamPermission::valueOf)
                                    .collect(Collectors.toSet());

            categoryRole.setPermissions(teamPermissions);

            category.getRolePermissions().add(categoryRole);
        });

        return categoryRepository.save(category);
    }


    public boolean checkCategoryPermission(Long categoryId, Long roleId, TeamPermission permission) {
        return categoryRepository.findCategoryRolePermission(categoryId, roleId)
                .map(crp -> crp.hasPermission(permission))
                .orElse(false);
    }
}
