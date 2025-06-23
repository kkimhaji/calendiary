package com.example.board.config.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockTeamPermissionSecurityContextFactory.class)
public @interface WithMockTeamPermission {
    String email() default "test@example.com";
    String nickname() default "테스트사용자";
    String password() default "1234";
    String[] teamPermissions() default {"MANAGE_TEAM"};
}