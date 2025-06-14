package com.example.board.category;

import com.example.board.category.dto.*;
import com.example.board.comment.CommentRepository;
import com.example.board.member.Member;
import com.example.board.post.ImageService;
import com.example.board.post.Post;
import com.example.board.post.PostRepository;
import com.example.board.role.CategoryPermissionRepository;
import com.example.board.role.CategoryRolePermission;
import com.example.board.role.TeamRole;
import com.example.board.role.TeamRoleRepository;
import com.example.board.team.Team;
import com.example.board.team.TeamRepository;
import com.example.board.permission.CategoryPermission;
import com.example.board.teamMember.TeamMemberService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final TeamRepository teamRepository;
    private final TeamRoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final CategoryPermissionRepository categoryPermissionRepository;
    private final TeamMemberService teamMemberService;
    private final CommentRepository commentRepository;
    private final ImageService imageService;

    @Transactional
    public TeamCategory createCategory(Long teamId, CreateCategoryRequest request) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));
        request.validate();
        if (categoryRepository.existsByTeamAndName(team, request.name())){
            throw new IllegalArgumentException("Category name '" + request.name() + "' already exists in this team");
        }
        TeamCategory category = TeamCategory.createCategory(
                request.name(),
                request.description(),
                team
        );

        // 카테고리 저장 (ID 생성을 위해)
        category = categoryRepository.save(category);

        Map<Long, TeamRole> teamRoles = getTeamRoles(team, request);
        List<CategoryRolePermission> categoryRolePermissions = createCategoryRolePermissions(
                category,
                request,
                teamRoles
        );

        categoryRolePermissions.forEach(category::addRolePermission);
        categoryPermissionRepository.saveAll(categoryRolePermissions);

        return category;
    }

    private List<CategoryRolePermission> createCategoryRolePermissions(
            TeamCategory category,
            CreateCategoryRequest request,
            Map<Long, TeamRole> teamRoles) {

        return request.rolePermissions().stream()
                .map(rolePermDto -> {
                    TeamRole role = teamRoles.get(rolePermDto.roleId());
                    if (role == null) {
                        throw new EntityNotFoundException("Role not found for ID: " + rolePermDto.roleId());
                    }

                    return CategoryRolePermission.create(
                            category,
                            role,
                            rolePermDto.permissions()
                    );
                })
                .collect(Collectors.toList());
    }

    private Map<Long, TeamRole> getTeamRoles(Team team, CreateCategoryRequest request) {

        List<TeamRole> teamRoles = roleRepository.findAllByTeamWithPermissions(team);
        Map<Long, TeamRole> roleMap = teamRoles.stream()
                .collect(Collectors.toMap(TeamRole::getId, Function.identity()));

        // 요청된 모든 역할 ID가 유효한지 검증
        Set<Long> requestedRoleIds = request.rolePermissions().stream()
                .map(CategoryRolePermissionDTO::roleId)
                .collect(Collectors.toSet());

        Set<Long> invalidRoleIds = requestedRoleIds.stream()
                .filter(roleId -> !roleMap.containsKey(roleId))
                .collect(Collectors.toSet());

        if (!invalidRoleIds.isEmpty()) {
            throw new IllegalArgumentException("Invalid role IDs: " + invalidRoleIds);
        }

        return roleMap;
    }

    public boolean checkCategoryPermission(Long categoryId, Member member, CategoryPermission permission) {
        TeamCategory category = categoryRepository.findWithTeamById(categoryId).orElseThrow(() -> new EntityNotFoundException("category not found"));
        Long roleId = teamMemberService.getCurrentUserRole(category.getTeam().getId(), member).getId();
        return categoryRepository.findCategoryRolePermission(categoryId, roleId)
                .map(crp -> crp.hasPermission(permission))
                .orElse(false);
    }

    @Transactional
    public void deleteAllCategoriesInTeam(Team team) {
        List<TeamCategory> categories = categoryRepository.findAllByTeam(team);
        for (TeamCategory category : categories) {
            deleteCategory(category.getId());
        }
        categoryRepository.deleteAll(categories);
    }

    public void deleteCategory(Long categoryId) {
        TeamCategory category = categoryRepository.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("category not found"));
        List<Post> postsInCategory = postRepository.findAllByCategory(category);
        List<Long> postIds = postsInCategory.stream().map(Post::getId).toList();
        commentRepository.deleteAllByPostIdIn(postIds);
        for (Post post : postsInCategory) {
            try {
                imageService.deleteAllPostImages(post);
            } catch (IOException e) {
//                log.error("Error deleting images for post {}: {}", post.getId(), e.getMessage());
            }
        }
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
    public CategoryResponse updateCategory(Long teamId, Long categoryId, UpdateCategoryRequest request) {
        TeamCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("category not found"));

        category.updateDescription(request.description());
        if (categoryRepository.existsByTeamAndNameAndIdNot(category.getTeam(), request.name(), categoryId)){
            throw new IllegalArgumentException("Category name already exists: " + request.name());
        }
        if (!category.getName().equals(request.name()) && request.name() != null){
            category.updateName(request.name());
        }

        //권한 업데이트 내용이 있을 때만(Optional 존재) 권한 정보 업데이트
        request.rolePermissions().ifPresent(permissions -> updateCategoryPermissions(category, request, teamId));

        return CategoryResponse.from(categoryRepository.save(category));
    }

    private void updateCategoryPermissions(TeamCategory category, UpdateCategoryRequest request, Long teamId) {
        Team team = teamRepository.findById(teamId).orElseThrow();
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
                    .map(perm -> CategoryRolePermission.create(category, roleMap.get(perm.roleId()), perm.permissions()))
                    .collect(Collectors.toList());

            categoryPermissionRepository.saveAll(newPermissions);
        });
    }

    public List<CategoryListDTO> getCategoryListByTeam(Long teamId) {
        return categoryRepository.findCategoryListByTeamId(teamId);
    }

    public CategoryResponse getCategoryInfo(Long categoryId) {
        TeamCategory category = categoryRepository.findById(categoryId).orElseThrow(() -> new EntityNotFoundException("category not found"));
        return CategoryResponse.from(category);
    }
}
