package com.example.board.permission;

import com.example.board.domain.member.Member;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.CategoryRepository;
import com.example.board.service.CategoryService;
import com.example.board.service.TeamMemberService;
import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class CategoryPermissionEvaluator implements CustomPermissionEvaluator{
    private final CategoryService categoryService;

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

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        Category category = (Category) targetDomainObject;
        CategoryPermission categoryPermission = (CategoryPermission) permission;

        CategoryRole role = categoryRoleService.findByUserAndCategory(userPrincipal.getUser(), category);
        return PermissionUtils.hasPermission(role.getPermissions(), categoryPermission);
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || targetId == null || !(permission instanceof CategoryPermission)) {
            return false;
        }

        Category category = categoryService.findById((Long) targetId);
        return hasPermission(authentication, category, permission);
    }
}
