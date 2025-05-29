package com.example.board.dto.role;

import com.example.board.permission.TeamPermission;
import com.example.board.permission.utils.PermissionUtils;

import java.util.Set;

public record RoleUpdateRequest(
        String roleName,
        String description,
        Set<TeamPermission> permissions
) {
    /**
     * 권한을 byte[]로 변환하는 메서드[3]
     */
    public byte[] toPermissionBytes() {
        if (permissions == null || permissions.isEmpty()) {
            return new byte[0];
        }
        return PermissionUtils.createPermissionBytes(permissions);
    }

    /**
     * 권한을 String으로 변환하는 메서드 (호환성)
     */
    public String toPermissionBits() {
        if (permissions == null || permissions.isEmpty()) {
            return "0";
        }
        return PermissionUtils.createPermissionBits(permissions);
    }

    /**
     * 유효성 검증 메서드
     */
    public void validate() {
        if (roleName != null && roleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Role name cannot be empty");
        }
        // 추가 유효성 검증 로직
    }
}