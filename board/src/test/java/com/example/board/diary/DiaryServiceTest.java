package com.example.board.diary;

import com.example.board.diary.dto.*;
import com.example.board.support.AbstractTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiaryServiceTest extends AbstractTestSupport {

    @Autowired
    private DiaryService diaryService;

    @Autowired
    private DiaryRepository diaryRepository;

    /* ===== CREATE ===== */
    @Test
    @DisplayName("일기 생성 - 성공")
    void createDiary_Success() throws IOException {
        // Given
        CreateDiaryRequest request = new CreateDiaryRequest(
                "오늘의 일기",
                "오늘은 즐거운 하루였다.",
                Visibility.PRIVATE
        );

        // When
        Diary result = diaryService.createDiary(request, member1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getTitle()).isEqualTo("오늘의 일기");
        assertThat(result.getContent()).isEqualTo("오늘은 즐거운 하루였다.");
        assertThat(result.getAuthor()).isEqualTo(member1);
        assertThat(result.getVisibility()).isEqualTo(Visibility.PRIVATE);
        assertThat(result.getCreatedDate()).isNotNull();
    }

    /* ===== READ ===== */
    @Test
    @DisplayName("일기 조회 - 성공 (작성자 본인)")
    void getDiary_Success_Owner() throws IOException {
        // Given
        CreateDiaryRequest request = new CreateDiaryRequest(
                "개인 일기", "비밀 내용", Visibility.PRIVATE);
        Diary savedDiary = diaryService.createDiary(request, member1);

        // When
        DiaryDetailResponse result = diaryService.getDiary(savedDiary.getId(), member1);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.diaryId()).isEqualTo(savedDiary.getId());
        assertThat(result.title()).isEqualTo("개인 일기");
        assertThat(result.content()).isEqualTo("비밀 내용");
        assertThat(result.authorNickname()).isEqualTo(member1.getNickname());
        assertThat(result.visibility()).isEqualTo("PRIVATE");
    }

    @Test
    @DisplayName("일기 조회 - 실패 (권한 없음)")
    void getDiary_Fail_AccessDenied() throws IOException {
        // Given
        CreateDiaryRequest request = new CreateDiaryRequest(
                "비공개 일기", "다른 사람이 볼 수 없는 내용", Visibility.PRIVATE);
        Diary savedDiary = diaryService.createDiary(request, member1);

        // When & Then
        assertThatThrownBy(() -> diaryService.getDiary(savedDiary.getId(), member2))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("본인만 열람할 수 있습니다.");
    }

    @Test
    @DisplayName("일기 조회 - 성공 (공개 일기는 다른 사용자도 조회 가능)")
    void getDiary_Success_PublicDiary() throws IOException {
        // Given
        CreateDiaryRequest request = new CreateDiaryRequest(
                "공개 일기", "모두가 볼 수 있는 내용", Visibility.PUBLIC);
        Diary savedDiary = diaryService.createDiary(request, member1);

        // When
        DiaryDetailResponse result = diaryService.getDiary(savedDiary.getId(), member2);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("공개 일기");
        assertThat(result.visibility()).isEqualTo("PUBLIC");
    }

    /* ===== UPDATE ===== */
    @Test
    @DisplayName("일기 수정 - 성공")
    void updateDiary_Success() throws IOException {
        // Given
        CreateDiaryRequest createRequest = new CreateDiaryRequest(
                "원래 제목", "원래 내용", Visibility.PRIVATE);
        Diary savedDiary = diaryService.createDiary(createRequest, member1);

        UpdateDiaryRequest updateRequest = new UpdateDiaryRequest(
                "수정된 제목", "수정된 내용", Visibility.PUBLIC, List.of());

        // When
        DiaryDetailResponse result = diaryService.updateDiary(
                savedDiary.getId(), updateRequest, member1);

        // Then
        assertThat(result.title()).isEqualTo("수정된 제목");
        assertThat(result.content()).isEqualTo("수정된 내용");
        assertThat(result.visibility()).isEqualTo("PUBLIC");

        // DB에서 직접 확인
        Diary updatedDiary = diaryRepository.findById(savedDiary.getId()).orElseThrow();
        assertThat(updatedDiary.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedDiary.getVisibility()).isEqualTo(Visibility.PUBLIC);
    }

    @Test
    @DisplayName("일기 수정 - 실패 (권한 없음)")
    void updateDiary_Fail_AccessDenied() throws IOException {
        // Given
        CreateDiaryRequest createRequest = new CreateDiaryRequest(
                "member1의 일기", "내용", Visibility.PRIVATE);
        Diary savedDiary = diaryService.createDiary(createRequest, member1);

        UpdateDiaryRequest updateRequest = new UpdateDiaryRequest(
                "해킹 시도", "악의적 수정", Visibility.PUBLIC, List.of());

        // When & Then
        assertThatThrownBy(() -> diaryService.updateDiary(
                savedDiary.getId(), updateRequest, member2))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("본인만 수정할 수 있습니다.");
    }

    /* ===== DELETE ===== */
    @Test
    @DisplayName("일기 삭제 - 성공")
    void deleteDiary_Success() throws IOException {
        // Given
        CreateDiaryRequest request = new CreateDiaryRequest(
                "삭제할 일기", "내용", Visibility.PRIVATE);
        Diary savedDiary = diaryService.createDiary(request, member1);
        Long diaryId = savedDiary.getId();

        // When
        diaryService.deleteDiary(diaryId, member1);

        // Then
        assertThat(diaryRepository.findById(diaryId)).isEmpty();
    }

    @Test
    @DisplayName("일기 삭제 - 실패 (권한 없음)")
    void deleteDiary_Fail_AccessDenied() throws IOException {
        // Given
        CreateDiaryRequest request = new CreateDiaryRequest(
                "member1의 일기", "내용", Visibility.PRIVATE);
        Diary savedDiary = diaryService.createDiary(request, member1);

        // When & Then
        assertThatThrownBy(() -> diaryService.deleteDiary(savedDiary.getId(), member2))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("본인만 삭제할 수 있습니다.");
    }

    /* ===== LIST ===== */
    @Test
    @DisplayName("작성자별 일기 목록 조회")
    void findByAuthor_Success() throws IOException {
        // Given
        CreateDiaryRequest request1 = new CreateDiaryRequest(
                "일기1", "내용1", Visibility.PRIVATE);
        CreateDiaryRequest request2 = new CreateDiaryRequest(
                "일기2", "내용2", Visibility.PUBLIC);

        diaryService.createDiary(request1, member1);
        diaryService.createDiary(request2, member1);
        diaryService.createDiary(new CreateDiaryRequest(
                "다른 사람 일기", "내용", Visibility.PRIVATE), member2);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<DiaryListResponse> result = diaryService.findByAuthor(member1, pageable);

        // Then
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).hasSize(2);

        List<String> titles = result.getContent().stream()
                .map(DiaryListResponse::title)
                .toList();
        assertThat(titles).containsExactlyInAnyOrder("일기1", "일기2");
    }

    /* ===== CALENDAR ===== */
    @Test
    @DisplayName("월별 캘린더 데이터 조회")
    void findMonthlyDiaries_Success() throws IOException {
        // Given
        CreateDiaryRequest request1 = new CreateDiaryRequest(
                "8월 일기1", "내용1", Visibility.PRIVATE);
        CreateDiaryRequest request2 = new CreateDiaryRequest(
                "8월 일기2", "내용2", Visibility.PUBLIC);

        diaryService.createDiary(request1, member1);
        diaryService.createDiary(request2, member1);

        // When
        List<DiaryCalendarDTO> result = diaryService.findMonthlyDiaries(member1, 2025, 8);

        // Then
        assertThat(result).hasSize(2);
        result.forEach(dto -> {
            LocalDate date = dto.date(); // 헬퍼 메서드 사용
            assertThat(date.getYear()).isEqualTo(2025);
            assertThat(date.getMonthValue()).isEqualTo(8);
        });
    }

    @Test
    @DisplayName("다른 사용자의 캘린더 데이터는 조회되지 않음")
    void findMonthlyDiaries_OnlyOwnDiaries() throws IOException {
        // Given
        diaryService.createDiary(new CreateDiaryRequest(
                "member1 일기", "내용", Visibility.PRIVATE), member1);
        diaryService.createDiary(new CreateDiaryRequest(
                "member2 일기", "내용", Visibility.PRIVATE), member2);

        // When
        List<DiaryCalendarDTO> result = diaryService.findMonthlyDiaries(member1, 2025, 8);

        // Then
        assertThat(result).hasSize(1);
    }
}