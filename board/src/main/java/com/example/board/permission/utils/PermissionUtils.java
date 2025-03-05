package com.example.board.permission.utils;

import com.example.board.permission.PermissionType;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class PermissionUtils {

    public static <T extends Enum<T> & PermissionType> String addPermission(String current, T permission) {
        StringBuilder binary = new StringBuilder(current);
        while (binary.length() <= permission.getPosition()) {
            binary.insert(0, "0");
        }
        binary.setCharAt(binary.length() - 1 - permission.getPosition(), '1');
        return binary.toString();
    }

    public static <T extends Enum<T> & PermissionType> boolean hasPermission(String permissions, T permission) {
        if (permission.getPosition() >= permissions.length()) {
            return false;
        }
        return permissions.charAt(permissions.length() - 1 - permission.getPosition()) == '1';
    }

//    public static <T extends Enum<T> & PermissionType> String createPermissionBits(Set<T> permissions) {
//        String bits = "0";
//        for (T permission : permissions) {
//            bits = addPermission(bits, permission);
//        }
//        return bits;
//    }

    public static <T extends PermissionType> String createPermissionBits(Set<T> permissions) {
        if (permissions.isEmpty()) return "0";

        int maxPosition = permissions.stream()
                .mapToInt(PermissionType::getPosition)
                .max()
                .orElse(0);

        char[] bits = new char[maxPosition + 1];
        Arrays.fill(bits, '0');
        permissions.forEach(p -> bits[p.getPosition()] = '1');
        return new String(bits);
    }

    public static <T extends Enum<T> & PermissionType> Set<T> getPermissionsFromBits(
            String permissionBits, Class<T> enumClass) {
        Set<T> permissions = new HashSet<>();
        for (T permission : enumClass.getEnumConstants()) {
            if (hasPermission(permissionBits, permission)) {
                permissions.add(permission);
            }
        }
        return permissions;
    }

}
