package com.example.board.permission;

import com.example.board.permission.evaluator.CategoryPermissionEvaluator;
import com.example.board.permission.evaluator.CustomPermissionEvaluator;
import com.example.board.permission.evaluator.TeamPermissionEvaluator;

import java.util.Arrays;

public enum TeamPermission implements PermissionType{
    MANAGE_MEMBERS(0, "MANAGE_MEMBERS"), // ✅ 코드 명시적 할당
    MANAGE_ROLES(1, "MANAGE_ROLES"),
    MANAGE_CATEGORIES(2, "MANAGE_CATEGORIES"),
    MANAGE_TEAM(3, "MANAGE_TEAM");

    private final int position;
    private final String code;

    TeamPermission(int position, String code) {
        this.position = position;
        this.code = code;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public String getCode() {
        return code;
    }


    //프론트에서 문자열로 파라미터를 넘겼을 때 Enum으로 변환 (대소문자 구분x)
    public static TeamPermission fromCode(String value) {
        return Arrays.stream(TeamPermission.values())
                .filter(e -> e.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid permission"));
    }

}