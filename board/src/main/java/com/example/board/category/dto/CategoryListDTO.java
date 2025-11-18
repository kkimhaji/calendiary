package com.example.board.category.dto;

public record CategoryListDTO(
        Long id,
        String name,
        Integer displayOrder
) {
    // 기존 생성자 유지
    public CategoryListDTO(Long id, String name) {
        this(id, name, 0);
    }
}