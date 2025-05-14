package com.example.board.permission.evaluator;

import com.example.board.auth.UserPrincipal;
import com.example.board.domain.member.Member;
import com.example.board.domain.role.CategoryPermissionRepository;
import com.example.board.domain.role.CategoryRolePermission;
import com.example.board.domain.team.CategoryRepository;
import com.example.board.domain.team.TeamCategory;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.domain.teamMember.TeamMemberRepository;
import com.example.board.permission.CategoryPermission;
import com.example.board.permission.utils.PermissionConverter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CategoryPermissionEvaluator implements CustomPermissionEvaluator {
    private final CategoryRepository categoryRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final CategoryPermissionRepository categoryPermissionRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || targetId == null || !(permission instanceof CategoryPermission)) {
            log.debug("권한 검사 실패: 유효하지 않은 매개변수");
            return false;
        }

        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
            log.debug("권한 검사 실패: UserPrincipal이 아님");
            return false;
        }

        try {
            Member member = ((UserPrincipal) authentication.getPrincipal()).getMember();
            CategoryPermission categoryPermission = (CategoryPermission) permission;
            Long categoryId = (Long) targetId;

            TeamCategory category = categoryRepository.findById((Long) targetId).orElseThrow(() -> new EntityNotFoundException("category not found"));

            TeamMember teamMember = teamMemberRepository.findByTeamIdAndMember(category.getTeam().getId(), member)
                    .orElseThrow(() -> new EntityNotFoundException("Team Member not found"));

            List<CategoryRolePermission> categoryRolePermissions =
                    categoryPermissionRepository.findAllByCategoryIdAndRoleId(
                            categoryId, teamMember.getRole().getId());

            // 역할에 카테고리 권한이 없는 경우
            if (categoryRolePermissions.isEmpty()) {
                log.debug("카테고리 {} 에 대한 역할 {} 의 권한 레코드가 없음",
                        categoryId, teamMember.getRole().getRoleName());
                return false;
            }

            // CategoryRolePermission에서 권한 확인
            for (CategoryRolePermission crp : categoryRolePermissions) {
            // 바이트 배열을 직접 사용
                byte[] permissionBytes = crp.getPermissionBytes();

                // 최적화된 권한 확인 메서드 사용
                if (PermissionConverter.hasPermissionOptimized(permissionBytes, categoryPermission)) {
                    return true;
                }
            }
            return false;

        } catch (Exception e) {
            log.error("카테고리 권한 검사 중 오류 발생: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || targetDomainObject == null || !(targetDomainObject instanceof TeamCategory)) {
            return false;
        }

        return hasPermission(authentication, ((TeamCategory) targetDomainObject).getId(), "TeamCategory", permission);
    }


}
