package com.example.board.service;

import com.example.board.domain.post.Post;
import com.example.board.domain.post.PostRepository;
import com.example.board.domain.role.CategoryPermissionRepository;
import com.example.board.domain.role.CategoryRolePermission;
import com.example.board.domain.team.CategoryRepository;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamCategory;
import com.example.board.domain.team.TeamRepository;
import com.example.board.dto.category.CategoryResponse;
import com.example.board.dto.category.CategoryRolePermissionDTO;
import com.example.board.dto.category.CreateCategoryRequest;
import com.example.board.dto.category.UpdateCategoryRequest;
import com.example.board.permission.TeamPermission;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.role.TeamRoleRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final TeamRepository teamRepository;
    private final TeamRoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final CategoryPermissionRepository categoryPermissionRepository;

    @Transactional
    public TeamCategory createCategory(Long teamId, CreateCategoryRequest request){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
        TeamCategory category = categoryRepository.save(request.toEntity(team));
        Map<Long, TeamRole> teamRoles = getTeamRoles(team, request);
        List<CategoryRolePermission> categoryRolePermissions = request.toCategoryRolePermissions(category, teamRoles);
//        request.rolePermissions().forEach((roleId, permissions) -> {
//            TeamRole role = roleRepository.findById(roleId)
//                    .orElseThrow(() -> new EntityNotFoundException("Role not found"));
//
//            CategoryRolePermission categoryRole = new CategoryRolePermission();
//            categoryRole.setCategory(category);
//            categoryRole.setRole(role);
//
//            //String -> TeamPermission
//            Set<TeamPermission> teamPermissions = permissions.stream()
//                            .map(TeamPermission::valueOf)
//                                    .collect(Collectors.toSet());
//
//            categoryRole.setPermissions(teamPermissions);
//
//            category.getRolePermissions().add(categoryRole);
//        });
        categoryPermissionRepository.saveAll(categoryRolePermissions);

        return category;
    }

    private Map<Long, TeamRole> getTeamRoles(Team team, CreateCategoryRequest request){
        Set<Long> requestRoleIds = request.rolePermissions().stream()
                .map(CategoryRolePermissionDTO::roleId).collect(Collectors.toSet());

        Map<Long, TeamRole> teamRoles = roleRepository.findAllByTeam(team).stream()
                .collect(Collectors.toMap(TeamRole::getId, role -> role));

        requestRoleIds.forEach(roleId ->{
            if (!teamRoles.containsKey(roleId))
                throw new IllegalArgumentException("Invalid role ID: " + roleId);
        });

        return teamRoles;
    }

    public boolean checkCategoryPermission(Long categoryId, Long roleId, TeamPermission permission) {
        return categoryRepository.findCategoryRolePermission(categoryId, roleId)
                .map(crp -> crp.hasPermission(permission))
                .orElse(false);
    }

    @Transactional
    public void deleteAllCategoriesInTeam(Team team){
        List<TeamCategory> categories = categoryRepository.findAllByTeam(team);
        for (TeamCategory category : categories) {
            deleteCategory(category);
        }
        categoryRepository.deleteAll(categories);
    }

    public void deleteCategory(TeamCategory category){
        List<Post> postsInCategory = postRepository.findAllByCategory(category);
        postsInCategory.forEach(post -> post.setCategory(null));
        List<CategoryRolePermission> categoryRolePermissions = categoryPermissionRepository.findAllByCategory(category);
        categoryRolePermissions.forEach(categoryPermission -> {
            categoryPermission.setCategory(null);
            categoryPermission.setRole(null);
        });
        categoryPermissionRepository.deleteAll(categoryRolePermissions);
        postRepository.deleteAll(postsInCategory);
    }

    @Transactional
    //카테고리 수정 -> 게시글에 저장된 카테고리 정보도 수정
    public CategoryResponse updateCategory(Long teamId, Long categoryId, UpdateCategoryRequest request){
        TeamCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(()->new EntityNotFoundException("category not found"));

        request.updateEntity(category);

        if (request.rolePermissions()!= null && request.rolePermissions().isPresent())
            updateCategoryPermissions(category, request, teamId);

        return CategoryResponse.from(categoryRepository.save(category));
    }

    private void updateCategoryPermissions(TeamCategory category, UpdateCategoryRequest request, Long teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow();
        Map<Long, TeamRole> teamRoles = roleRepository.findAllByTeam(team)
                .stream().collect(Collectors.toMap(TeamRole::getId, role -> role));

        //기존 권한 삭제
        category.clearRolePermissions();
        categoryPermissionRepository.deleteAllByCategoryId(category.getId());

        List<CategoryRolePermission> newPermissions = request.toCategoryRolePermissions(category, teamRoles);

        categoryPermissionRepository.saveAll(newPermissions);
    }

}
