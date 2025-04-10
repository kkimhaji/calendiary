package com.example.board.service;

import com.example.board.domain.member.Member;
import com.example.board.domain.post.Post;
import com.example.board.domain.post.PostRepository;
import com.example.board.domain.role.CategoryPermissionRepository;
import com.example.board.domain.role.CategoryRolePermission;
import com.example.board.domain.team.CategoryRepository;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamCategory;
import com.example.board.domain.team.TeamRepository;
import com.example.board.dto.category.*;
import com.example.board.permission.CategoryPermission;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.role.TeamRoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final TeamRepository teamRepository;
    private final TeamRoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final CategoryPermissionRepository categoryPermissionRepository;
    private final TeamMemberService teamMemberService;

    @Transactional
    public TeamCategory createCategory(Long teamId, CreateCategoryRequest request){
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
        TeamCategory category = request.toEntity(team);


        Map<Long, TeamRole> teamRoles = getTeamRoles(team, request);
        List<CategoryRolePermission> categoryRolePermissions = request.toCategoryRolePermissions(category, teamRoles);

        categoryRolePermissions.forEach(crp -> {
            TeamRole role = crp.getRole();
            if (role == null){
                throw new EntityNotFoundException("Role not found: " + crp.getRole().getId());
            }
            category.addRolePermission(crp);
        });

        categoryPermissionRepository.saveAll(categoryRolePermissions);


        return categoryRepository.save(category);
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

    public boolean checkCategoryPermission(Long categoryId, Member member, CategoryPermission permission) {
        TeamCategory category = categoryRepository.findWithTeamById(categoryId).orElseThrow(() -> new EntityNotFoundException("category not found"));
        Long roleId = teamMemberService.getCurrentUserRole(category.getTeam().getId(), member).getId();
        return categoryRepository.findCategoryRolePermission(categoryId, roleId)
                .map(crp -> crp.hasPermission(permission))
                .orElse(false);
    }

    @Transactional
    public void deleteAllCategoriesInTeam(Team team){
        List<TeamCategory> categories = categoryRepository.findAllByTeam(team);
        for (TeamCategory category : categories) {
            deleteCategory(category.getId());
        }
        categoryRepository.deleteAll(categories);
    }

    public void deleteCategory(Long categoryId){
        TeamCategory category = categoryRepository.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("category not found"));
        List<Post> postsInCategory = postRepository.findAllByCategory(category);
        postsInCategory.forEach(post -> post.setCategory(null));
        List<CategoryRolePermission> categoryRolePermissions = categoryPermissionRepository.findAllByCategory(category);
        categoryRolePermissions.forEach(categoryPermission -> {
            categoryPermission.setCategory(null);
            categoryPermission.setRole(null);
        });
        categoryPermissionRepository.deleteAll(categoryRolePermissions);
        postRepository.deleteAll(postsInCategory);
        categoryRepository.deleteById(categoryId);
    }

    @Transactional
    //카테고리 수정 -> 게시글에 저장된 카테고리 정보도 수정
    public CategoryResponse updateCategory(Long teamId, Long categoryId, UpdateCategoryRequest request){
        TeamCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(()->new EntityNotFoundException("category not found"));

        request.updateEntity(category);

        //권한 업데이트 내용이 있을 때만(Optional 존재) 권한 정보 업데이트
        request.rolePermissions().ifPresent(permissions -> updateCategoryPermissions(category, request, teamId));

        return CategoryResponse.from(categoryRepository.save(category));
    }

    private void updateCategoryPermissions(TeamCategory category, UpdateCategoryRequest request, Long teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow();
//        Map<Long, TeamRole> teamRoles = roleRepository.findAllByTeam(team)
//                .stream().collect(Collectors.toMap(TeamRole::getId, role -> role));
        List<TeamRole> teamRoles = roleRepository.findAllByTeamWithPermissions(team);
        Map<Long, TeamRole> roleMap = teamRoles.stream()
                .collect(Collectors.toMap(TeamRole::getId, Function.identity()));

        // 기존 권한 삭제
        category.clearRolePermissions();
        categoryPermissionRepository.deleteAllByCategoryId(category.getId());

        // 유효한 역할 ID 검증
        request.rolePermissions().ifPresent(permissions -> {
            permissions.forEach(perm -> {
                if (!roleMap.containsKey(perm.roleId())) {
                    throw new IllegalArgumentException("Invalid role ID: " + perm.roleId());
                }
            });

            List<CategoryRolePermission> newPermissions = permissions.stream()
                    .map(perm -> perm.toEntity(category, roleMap.get(perm.roleId())))
                    .collect(Collectors.toList());

            categoryPermissionRepository.saveAll(newPermissions);
        });
    }

    public List<CategoryListDTO> getCategoryListByTeam(Long teamId){
        return categoryRepository.findCategoryListByTeamId(teamId);
    }

    public CategoryResponse getCategoryInfo(Long categoryId){
        TeamCategory category = categoryRepository.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("category not found"));
        return CategoryResponse.from(category);
    }
}
