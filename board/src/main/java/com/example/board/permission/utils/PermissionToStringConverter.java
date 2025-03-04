package com.example.board.permission.utils;

import com.example.board.permission.PermissionType;
import org.springframework.stereotype.Component;
import org.springframework.core.convert.converter.Converter;

@Component
public class PermissionToStringConverter implements Converter<PermissionType, String> {
    @Override
    public String convert(PermissionType source) {
        return source.getCode();
    }
}
