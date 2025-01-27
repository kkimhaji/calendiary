package com.example.board.dto.role;

public record PostPermissionResponse(
        boolean canEdit,
        boolean canDelete
) {
    public static PostPermissionResponse of(boolean canEdit, boolean canDelete){
        return new PostPermissionResponse(canEdit, canDelete);
    }
}
