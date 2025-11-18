package com.example.board.category.dto;

import java.util.List;

public record CategoryReorderRequest(
    List<Long> categoryIds
) {
        public void validate() {
            if (categoryIds == null || categoryIds.isEmpty()) {
                throw new IllegalArgumentException("카테고리 ID 목록이 비어있습니다.");
            }
        }
}