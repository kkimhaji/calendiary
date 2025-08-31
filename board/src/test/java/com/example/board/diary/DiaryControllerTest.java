package com.example.board.diary;

import com.example.board.common.exception.DiaryNotFoundException;
import com.example.board.diary.dto.CreateDiaryRequest;
import com.example.board.support.AbstractControllerTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.hamcrest.Matchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DiaryControllerTest extends AbstractControllerTestSupport {

    @Autowired
    private DiaryService diaryService;

    /* ===== CREATE 테스트 ===== */
    @Test
    @DisplayName("일기 생성 - 성공")
    @WithMockUser(username = "test1@test.com")
    void createDiary_Success() throws Exception {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "application/octet-stream", new byte[0]);

        // When & Then
        mockMvc.perform(multipart("/diary")
                        .file(emptyFile)  // 빈 파일
                        .param("title", "새로운 일기")
                        .param("content", "오늘은 좋은 하루였다.")
                        .param("visibility", "PRIVATE")
                        .with(user(principal1))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(content().string(matchesPattern("\\d+")));
    }

    @Test
    @DisplayName("일기 생성 - 실패 (빈 제목)")
    @WithMockUser(username = "test1@test.com")
    void createDiary_Fail_EmptyTitle() throws Exception {

        // When & Then
        mockMvc.perform(multipart("/diary")
                        .param("title", "")
                        .param("content", "오늘은 좋은 하루였다.")
                        .param("visibility", "PRIVATE")
                        .with(user(principal1))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest());
    }

    /* ===== READ 테스트 ===== */
    @Test
    @DisplayName("일기 조회 - 성공")
    @WithMockUser(username = "test1@test.com")
    void getDiary_Success() throws Exception {
        // Given
        given(memberService.findByEmail("test1@test.com")).willReturn(member1);

        CreateDiaryRequest request = new CreateDiaryRequest(
                "테스트 일기", "테스트 내용", Visibility.PRIVATE);
        Diary savedDiary = diaryService.createDiary(request, member1);

        // When & Then
        mockMvc.perform(get("/diary/{diaryId}", savedDiary.getId())
                        .with(user(principal1))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트 일기"))
                .andExpect(jsonPath("$.content").value("테스트 내용"));
    }

    @Test
    @DisplayName("일기 조회 - 실패 (권한 없음)")
    @WithMockUser(username = "test2@test.com")
    void getDiary_Fail_AccessDenied() throws Exception {
        // Given
        CreateDiaryRequest request = new CreateDiaryRequest(
                "비공개 일기", "비밀 내용", Visibility.PRIVATE);
        Diary savedDiary = diaryService.createDiary(request, member1);

        // When & Then
        mockMvc.perform(get("/diary/{diaryId}", savedDiary.getId())
                        .with(user(principal2)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("일기 조회 - 성공 (공개 일기)")
    @WithMockUser(username = "test2@test.com")
    void getDiary_Success_PublicDiary() throws Exception {
        // Given
        CreateDiaryRequest request = new CreateDiaryRequest(
                "공개 일기", "모두가 볼 수 있는 내용", Visibility.PUBLIC);
        Diary savedDiary = diaryService.createDiary(request, member1);

        // When & Then
        mockMvc.perform(get("/diary/{diaryId}", savedDiary.getId())
                        .with(user(principal2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("공개 일기"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"));
    }

    /* ===== UPDATE 테스트 ===== */
    @Test
    @DisplayName("일기 수정 - 성공")
    @WithMockUser(username = "test1@test.com")
    void updateDiary_Success() throws Exception {
        // Given
        given(memberService.findByEmail("test1@test.com")).willReturn(member1);

        CreateDiaryRequest createRequest = new CreateDiaryRequest(
                "원본 제목", "원본 내용", Visibility.PRIVATE);
        Diary savedDiary = diaryService.createDiary(createRequest, member1);

        MockMultipartFile emptyFile = new MockMultipartFile("file", "", "application/octet-stream", new byte[0]);

        // When & Then
        mockMvc.perform(multipart("/diary/{diaryId}", savedDiary.getId())
                        .file(emptyFile)
                        .param("title", "수정된 제목")
                        .param("content", "수정된 내용")
                        .param("visibility", "PUBLIC")
                        .param("deleteImageIds", "")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(user(principal1))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"));
    }

    @Test
    @DisplayName("일기 수정 - 실패 (권한 없음)")
    @WithMockUser(username = "test2@test.com")
    void updateDiary_Fail_AccessDenied() throws Exception {
        // Given
        CreateDiaryRequest createRequest = new CreateDiaryRequest(
                "원본 제목", "원본 내용", Visibility.PRIVATE);
        Diary savedDiary = diaryService.createDiary(createRequest, member1);

        // When & Then
        mockMvc.perform(multipart("/diary/{diaryId}", savedDiary.getId())
                        .param("title", "인증X")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(user(principal2)))
                .andExpect(status().isForbidden());
    }

    /* ===== DELETE 테스트 ===== */
    @Test
    @DisplayName("일기 삭제 - 성공")
    @WithMockUser(username = "test1@test.com")
    void deleteDiary_Success() throws Exception {
        // Given
        CreateDiaryRequest createRequest = new CreateDiaryRequest(
                "삭제할 일기", "삭제될 내용", Visibility.PRIVATE);
        Diary savedDiary = diaryService.createDiary(createRequest, member1);
        Long diaryId = savedDiary.getId();

        // When
        mockMvc.perform(delete("/diary/{diaryId}", diaryId)
                        .with(user(principal1)))
                .andExpect(status().isOk());

        // Then - 삭제 후 조회 시도
        assertThatThrownBy(() -> diaryService.getDiary(diaryId, member1))
                .isInstanceOf(DiaryNotFoundException.class);
    }

    @Test
    @DisplayName("일기 삭제 - 실패 (권한 없음)")
    @WithMockUser(username = "test2@test.com")
    void deleteDiary_Fail_AccessDenied() throws Exception {
        // Given
        CreateDiaryRequest createRequest = new CreateDiaryRequest(
                "삭제 시도할 일기", "내용", Visibility.PRIVATE);
        Diary savedDiary = diaryService.createDiary(createRequest, member1);

        // When & Then
        mockMvc.perform(delete("/diary/{diaryId}", savedDiary.getId())
                        .with(user(principal2)))
                .andExpect(status().isForbidden());
    }

    /* ===== IMAGE 테스트 ===== */
    @Test
    @DisplayName("임시 이미지 업로드 - 성공")
    @WithMockUser(username = "test2@test.com")
    void uploadTempImage_Success() throws Exception {
        // Given
        MockMultipartFile imageFile = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "fake image content".getBytes(StandardCharsets.UTF_8)
        );

        // When & Then
        mockMvc.perform(multipart("/diary/images/temp-upload")
                        .file(imageFile))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("/diary-temp-images/")));
    }

    /* ===== CALENDAR 테스트 ===== */
    @Test
    @DisplayName("월별 달력 조회 - 성공")
    @WithMockUser(username = "test1@test.com")
    void getMonthlyDiaries_Success() throws Exception {
        // Given
        CreateDiaryRequest request1 = new CreateDiaryRequest(
                "8월 일기1", "내용1", Visibility.PRIVATE);
        CreateDiaryRequest request2 = new CreateDiaryRequest(
                "8월 일기2", "내용2", Visibility.PUBLIC);

        diaryService.createDiary(request1, member1);
        diaryService.createDiary(request2, member1);

        // When & Then
        mockMvc.perform(get("/diary/calendar")
                        .param("year", "2025")
                        .param("month", "8")
                        .with(user(principal1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))))
                .andExpect(jsonPath("$[0].diaryId").exists())
                .andExpect(jsonPath("$[0].createdDateTime").exists());
    }

    @Test
    @DisplayName("월별 달력 조회 - 잘못된 매개변수")
    @WithMockUser(username = "test1@test.com")
    void getMonthlyDiaries_Fail_InvalidParams() throws Exception {
        // When & Then
        mockMvc.perform(get("/diary/calendar")
                        .param("year", "invalid")
                        .param("month", "13")
                        .with(user(principal1)))
                .andExpect(status().isBadRequest());
    }

    /* ===== 인증 테스트 ===== */
    @Test
    @DisplayName("인증되지 않은 사용자 접근 차단")
    void unauthenticatedAccess_Denied() throws Exception {
        mockMvc.perform(get("/diary/1"))
                .andExpect(status().isUnauthorized());
    }
}