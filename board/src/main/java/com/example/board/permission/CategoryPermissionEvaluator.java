package com.example.board.permission;

import com.example.board.domain.member.Member;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.role.TeamRoleRepository;
import com.example.board.domain.team.CategoryRepository;
import com.example.board.domain.team.TeamCategory;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.service.CategoryService;
import com.example.board.service.TeamMemberService;
import com.sun.security.auth.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class CategoryPermissionEvaluator implements CustomPermissionEvaluator{
    private final CategoryService categoryService;
    private final CategoryRepository categoryRepository;
    private final TeamRoleRepository teamRoleRepository;

//    public boolean hasPermission(Long categoryId, Authentication authentication, String permission) {
//        Member member = (Member) authentication.getPrincipal();
//        return categoryService.checkCategoryPermission(
//                categoryId,
//                member,
//                TeamPermission.valueOf(permission)
//        );
//    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || targetDomainObject == null || !(permission instanceof CategoryPermission)) {
            return false;
        }

        Member member = (Member) authentication.getPrincipal();
        TeamCategory category = (TeamCategory) targetDomainObject;
        CategoryPermission categoryPermission = (CategoryPermission) permission;

        TeamRole role = teamRoleRepository.findByTeamAndMember(category.getTeam(), member)
                .orElseThrow(() -> new EntityNotFoundException("팀 멤버를 찾을 수 없습니다."));

        return PermissionUtils.hasPermission(role.getPermissions(), categoryPermission);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || targetId == null || !(permission instanceof CategoryPermission)) {
            return false;
        }

        TeamCategory category = categoryRepository.findById((Long) targetId).orElseThrow(() -> new EntityNotFoundException("category not found"));
        return hasPermission(authentication, category, permission);
    }
}
