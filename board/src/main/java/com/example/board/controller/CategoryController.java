package com.example.board.controller;

import com.example.board.domain.team.TeamCategory;
import com.example.board.dto.category.CategoryResponse;
import com.example.board.dto.category.CreateCategoryRequest;
import com.example.board.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasPermission(@teamRepository.findById(#teamId).orElse(null), 'MANAGE_ROLES')")
    public ResponseEntity<CategoryResponse> createCategory(@PathVariable Long teamId, @RequestBody CreateCategoryRequest request) {
        TeamCategory category = categoryService.createCategory(teamId, request);
        return ResponseEntity.ok(CategoryResponse.from(category));
    }

    //category
}