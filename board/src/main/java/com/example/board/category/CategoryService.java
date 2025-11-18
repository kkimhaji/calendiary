package com.example.board.category;

import com.example.board.category.dto.*;
import com.example.board.comment.CommentRepository;
import com.example.board.common.exception.CategoryNotFoundException;
import com.example.board.common.exception.RoleNotFoundException;
import com.example.board.common.service.EntityValidationService;
import com.example.board.member.Member;
import com.example.board.permission.CategoryPermission;
import com.example.board.image.ImageService;
import com.example.board.post.Post;
import com.example.board.post.PostRepository;
import com.example.board.role.CategoryPermissionRepository;
import com.example.board.role.CategoryRolePermission;
import com.example.board.role.TeamRole;
import com.example.board.role.TeamRoleRepository;
import com.example.board.team.Team;
import com.example.board.teamMember.TeamMemberService;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class CategoryService {
    private final TeamRoleRepository roleRepository;
    private final CategoryRepository categoryRepository;
    private final PostRepository postRepository;
    private final CategoryPermissionRepository categoryPermissionRepository;
    private final TeamMemberService teamMemberService;
    private final CommentRepository commentRepository;
    private final ImageService imageService;
    private final EntityManager entityManager;
    private final EntityValidationService validationService;

    @Transactional
    public TeamCategory createCategory(Long teamId, CreateCategoryRequest request) {
        Team team = validationService.validateTeamExists(teamId);
        request.validate();
        if (categoryRepository.existsByTeamAndName(team, request.name())) {
            throw new IllegalArgumentException("Category name '" + request.name() + "' already exists in this team");
        }
        // 마지막 순서 값 조회 후 +1
        Integer maxOrder = categoryRepository.findMaxDisplayOrderByTeamId(teamId);
        Integer newOrder = maxOrder + 1;


        TeamCategory category = TeamCategory.createCategoryWithOrder(
                request.name(),
                request.description(),
                team,
                newOrder
        );

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
                        throw new RoleNotFoundException("Role not found for ID: " + rolePermDto.roleId());
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
        TeamCategory category = categoryRepository.findWithTeamById(categoryId).orElseThrow(CategoryNotFoundException::new);
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

    @Transactional
    public void deleteCategory(Long categoryId) {
        TeamCategory category = validationService.validateCategoryExists(categoryId);
        Long teamId = category.getTeam().getId();
        Integer deletedOrder = category.getDisplayOrder();

        List<Post> postsInCategory = postRepository.findAllByCategory(category);
        List<Long> postIds = postsInCategory.stream().map(Post::getId).toList();
        commentRepository.deleteAllByPostIdIn(postIds);

        for (Post post : postsInCategory) {
            try {
                imageService.deleteAllPostImages(post);
            } catch (IOException e) {
                log.error("Error deleting images for post {}: {}", post.getId(), e.getMessage());
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

        // 삭제 후 순서 재정렬
        List<TeamCategory> categoriesAfter = categoryRepository.findByTeamIdAndDisplayOrderBetween(
                teamId, deletedOrder + 1, Integer.MAX_VALUE
        );

        categoriesAfter.forEach(c -> c.updateDisplayOrder(c.getDisplayOrder() - 1));
        categoryRepository.saveAll(categoriesAfter);

    }

    @Transactional
    //카테고리 수정 -> 게시글에 저장된 카테고리 정보도 수정
    public CategoryResponse updateCategory(Long teamId, Long categoryId, UpdateCategoryRequest request) {
        TeamCategory category = validationService.validateCategoryExists(categoryId);

        category.updateDescription(request.description());
        if (categoryRepository.existsByTeamAndNameAndIdNot(category.getTeam(), request.name(), categoryId)) {
            throw new IllegalArgumentException("Category name already exists: " + request.name());
        }
        if (!category.getName().equals(request.name()) && request.name() != null) {
            category.updateName(request.name());
        }

        //권한 업데이트 내용이 있을 때만(Optional 존재) 권한 정보 업데이트
        request.rolePermissions().ifPresent(permissions -> updateCategoryPermissions(category, request, teamId));
        // 카테고리 저장 후 권한 정보와 함께 재조회
        categoryRepository.save(category);

        entityManager.flush();
        entityManager.clear(); // 1차 캐시 클리어

        TeamCategory updatedCategory = categoryRepository.findByIdWithPermissions(categoryId)
                .orElseThrow(CategoryNotFoundException::new);

        return CategoryResponse.from(updatedCategory);
    }

    @Transactional
    private void updateCategoryPermissions(TeamCategory category, UpdateCategoryRequest request, Long teamId) {
        categoryPermissionRepository.deleteAllByCategoryId(category.getId());
        entityManager.flush(); // 즉시 삭제 반영
        category.getRolePermissions().clear(); // 엔티티 상태 동기화

        Team team = validationService.validateTeamExists(teamId);
        List<TeamRole> teamRoles = roleRepository.findAllByTeamWithPermissions(team);

        Map<Long, TeamRole> roleMap = teamRoles.stream()
                .collect(Collectors.toMap(TeamRole::getId, Function.identity()));

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
        TeamCategory category = validationService.validateCategoryExists(categoryId);
        return CategoryResponse.from(category);
    }


    /**
     * 카테고리 순서 변경 (단일 이동)
     */
    @Transactional
    public void updateCategoryOrder(Long categoryId, Integer newOrder) {
        TeamCategory category = validationService.validateCategoryExists(categoryId);
        Long teamId = category.getTeam().getId();

        Integer oldOrder = category.getDisplayOrder();

        if (oldOrder.equals(newOrder)) {
            return; // 순서 변경 없음
        }

        List<TeamCategory> affectedCategories;

        if (newOrder > oldOrder) {
            // 아래로 이동: oldOrder < order <= newOrder 범위의 카테고리들을 위로
            affectedCategories = categoryRepository.findByTeamIdAndDisplayOrderBetween(
                    teamId, oldOrder + 1, newOrder
            );
            affectedCategories.forEach(c -> c.updateDisplayOrder(c.getDisplayOrder() - 1));
        } else {
            // 위로 이동: newOrder <= order < oldOrder 범위의 카테고리들을 아래로
            affectedCategories = categoryRepository.findByTeamIdAndDisplayOrderBetween(
                    teamId, newOrder, oldOrder - 1
            );
            affectedCategories.forEach(c -> c.updateDisplayOrder(c.getDisplayOrder() + 1));
        }

        category.updateDisplayOrder(newOrder);

        categoryRepository.saveAll(affectedCategories);
        categoryRepository.save(category);

        log.info("카테고리 순서 변경 완료 - categoryId: {}, oldOrder: {}, newOrder: {}",
                categoryId, oldOrder, newOrder);
    }

    /**
     * 카테고리 순서 일괄 변경 (드래그 앤 드롭)
     */
    @Transactional
    public void reorderCategories(Long teamId, List<Long> categoryIds) {
        Team team = validationService.validateTeamExists(teamId);
        List<TeamCategory> categories = categoryRepository.findAllById(categoryIds);

        // 모든 카테고리가 같은 팀인지 확인
        boolean allSameTeam = categories.stream()
                .allMatch(c -> c.getTeam().getId().equals(teamId));

        if (!allSameTeam) {
            throw new IllegalArgumentException("다른 팀의 카테고리는 재정렬할 수 없습니다.");
        }

        if (categories.size() != categoryIds.size()) {
            throw new IllegalArgumentException("일부 카테고리를 찾을 수 없습니다.");
        }

        // 순서대로 displayOrder 업데이트
        for (int i = 0; i < categoryIds.size(); i++) {
            Long categoryId = categoryIds.get(i);
            TeamCategory category = categories.stream()
                    .filter(c -> c.getId().equals(categoryId))
                    .findFirst()
                    .orElseThrow(() -> new CategoryNotFoundException("카테고리를 찾을 수 없습니다."));

            category.updateDisplayOrder(i);
        }

        categoryRepository.saveAll(categories);

        log.info("카테고리 일괄 순서 변경 완료 - teamId: {}, count: {}", teamId, categories.size());
    }
}
