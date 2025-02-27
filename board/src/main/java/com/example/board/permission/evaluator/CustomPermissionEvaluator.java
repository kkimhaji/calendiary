package com.example.board.permission.evaluator;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

import java.io.Serializable;

public interface CustomPermissionEvaluator extends PermissionEvaluator {
    boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission);
    boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission);
}