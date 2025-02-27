package com.example.board.permission;

import com.example.board.permission.evaluator.CategoryPermissionEvaluator;
import com.example.board.permission.evaluator.CustomPermissionEvaluator;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

public enum CategoryPermission implements PermissionType {
    VIEW_POST(0),
    CREATE_POST(1),
    DELETE_POST(2),
    CREATE_COMMENT(3),
    DELETE_COMMENT(4);

    private final int position;

    CategoryPermission(int position) {
        this.position = position;
    }

    public int getPosition(){
        return position;
    }

    //프론트에서 문자열로 파라미터를 넘겼을 때 Enum으로 변환 (대소문자 구분x)
    public static CategoryPermission fromCode(String value) {
        return Arrays.stream(CategoryPermission.values())
                .filter(e -> e.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid permission"));
    }
}
