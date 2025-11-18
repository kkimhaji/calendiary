package com.example.board.category.dto;

public record CategoryOrderUpdateRequest(
        Integer newOrder
) {
    public void validate() {
        if (newOrder == null || newOrder < 0) {
            throw new IllegalArgumentException("순서는 0 이상이어야 합니다.");
        }
    }
}