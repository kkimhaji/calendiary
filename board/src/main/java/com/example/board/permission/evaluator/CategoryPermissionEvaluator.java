package com.example.board.permission.evaluator;

import com.example.board.auth.UserPrincipal;
import com.example.board.common.exception.CategoryNotFoundException;
import com.example.board.member.Member;
import com.example.board.role.CategoryPermissionRepository;
import com.example.board.role.CategoryRolePermission;
import com.example.board.category.CategoryRepository;
import com.example.board.category.TeamCategory;
import com.example.board.teamMember.TeamMember;
import com.example.board.teamMember.TeamMemberRepository;
import com.example.board.permission.CategoryPermission;
import com.example.board.permission.utils.PermissionConverter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

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

            // ✅ 카테고리 존재 여부 먼저 확인 (예외 발생 방지)
            Optional<TeamCategory> categoryOpt = categoryRepository.findById(categoryId);
            if (categoryOpt.isEmpty()) {
                log.warn("카테고리를 찾을 수 없습니다: categoryId={}", categoryId);
                return false; // ✅ 예외 대신 false 반환으로 403 처리
            }

            TeamCategory category = categoryOpt.get();

            // ✅ 팀 멤버 존재 여부 확인 (예외 발생 방지)
            Optional<TeamMember> teamMemberOpt = teamMemberRepository.findByTeamIdAndMember(category.getTeam().getId(), member);
            if (teamMemberOpt.isEmpty()) {
                log.warn("팀 멤버를 찾을 수 없습니다: teamId={}, memberId={}", category.getTeam().getId(), member.getMemberId());
                return false; // ✅ 예외 대신 false 반환으로 403 처리
            }

            TeamMember teamMember = teamMemberOpt.get();

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
                byte[] permissionBytes = crp.getPermissionBytes();

                if (PermissionConverter.hasPermissionOptimized(permissionBytes, categoryPermission)) {
                    log.debug("카테고리 권한 확인 성공: categoryId={}, permission={}, member={}",
                            categoryId, categoryPermission, member.getMemberId());
                    return true;
                }
            }

            log.debug("카테고리 권한 부족: categoryId={}, permission={}, member={}",
                    categoryId, categoryPermission, member.getMemberId());
            return false;

        } catch (EntityNotFoundException e) {
            // ✅ EntityNotFoundException을 명시적으로 처리
            log.warn("엔티티를 찾을 수 없습니다 - categoryId: {}, message: {}", targetId, e.getMessage());
            return false;
        } catch (Exception e) {
            // ✅ 기타 모든 예외를 안전하게 처리
            log.error("카테고리 권한 검사 중 예상치 못한 오류 발생 - categoryId: {}, error: {}", targetId, e.getMessage(), e);
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
