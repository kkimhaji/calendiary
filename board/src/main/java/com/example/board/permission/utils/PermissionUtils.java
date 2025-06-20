package com.example.board.permission.utils;

import com.example.board.permission.PermissionType;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class PermissionUtils {
    /**
     * 권한 비트열에 새로운 권한 추가
     */
    public static <T extends Enum<T> & PermissionType> String addPermission(String current, T permission) {
        StringBuilder binary = new StringBuilder(current);
        while (binary.length() <= permission.getPosition()) {
            binary.insert(0, "0");
        }
        binary.setCharAt(binary.length() - 1 - permission.getPosition(), '1');
        return binary.toString();
    }

    /**
     * 권한 비트열에서 특정 권한 확인 (문자열 기반)
     */
    public static <T extends Enum<T> & PermissionType> boolean hasPermission(String permissions, T permission) {
        if (permissions == null || permissions.isEmpty() ||
                permission.getPosition() >= permissions.length()) {
            return false;
        }
        return permissions.charAt(permissions.length() - 1 - permission.getPosition()) == '1';
    }

    /**
     * 권한 비트열에서 특정 권한 확인 (바이트 배열 기반 - 최적화)
     */
    public static <T extends Enum<T> & PermissionType> boolean hasPermission(byte[] permissionBytes, T permission) {
        return PermissionConverter.hasPermissionOptimized(permissionBytes, permission);
    }

    /**
     * 권한 목록으로부터 권한 바이트 배열 생성 (최적화)
     */
    public static <T extends PermissionType> byte[] createPermissionBytes(Set<T> permissions) {
        return PermissionConverter.createPermissionBytes(permissions);
    }

    /**
     * 권한 바이트 배열로부터 권한 목록 추출 (최적화)
     */
    public static <T extends Enum<T> & PermissionType> Set<T> getPermissionsFromBytes(
            byte[] permissionBytes, Class<T> enumClass) {

        return PermissionConverter.getPermissionsFromBytes(permissionBytes, enumClass);
    }
}