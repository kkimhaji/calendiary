package com.example.board.category;

import com.example.board.auth.UserPrincipal;
import com.example.board.category.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @DeleteMapping("/{categoryId}/delete")
    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_CATEGORIES)")
    public ResponseEntity<Void> deleteCategory(@PathVariable("teamId") @P("teamId") Long teamId, @PathVariable("categoryId") Long categoryId){
        categoryService.deleteCategory(categoryId);
        return ResponseEntity.ok().build();
    }

    /**
     * 카테고리 순서 변경 (단일)
     */
    @PutMapping("/{categoryId}/order")
    public ResponseEntity<Void> updateCategoryOrder(
            @PathVariable("teamId") Long teamId,
            @PathVariable("categoryId") Long categoryId,
            @RequestBody CategoryOrderUpdateRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        request.validate();
        categoryService.updateCategoryOrder(categoryId, request.newOrder());
        return ResponseEntity.ok().build();
    }

    /**
     * 카테고리 순서 일괄 변경 (드래그 앤 드롭)
     */
    @PutMapping("/reorder")
    public ResponseEntity<Void> reorderCategories(
            @PathVariable("teamId") Long teamId,
            @RequestBody CategoryReorderRequest request,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        request.validate();
        categoryService.reorderCategories(teamId, request.categoryIds());
        return ResponseEntity.ok().build();
    }
}