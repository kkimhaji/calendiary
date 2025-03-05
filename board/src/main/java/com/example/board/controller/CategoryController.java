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
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/teams/{teamId}/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("/create")
    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_CATEGORIES)")
    public ResponseEntity<CategoryResponse> createCategory(@PathVariable(name="teamId") @P("teamId") Long teamId, @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.ok(CategoryResponse.from(categoryService.createCategory(teamId, request)));
    }

    @PutMapping("/{categoryId}/update")
    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_CATEGORIES)")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable(name="teamId") @P("teamId") Long teamId, @PathVariable(name="categoryId") Long categoryId, @RequestBody UpdateCategoryRequest request){
        return ResponseEntity.ok(categoryService.updateCategory(teamId, categoryId, request));
    }

    //팀의 카테고리 리스트 받아오기
    @GetMapping
    public ResponseEntity<List<CategoryListDTO>> getCategories(@PathVariable(name="teamId") Long teamId){
        return ResponseEntity.ok(categoryService.getCategoryListByTeam(teamId));
    }

    //카테고리 정보 받아오기
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryInfo(@PathVariable(name = "teamId") Long teamId, @PathVariable(name="categoryId") Long categoryId){
        return ResponseEntity.ok(categoryService.getCategoryInfo(categoryId));
    }
}