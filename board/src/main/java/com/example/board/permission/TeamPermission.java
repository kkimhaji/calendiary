package com.example.board.permission;

import com.example.board.permission.evaluator.CategoryPermissionEvaluator;
import com.example.board.permission.evaluator.CustomPermissionEvaluator;
import com.example.board.permission.evaluator.TeamPermissionEvaluator;

import java.util.Arrays;

public enum TeamPermission implements PermissionType{
    MANAGE_MEMBERS(0),
    MANAGE_ROLES(1),
    MANAGE_CATEGORIES(2),
    MANAGE_TEAM(3);

    private final int position;

    TeamPermission(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }


    //프론트에서 문자열로 파라미터를 넘겼을 때 Enum으로 변환 (대소문자 구분x)
    public static TeamPermission fromCode(String value) {
        return Arrays.stream(TeamPermission.values())
                .filter(e -> e.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid permission"));
    }

}