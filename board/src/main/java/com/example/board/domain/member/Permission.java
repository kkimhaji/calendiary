package com.example.board.domain.member;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Permission {
    ADMIN_READ("admin:read"),
    ADMIN_UPDATE("admin:update"),
    ADMIN_CREATE("admin:create"),
    ADMIN_DELETE("admin:delete"),

    MANAGER_READ("manger:read"),
    MANAGER_UPDATE("manger:update"),
    MANAGER_CREATE("manger:create"),
    MANAGER_DELETE("manger:delete")
    ;

    @Getter
    private final String permission;
}
