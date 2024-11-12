package com.example.board.permission;

import com.example.board.domain.member.Member;
import com.example.board.domain.team.Team;
import com.example.board.domain.teamMember.TeamMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class TeamPermissionEvaluator implements PermissionEvaluator {

    private final TeamMemberRepository teamMemberRepository;

    @Override
    public boolean hasPermission(Authentication authentication,
                                 Object targetDomainObject,
                                 Object permission) {
        if (authentication == null || targetDomainObject == null || permission == null) {
            return false;
        }

        Member member  = (Member) authentication.getPrincipal();
        if (targetDomainObject instanceof Team team) {
            return hasTeamPermission(member, team,
                    TeamPermission.valueOf(permission.toString()));
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }

    private boolean hasTeamPermission(Member member, Team team, TeamPermission permission) {
        return teamMemberRepository.findByTeamAndMember(team, member)
                .map(user -> PermissionUtils.hasPermission(
                        user.getRole().getPermissions(),
                        permission))
                .orElse(false);
    }

}
