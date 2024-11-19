package com.example.board.dto.category;

import com.example.board.domain.team.TeamCategory;

import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

//public record CategoryResponse(
//        Long id,
//        String name,
//        String description,
//        List<CategoryRolePermissionResponse> rolePermissions
//) {
//    public static CategoryResponse from(TeamCategory category){
//        return new CategoryResponse(
//                category.getId(),
//                category.getName(),
//                category.getDescription(),
//                category.getRolePermissions().stream()
//                        .map(CategoryRolePermissionResponse::from)
//                        .collect(Collectors.toList())
//        );
//    }
//}
