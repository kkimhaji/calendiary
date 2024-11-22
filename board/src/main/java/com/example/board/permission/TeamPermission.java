package com.example.board.permission;

import lombok.RequiredArgsConstructor;

public enum TeamPermission {
    VIEW_POST(0),      // 1
    CREATE_POST(1),         // 2
    EDIT_POST(2),          // 4
    DELETE_POST(3),        // 8
    MANAGE_MEMBERS(4),     // 16
    MANAGE_ROLES(5),       // 32
    CREATE_COMMENT(6),
    DELETE_COMMENT(7),
    MANAGE_CATEGORIES(8);

    private final int position;

    TeamPermission(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

}