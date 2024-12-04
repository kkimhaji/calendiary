package com.example.board.permission;

import com.example.board.domain.member.Member;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.CategoryRepository;
import com.example.board.service.CategoryService;
import com.example.board.service.TeamMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryPermissionEvaluator {
    private final CategoryService categoryService;

    public boolean hasPermission(Long categoryId, Authentication authentication, String permission) {
        Member member = (Member) authentication.getPrincipal();
        return categoryService.checkCategoryPermission(
                categoryId,
                member,
                TeamPermission.valueOf(permission)
        );
    }
}
