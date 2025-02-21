package com.example.board.dto.role;

public record EditAndDeletePermissionResponse(
        boolean canEdit,
        boolean canDelete
) {
    public static EditAndDeletePermissionResponse of(boolean canEdit, boolean canDelete){
        return new EditAndDeletePermissionResponse(canEdit, canDelete);
    }
}
