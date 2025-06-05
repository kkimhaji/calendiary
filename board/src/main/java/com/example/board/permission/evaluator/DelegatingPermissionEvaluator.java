package com.example.board.permission.evaluator;

import com.example.board.team.Team;
import com.example.board.category.TeamCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class DelegatingPermissionEvaluator implements PermissionEvaluator {

    private final TeamPermissionEvaluator teamPermissionEvaluator;
    private final CategoryPermissionEvaluator categoryPermissionEvaluator;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (targetDomainObject instanceof Team) {
            return teamPermissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
        } else if (targetDomainObject instanceof TeamCategory) {
            return categoryPermissionEvaluator.hasPermission(authentication, targetDomainObject, permission);
        }
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if ("Team".equals(targetType)) {
            return teamPermissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        } else if ("TeamCategory".equals(targetType)) {
            return categoryPermissionEvaluator.hasPermission(authentication, targetId, targetType, permission);
        }
        return false;
    }

}
