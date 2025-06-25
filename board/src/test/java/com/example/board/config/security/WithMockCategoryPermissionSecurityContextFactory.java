package com.example.board.config.security;

import com.example.board.auth.UserPrincipal;
import com.example.board.category.TeamCategory;
import com.example.board.member.Member;
import com.example.board.permission.CategoryPermission;
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

public class WithMockCategoryPermissionSecurityContextFactory implements WithSecurityContextFactory<WithMockCategoryPermission> {

    @Autowired
    private TestDataBuilder builder;
    @Autowired
    private TestDataFactory factory;

    @Override
    public SecurityContext createSecurityContext(WithMockCategoryPermission annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Member admin = factory.createMember("admin@test.com", "admin", "1234");
        //테스트용 멤버
        Member member = factory.createMember(annotation.email(), annotation.nickname(), annotation.password());
        Team team = builder.createTeam(admin);
        builder.addMemberToTeam(member, team);
        Set<CategoryPermission> permissions = Arrays.stream(annotation.categoryPermissions())
                .map(CategoryPermission::fromCode).collect(Collectors.toSet());
        //카테고리 생성
        TeamCategory category = builder.createCategory(team.getBasicRoleId(), team, member, permissions);

        UserPrincipal userPrincipal = new UserPrincipal(member);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, "password", Collections.emptyList());

        context.setAuthentication(authentication);
        return context;
    }
}
