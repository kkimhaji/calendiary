package com.example.board.controller;

import com.example.board.domain.team.TeamCategory;
import com.example.board.dto.category.CategoryListDTO;
import com.example.board.dto.category.CategoryResponse;
import com.example.board.dto.category.CreateCategoryRequest;
import com.example.board.dto.category.UpdateCategoryRequest;
import com.example.board.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/create")
    @PreAuthorize("hasPermission(@teamRepository.findById(#teamId).orElse(null), 'MANAGE_CATEGORIES')")
    public ResponseEntity<CategoryResponse> createCategory(@PathVariable Long teamId, @RequestBody CreateCategoryRequest request) {
        TeamCategory category = categoryService.createCategory(teamId, request);
        return ResponseEntity.ok(CategoryResponse.from(category));
    }

    @PutMapping("/update/{categoryId}")
    @PreAuthorize("hasPermission(@teamRepository.findById(#teamId).orElse(null), 'MANAGE_CATEGORIES')")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable Long teamId, @PathVariable Long categoryId, @RequestBody UpdateCategoryRequest request){
        return ResponseEntity.ok(categoryService.updateCategory(teamId, categoryId, request));
    }

    @GetMapping
    public ResponseEntity<List<CategoryListDTO>> getCategories(@PathVariable Long teamId){
        return ResponseEntity.ok(categoryService.getCategoryListByTeam(teamId));
    }
}