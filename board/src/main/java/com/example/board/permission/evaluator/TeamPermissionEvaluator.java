package com.example.board.permission.evaluator;

import com.example.board.auth.UserPrincipal;
import com.example.board.member.Member;
import com.example.board.role.TeamRole;
import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import com.example.board.teamMember.TeamMemberRepository;
import com.example.board.permission.TeamPermission;
import com.example.board.permission.utils.PermissionConverter;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class TeamPermissionEvaluator implements CustomPermissionEvaluator {

    private final TeamMemberRepository teamMemberRepository;

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

            TeamRole role = teamMember.getRole();
            byte[] permissionBytes = role.getPermissionBytes();

            return PermissionConverter.hasPermissionOptimized(permissionBytes, teamPermission);
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
