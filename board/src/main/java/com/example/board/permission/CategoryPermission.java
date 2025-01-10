package com.example.board.permission;

public enum CategoryPermission implements PermissionType {
    VIEW_POST(0),
    CREATE_POST(1),
    EDIT_POST(2),
    DELETE_POST(3),
    CREATE_COMMENT(4),
    DELETE_COMMENT(5);

    private final int position;

    CategoryPermission(int position){
        this.position = position;
    }

    public int getPosition(){
        return position;
    }
}
