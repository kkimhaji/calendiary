package com.example.board.permission.utils;

import com.example.board.permission.PermissionType;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class PermissionConverter {
    private static final int CHUNK_SIZE = 8; // 8비트 = 1바이트

    /**
     * 권한 문자열을 바이트 배열로 변환
     */
    public static byte[] stringToBytes(String permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return new byte[0];
        }

        int length = permissions.length();
        byte[] bytes = new byte[(length + CHUNK_SIZE - 1) / CHUNK_SIZE];

        for (int i = 0; i < length; i++) {
            // 기존 권한 구현 방식은 역순으로 저장되어 있을 수 있음
            // 현재 구현에 맞게 인덱스 계산
            int position = length - 1 - i;
            if (position >= 0 && permissions.charAt(position) == '1') {
                int byteIndex = i / CHUNK_SIZE;
                int bitIndex = i % CHUNK_SIZE;
                bytes[byteIndex] |= (1 << bitIndex);
            }
        }

        return bytes;
    }

    /**
     * 바이트 배열을 권한 문자열로 변환
     */
    public static String bytesToString(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "0";
        }

        StringBuilder sb = new StringBuilder();
        int totalBits = bytes.length * CHUNK_SIZE;

        for (int i = 0; i < totalBits; i++) {
            int byteIndex = i / CHUNK_SIZE;
            int bitIndex = i % CHUNK_SIZE;

            if (byteIndex < bytes.length) {
                boolean isSet = (bytes[byteIndex] & (1 << bitIndex)) != 0;
                sb.append(isSet ? '1' : '0');
            }
        }

        // 불필요한 앞쪽 0 제거 (예: 00010 → 10)
        while (sb.length() > 1 && sb.charAt(0) == '0') {
            sb.deleteCharAt(0);
        }

        // 기존 코드와 호환성을 위해 역순으로 반환
        return sb.reverse().toString();
    }

    /**
     * 권한 목록을 바이트 배열로 변환
     */
    public static <T extends PermissionType> byte[] createPermissionBytes(Set<T> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            return new byte[0];
        }

        int maxPosition = permissions.stream()
                .mapToInt(PermissionType::getPosition)
                .max()
                .orElse(0);

        byte[] bytes = new byte[(maxPosition / 8) + 1];

        for (T permission : permissions) {
            int position = permission.getPosition();
            int byteIndex = position / 8;
            int bitIndex = position % 8;

            bytes[byteIndex] |= (1 << bitIndex);
        }

        return bytes;
    }

    /**
     * 바이트 배열에서 권한 존재 여부 확인 (캐싱 적용)
     */
    @Cacheable(value = "permission-checks", key = "#bytes.hashCode() + '-' + #permission.name()")
    public static <T extends Enum<T> & PermissionType> boolean hasPermissionOptimized(
            byte[] bytes, T permission) {

        if (bytes == null || bytes.length == 0) {
            return false;
        }

        int position = permission.getPosition();
        int byteIndex = position / 8;
        int bitIndex = position % 8;

        if (byteIndex >= bytes.length) {
            return false;
        }

        return (bytes[byteIndex] & (1 << bitIndex)) != 0;
    }

    /**
     * 바이트 배열에서 권한 목록 추출
     */
    public static <T extends Enum<T> & PermissionType> Set<T> getPermissionsFromBytes(
            byte[] bytes, Class<T> enumClass) {

        Set<T> permissions = new HashSet<>();
        T[] values = enumClass.getEnumConstants();

        for (T permission : values) {
            if (hasPermissionOptimized(bytes, permission)) {
                permissions.add(permission);
            }
        }

        return permissions;
    }
}