package com.example.board.diary;

import com.example.board.common.service.EntityValidationService;
import com.example.board.diary.dto.DiaryResponse;
import com.example.board.post.enums.SearchType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DiarySearchService {
    private final DiaryRepository diaryRepository;
    private final EntityValidationService validationService;

    public Page<DiaryResponse> searchDiaries(
            Long memberId,
            String keyword,
            Pageable pageable,
            SearchType searchType
    ) {
        validationService.validateMemberExists(memberId);

        if (searchType == null) searchType = SearchType.BOTH;
        if (keyword == null || keyword.trim().isEmpty()) return Page.empty(pageable);

        return switch (searchType) {
            case TITLE -> diaryRepository.searchByTitle(keyword, memberId, pageable).map(DiaryResponse::from);
            case CONTENT -> diaryRepository.searchByContent(keyword, memberId, pageable).map(DiaryResponse::from);
            case BOTH, default -> diaryRepository.searchByTitleOrContent(keyword, memberId, pageable).map(DiaryResponse::from);
        };
    }
}
