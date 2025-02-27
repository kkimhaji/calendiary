package com.example.board.permission.utils;

import com.example.board.permission.CategoryPermission;
import com.example.board.permission.PermissionType;
import com.example.board.permission.TeamPermission;
import org.springframework.core.convert.converter.Converter;

public class StringToPermissionConverter implements Converter<String, PermissionType> {

    @Override
    public PermissionType convert(String code) {
        // TeamPermission에서 먼저 검색
        try {
            return TeamPermission.fromCode(code);
        } catch (IllegalArgumentException e) {
            // TeamPermission에 없으면 CategoryPermission 검색
            return CategoryPermission.fromCode(code);
        }
    }
}
