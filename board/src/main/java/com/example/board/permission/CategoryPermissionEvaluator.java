package com.example.board.permission;

import com.example.board.auth.UserPrincipal;
import com.example.board.domain.member.Member;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.role.TeamRoleRepository;
import com.example.board.domain.team.CategoryRepository;
import com.example.board.domain.team.TeamCategory;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.domain.teamMember.TeamMemberRepository;
import com.example.board.service.CategoryService;
import com.example.board.service.TeamMemberService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class CategoryPermissionEvaluator implements CustomPermissionEvaluator {
    private final CategoryRepository categoryRepository;
    private final TeamMemberRepository teamMemberRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || targetId == null || !(permission instanceof CategoryPermission)) {
            return false;
        }

        if (!(authentication.getPrincipal() instanceof UserPrincipal))
            return false;

        try {
            Member member = ((UserPrincipal) authentication.getPrincipal()).getMember();
            CategoryPermission categoryPermission = (CategoryPermission) permission;

            TeamCategory category = categoryRepository.findById((Long) targetId).orElseThrow(() -> new EntityNotFoundException("category not found"));

            TeamMember teamMember = teamMemberRepository.findByTeamIdAndMember(category.getTeam().getId(), member)
                    .orElseThrow(() -> new EntityNotFoundException("Team Member not found"));

            return PermissionUtils.hasPermission(teamMember.getRole().getPermissions(), categoryPermission);
        } catch (Exception e) {
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
