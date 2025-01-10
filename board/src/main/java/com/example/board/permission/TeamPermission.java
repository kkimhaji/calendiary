package com.example.board.permission;

import lombok.RequiredArgsConstructor;

public enum TeamPermission implements PermissionType{
    MANAGE_MEMBERS(0),
    MANAGE_ROLES(1),
    MANAGE_CATEGORIES(2);

    private final int position;

    TeamPermission(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

}