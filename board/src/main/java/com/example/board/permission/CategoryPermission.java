package com.example.board.permission;

import java.util.Arrays;

public enum CategoryPermission implements PermissionType {
    VIEW_POST(0, "VIEW_POST"),
    CREATE_POST(1, "CREATE_POST"),
    DELETE_POST(2, "DELETE_POST"),
    CREATE_COMMENT(3, "CREATE_COMMENT"),
    DELETE_COMMENT(4, "DELETE_COMMENT");

    private final int position;
    private final String code;

    CategoryPermission(int position, String code) {
        this.position = position;
        this.code = code;
    }

    public int getPosition(){
        return position;
    }

    @Override
    public String getCode() {
        return code;
    }

    //프론트에서 문자열로 파라미터를 넘겼을 때 Enum으로 변환 (대소문자 구분x)
    public static CategoryPermission fromCode(String value) {
        return Arrays.stream(CategoryPermission.values())
                .filter(e -> e.name().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid permission"));
    }
}
