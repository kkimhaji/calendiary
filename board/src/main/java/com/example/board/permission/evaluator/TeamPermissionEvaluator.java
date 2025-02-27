package com.example.board.permission.evaluator;

import com.example.board.auth.UserPrincipal;
import com.example.board.domain.member.Member;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamRepository;
import com.example.board.domain.teamMember.TeamMember;
import com.example.board.domain.teamMember.TeamMemberRepository;
import com.example.board.permission.utils.PermissionUtils;
import com.example.board.permission.TeamPermission;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class TeamPermissionEvaluator implements CustomPermissionEvaluator {

    private final TeamMemberRepository teamMemberRepository;
    private final TeamRepository teamRepository;

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        if (authentication == null || targetId == null || !(permission instanceof TeamPermission)) {
            return false;
        }

        if (!((authentication.getPrincipal()) instanceof UserPrincipal))
            return false;
        try {
            Member member = ((UserPrincipal) authentication.getPrincipal()).getMember();
            Long teamId = (Long) targetId;
            TeamPermission teamPermission = (TeamPermission) permission;

            TeamMember teamMember = teamMemberRepository.findByTeamIdAndMember(teamId, member)
                    .orElseThrow(() -> new EntityNotFoundException("Team member not found"));

            return PermissionUtils.hasPermission(teamMember.getRole().getPermissions(), teamPermission);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || targetDomainObject == null || !(targetDomainObject instanceof Team)) {
            return false;
        }

        return hasPermission(authentication, ((Team) targetDomainObject).getId(), "Team", permission);
    }
}
