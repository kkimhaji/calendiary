package com.example.board.config.security;

import com.example.board.auth.UserPrincipal;
import com.example.board.member.Member;
import com.example.board.permission.TeamPermission;
import com.example.board.permission.utils.PermissionConverter;
import com.example.board.role.TeamRole;
import com.example.board.support.TestDataBuilder;
import com.example.board.support.TestDataFactory;
import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class WithMockTeamPermissionSecurityContextFactory implements WithSecurityContextFactory<WithMockTeamPermission>{
    @Autowired private TestDataBuilder builder;
    @Autowired private TestDataFactory factory;

    @Override
    public SecurityContext createSecurityContext(WithMockTeamPermission annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Member member1 = factory.createMember(annotation.email(), annotation.nickname(), annotation.password());
        //member1: 관리자이므로 모든 권한을 갖고 있음
        Team team = builder.createTeam(member1);

        Set<TeamPermission> permissions = new HashSet<>();
        for (String perm : annotation.teamPermissions()) {
            permissions.add(TeamPermission.fromCode(perm));
        }

        //권한 테스트용 새 역할
        TeamRole newRole = builder.createNewRoleWithPermissions(team, "test role", permissions);

        // 테스트용 TeamMember 생성

        // UserPrincipal 생성 및 설정
        UserPrincipal userPrincipal = new UserPrincipal(member1);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, "password", Collections.emptyList());

        context.setAuthentication(auth);
        return context;
    }
}
