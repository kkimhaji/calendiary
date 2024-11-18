package com.example.board.permission;

import com.example.board.domain.team.TeamCategory;
import com.example.board.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CategoryPermissionEvaluator {
    private final CategoryService categoryService;

}
