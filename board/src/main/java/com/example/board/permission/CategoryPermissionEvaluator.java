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
    private final CategoryRepository categoryRepository;
    private final TeamMemberService teamMemberService;

    public boolean hasPermission(Long categoryId, Authentication authentication, String permission) {
        Member member = (Member) authentication.getPrincipal();
        Long teamId = categoryRepository.findTeamById(categoryId).getId();
        TeamRole userRole = teamMemberService.getCurrentUserRole(teamId, member);
        return categoryService.checkCategoryPermission(
                categoryId,
                userRole.getId(),
                TeamPermission.valueOf(permission)
        );
    }
}
