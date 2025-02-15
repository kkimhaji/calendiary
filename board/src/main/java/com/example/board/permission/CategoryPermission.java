package com.example.board.permission;

public enum CategoryPermission implements PermissionType {
    VIEW_POST(0),
    CREATE_POST(1),
    DELETE_POST(2),
    CREATE_COMMENT(3),
    DELETE_COMMENT(4);

    private final int position;

    CategoryPermission(int position){
        this.position = position;
    }

    public int getPosition(){
        return position;
    }
}
