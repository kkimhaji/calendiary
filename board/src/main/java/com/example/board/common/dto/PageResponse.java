package com.example.board.common.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,       // 현재 페이지의 데이터 목록
        int currentPage,       // 현재 페이지 번호 (0-based)
        int pageSize,          // 페이지 당 항목 수
        int totalPages,       // 전체 페이지 수
        long totalElements    // 전체 항목 개수
) {
    // ✅ Spring Data Page 객체를 편리하게 변환하기 위한 팩토리 메서드
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalPages(),
                page.getTotalElements()
        );
    }
}
