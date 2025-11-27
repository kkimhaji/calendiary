package com.example.board.config.security;

import com.example.board.auth.UserPrincipal;
import com.example.board.member.Member;
import com.example.board.permission.TeamPermission;
import com.example.board.support.TestDataBuilder;
import com.example.board.support.TestDataFactory;
import com.example.board.team.Team;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public class WithMockTeamPermissionSecurityContextFactory implements WithSecurityContextFactory<WithMockTeamPermission> {
    @Autowired
    private TestDataBuilder builder;
    @Autowired
    private TestDataFactory factory;

    @Override
    public SecurityContext createSecurityContext(WithMockTeamPermission annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        //팀 생성자 - 관리자 이므로 모든 권한을 갖고 있음
        Member admin = factory.createMember("admin@test.com", "admin", "1234");
        Team team = builder.createTeam(admin);

        //테스트용 멤버
        Member member = factory.createMember(annotation.email(), annotation.nickname(), annotation.password());

        builder.addMemberToTeam(member, team.getId());
        Set<TeamPermission> teamPermissions = Arrays.stream(annotation.teamPermissions())
                .map(TeamPermission::fromCode).collect(Collectors.toSet());

        builder.updateRolePermission(team.getBasicRoleId(), teamPermissions);

        // UserPrincipal 생성 및 설정(Team id 포함)
        UserPrincipal userPrincipal = new UserPrincipal(member, team.getId());

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userPrincipal, "password", Collections.emptyList());

        context.setAuthentication(auth);
        return context;
    }
}