package com.example.board.permission;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class PermissionUtils {

//    public static String initializePermissions() {
//        return "0";  // 초기 권한 없음
//    }
//
//    public static String addPermission(String currentPermissions, TeamPermission permission) {
//        StringBuilder binary = new StringBuilder(currentPermissions);
//        while (binary.length() <= permission.getPosition()) {
//            binary.insert(0, "0");
//        }
//        binary.setCharAt(binary.length() - 1 - permission.getPosition(), '1');
//        return binary.toString();
//    }
//
//    public static boolean hasPermission(String permissions, TeamPermission permission) {
//        if (permission.getPosition() >= permissions.length()) {
//            return false;
//        }
//        return permissions.charAt(permissions.length() - 1 - permission.getPosition()) == '1';
//    }


    public static String addPermission(String current, TeamPermission permission) {
        StringBuilder binary = new StringBuilder(current);
        while (binary.length() <= permission.getPosition()) {
            binary.insert(0, "0");
        }
        binary.setCharAt(binary.length() - 1 - permission.getPosition(), '1');
        return binary.toString();
    }

    public static boolean hasPermission(String permissions, TeamPermission permission) {
        if (permission.getPosition() >= permissions.length()) {
            return false;
        }
        return permissions.charAt(permissions.length() - 1 - permission.getPosition()) == '1';
    }

    public static String createPermissionBits(Set<TeamPermission> permissions) {
        String bits = "0";
        for (TeamPermission permission : permissions) {
            bits = addPermission(bits, permission);
        }
        return bits;
    }

    public static Set<TeamPermission> getPermissionsFromBits(String permissionBits) {
        Set<TeamPermission> permissions = new HashSet<>();
        for (TeamPermission permission : TeamPermission.values()) {
            if (hasPermission(permissionBits, permission)) {
                permissions.add(permission);
            }
        }
        return permissions;
    }


//String
//    public static String addPermission(String current, int position) {
//        char[] bits = current.toCharArray();
//        if (position >= bits.length) {
//            // 새로운 비트열 생성
//            char[] newBits = new char[position + 1];
//            Arrays.fill(newBits, '0');
//            System.arraycopy(bits, 0, newBits, newBits.length - bits.length, bits.length);
//            bits = newBits;
//        }
//        bits[bits.length - 1 - position] = '1';
//        return new String(bits);
//    }
//
//    public static String removePermission(String current, int position) {
//        if (position >= current.length()) {
//            return current;
//        }
//        char[] bits = current.toCharArray();
//        bits[bits.length - 1 - position] = '0';
//        return new String(bits);
//    }
//
//    public static boolean hasPermission(String permissions, int position) {
//        if (position >= permissions.length()) {
//            return false;
//        }
//        return permissions.charAt(permissions.length() - 1 - position) == '1';
//    }

}
