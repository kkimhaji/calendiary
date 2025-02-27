package com.example.board.permission.utils;

import org.springframework.cache.annotation.Cacheable;

public class OptimizingPermissionUtils {
    private static final int CHUNK_SIZE = 8;

    // 문자열을 바이트 배열로 변환하여 처리
    public static byte[] stringToBytes(String permissions) {
        int length = permissions.length();
        byte[] bytes = new byte[(length + CHUNK_SIZE - 1) / CHUNK_SIZE];
        for (int i = 0; i < length; i++) {
            if (permissions.charAt(i) == '1') {
                int byteIndex = i / CHUNK_SIZE;
                int bitIndex = i % CHUNK_SIZE;
                bytes[byteIndex] |= (1 << bitIndex);
            }
        }
        return bytes;
    }

    // 캐싱을 통한 성능 최적화
    @Cacheable("permissions")
    public static boolean hasPermissionOptimized(String permissions,
                                                 int position) {
        byte[] bytes = stringToBytes(permissions);
        int byteIndex = position / CHUNK_SIZE;
        int bitIndex = position % CHUNK_SIZE;
        return byteIndex < bytes.length &&
                (bytes[byteIndex] & (1 << bitIndex)) != 0;
    }
}
