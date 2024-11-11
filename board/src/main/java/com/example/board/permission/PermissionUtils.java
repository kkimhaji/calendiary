package com.example.board.permission;

import org.springframework.stereotype.Component;

@Component
public class PermissionUtils {

//    // 권한 추가
//    public static String addPermission(String currentPermissions, long permissionToAdd) {
//        long current = TeamPermission.fromString(currentPermissions);
//        long updated = current | permissionToAdd;
//        return TeamPermission.toBinaryString(updated);
//    }
//
//    // 권한 제거
//    public static String removePermission(String currentPermissions, long permissionToRemove) {
//        long current = TeamPermission.fromString(currentPermissions);
//        long updated = current & ~permissionToRemove;
//        return TeamPermission.toBinaryString(updated);
//    }
//
//    // 권한 확인
//    public static boolean hasPermission(String permissions, long permissionToCheck) {
//        long current = TeamPermission.fromString(permissions);
//        return (current & permissionToCheck) == permissionToCheck;
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
