package com.example.board.diary;

import com.example.board.common.exception.DiaryNotFoundException;
import com.example.board.diary.dto.CreateDiaryRequest;
import com.example.board.diary.dto.DiaryCalendarDTO;
import com.example.board.diary.dto.DiaryDetailResponse;
import com.example.board.diary.dto.UpdateDiaryRequest;
import com.example.board.image.ImageService;
import com.example.board.member.Member;
import com.example.board.support.AbstractControllerTestSupport;
import com.example.board.support.TestDataBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.example.board.diary.Visibility.PUBLIC;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class DiaryControllerTest extends AbstractControllerTestSupport {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private DiaryService diaryService;
    @Autowired
    private TestDataBuilder testDataBuilder;
    @MockBean
    private ImageService imageService;

    @Test
    @WithMockUser(username = "test1@test.com")
    @DisplayName("일기 생성 성공")
    void createDiary_success() throws Exception {
        // Given
        CreateDiaryRequest request = new CreateDiaryRequest(
                "오늘의 일기",
                "<p>오늘은 좋은 하루였다.</p>",
                PUBLIC,
                LocalDate.of(2025, 9, 3) // diaryDate 추가
        );
        Member testMember = principal1.getMember();
        Diary mockDiary = Diary.create("오늘의 일기", "<p>오늘은 좋은 하루였다.</p>", testMember, PUBLIC);
        ReflectionTestUtils.setField(mockDiary, "id", 1L);

        given(diaryService.createDiary(any(CreateDiaryRequest.class), any(Member.class)))
                .willReturn(mockDiary);

        // When & Then
        mockMvc.perform(post("/diary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(principal1))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));

        verify(diaryService).createDiary(any(CreateDiaryRequest.class), any(Member.class));
    }

    @Test
    @WithMockUser(username = "test1@test.com")
    @DisplayName("일기 생성 실패 - 제목 누락")
    void createDiary_missingTitle_badRequest() throws Exception {
        // Given
        CreateDiaryRequest request = new CreateDiaryRequest(
                "", // 빈 제목
                "내용입니다.",
                Visibility.PRIVATE,
                LocalDate.now()
        );

        // When & Then
        mockMvc.perform(post("/diary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(principal1))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test1@test.com")
    @DisplayName("일기 생성 실패 - visibility 누락")
    void createDiary_missingVisibility_badRequest() throws Exception {
        // Given - visibility가 null인 요청 (JSON에서 누락)
        String invalidJson = "{\"title\":\"제목\",\"content\":\"내용\",\"diaryDate\":\"2025-09-03\"}"; // visibility 없음

        // When & Then
        mockMvc.perform(post("/diary")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson)
                        .with(user(principal1))
                        .with(csrf()))
                .andExpect(status().isBadRequest());
    }

    /* ===== READ 테스트 ===== */

    @Test
    @WithMockUser(username = "test1@test.com")
    @DisplayName("일기 상세 조회 성공")
    void getDiary_success() throws Exception {
        // Given
        Long diaryId = 1L;
        DiaryDetailResponse mockResponse = new DiaryDetailResponse(
                diaryId,
                "일기 제목",
                "<p>일기 내용</p>",
                "테스트사용자",
                LocalDateTime.of(2025, 9, 3, 14, 30),
                LocalDate.of(2025, 9, 3), // diaryDate 추가
                "PUBLIC",
                Arrays.asList("https://example.com/image1.jpg", "https://example.com/image2.jpg")
        );

        given(diaryService.getDiary(eq(diaryId), any(Member.class)))
                .willReturn(mockResponse);

        // When & Then
        mockMvc.perform(get("/diary/{diaryId}", diaryId)
                        .with(user(principal1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diaryId").value(diaryId))
                .andExpect(jsonPath("$.title").value("일기 제목"))
                .andExpect(jsonPath("$.content").value("<p>일기 내용</p>"))
                .andExpect(jsonPath("$.authorNickname").value("테스트사용자"))
                .andExpect(jsonPath("$.diaryDate").value("2025-09-03"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.imageUrls", hasSize(2)))
                .andExpect(jsonPath("$.imageUrls[0]").value("https://example.com/image1.jpg"));

        verify(diaryService).getDiary(eq(diaryId), any(Member.class));
    }

    @Test
    @WithMockUser
    @DisplayName("일기 조회 실패 - 존재하지 않는 일기")
    void getDiary_notFound() throws Exception {
        // Given
        Long diaryId = 999L;

        given(diaryService.getDiary(eq(diaryId), any(Member.class)))
                .willThrow(new DiaryNotFoundException());

        // When & Then
        mockMvc.perform(get("/diary/{diaryId}", diaryId)
                        .with(user(principal1)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("일기 조회 - 실패 (권한 없음)")
    void getDiary_Fail_AccessDenied() throws Exception {
        // Given
        Long diaryId = 1L;

        given(diaryService.getDiary(eq(diaryId), any(Member.class)))
                .willThrow(new AccessDeniedException("본인만 열람할 수 있습니다."));

        // When & Then
        mockMvc.perform(get("/diary/{diaryId}", diaryId)
                        .with(user(principal2)))
                .andExpect(status().isForbidden());

        verify(diaryService).getDiary(eq(diaryId), any(Member.class));
    }

    @Test
    @DisplayName("일기 조회 - 성공 (공개 일기)")
    void getDiary_Success_PublicDiary() throws Exception {
        // Given
        Long diaryId = 2L;

        DiaryDetailResponse publicDiaryResponse = new DiaryDetailResponse(
                diaryId,
                "공개 일기",
                "모두가 볼 수 있는 내용",
                "테스트사용자1",
                LocalDateTime.of(2025, 9, 3, 10, 30),
                LocalDate.of(2025, 9, 3),
                "PUBLIC",
                List.of("https://example.com/public-image.jpg")
        );

        given(diaryService.getDiary(eq(diaryId), any(Member.class)))
                .willReturn(publicDiaryResponse);

        // When & Then
        mockMvc.perform(get("/diary/{diaryId}", diaryId)
                        .with(user(principal2)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diaryId").value(diaryId))
                .andExpect(jsonPath("$.title").value("공개 일기"))
                .andExpect(jsonPath("$.content").value("모두가 볼 수 있는 내용"))
                .andExpect(jsonPath("$.visibility").value("PUBLIC"))
                .andExpect(jsonPath("$.authorNickname").value("테스트사용자1"));

        verify(diaryService).getDiary(eq(diaryId), any(Member.class));
    }

    @Test
    @DisplayName("일기 조회 - 성공 (작성자 본인)")
    @WithMockUser(username = "test1@test.com")
    void getDiary_Success_AuthorAccess() throws Exception {
        // Given
        Long diaryId = 1L;

        DiaryDetailResponse privateDiaryResponse = new DiaryDetailResponse(
                diaryId,
                "비공개 일기",
                "비밀 내용",
                "작성자",
                LocalDateTime.of(2025, 9, 3, 10, 30),
                LocalDate.of(2025, 9, 3),
                "PRIVATE",
                Collections.emptyList()
        );

        given(diaryService.getDiary(eq(diaryId), any(Member.class)))
                .willReturn(privateDiaryResponse);

        // When & Then
        mockMvc.perform(get("/diary/{diaryId}", diaryId)
                        .with(user(principal1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diaryId").value(diaryId))
                .andExpect(jsonPath("$.title").value("비공개 일기"))
                .andExpect(jsonPath("$.visibility").value("PRIVATE"));
    }

    /* ===== UPDATE 테스트 ===== */
    @Test
    @DisplayName("일기 수정 성공")
    void updateDiary_success() throws Exception {
        // Given
        Long diaryId = 1L;
        UpdateDiaryRequest request = new UpdateDiaryRequest(
                "수정된 제목",
                "<p>수정된 내용</p>",
                Visibility.PRIVATE,
                LocalDate.of(2025, 9, 5), // diaryDate 추가
                Arrays.asList(1L, 2L)
        );

        DiaryDetailResponse mockResponse = new DiaryDetailResponse(
                diaryId,
                "수정된 제목",
                "<p>수정된 내용</p>",
                "테스트사용자",
                LocalDateTime.now(),
                LocalDate.of(2025, 9, 5),
                "PRIVATE",
                Collections.emptyList()
        );

        given(diaryService.updateDiary(eq(diaryId), any(), any())).willReturn(mockResponse);

        // When & Then
        mockMvc.perform(put("/diary/{diaryId}", diaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                        .with(user(principal1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.diaryDate").value("2025-09-05"));
    }

    @Test
    @DisplayName("일기 수정 - 실패 (다른 사용자의 일기)")
    void updateDiary_Fail_NotAuthor() throws Exception {
        // Given
        Long diaryId = 1L;
        UpdateDiaryRequest request = new UpdateDiaryRequest(
                "수정 시도",
                "수정 내용",
                Visibility.PRIVATE,
                LocalDate.now(),
                Collections.emptyList()
        );

        given(diaryService.updateDiary(eq(diaryId), any(UpdateDiaryRequest.class), any(Member.class)))
                .willThrow(new AccessDeniedException("다른 사용자의 일기를 수정할 수 없습니다."));

        // When & Then
        mockMvc.perform(put("/diary/{diaryId}", diaryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(csrf())
                        .with(user(principal2)))
                .andExpect(status().isForbidden());
    }

    /* ===== DELETE 테스트 ===== */
    @Test
    @DisplayName("일기 삭제 성공")
    void deleteDiary_success() throws Exception {
        // Given
        Long diaryId = 1L;
        doNothing().when(diaryService).deleteDiary(eq(diaryId), any());

        // When & Then
        mockMvc.perform(delete("/diary/{diaryId}", diaryId)
                        .with(csrf())
                        .with(user(principal1)))
                .andExpect(status().isOk());

        verify(diaryService).deleteDiary(eq(diaryId), any());
    }

    @Test
    @DisplayName("일기 삭제 - 실패 (다른 사용자의 일기)")
    void deleteDiary_Fail_NotAuthor() throws Exception {
        // Given
        Long diaryId = 1L;

        doThrow(new AccessDeniedException("다른 사용자의 일기를 삭제할 수 없습니다."))
                .when(diaryService).deleteDiary(eq(diaryId), any(Member.class));

        // When & Then
        mockMvc.perform(delete("/diary/{diaryId}", diaryId)
                        .with(csrf())
                        .with(user(principal2)))
                .andExpect(status().isForbidden());
    }

    /* ===== CALENDAR 테스트 ===== */
    @Test
    @WithMockUser
    @DisplayName("월별 일기 조회 성공")
    void getMonthlyDiaries_success() throws Exception {
        // Given
        int year = 2025;
        int month = 9;

        List<DiaryCalendarDTO> mockDiaries = Arrays.asList(
                new DiaryCalendarDTO(
                        1L,
                        "9월 1일 일기",
                        LocalDateTime.of(2025, 9, 1, 14, 30),
                        "https://example.com/thumbnail1.jpg",
                        3L
                ),
                new DiaryCalendarDTO(
                        2L,
                        "9월 15일 일기",
                        LocalDateTime.of(2025, 9, 15, 10, 15),
                        "https://example.com/thumbnail2.jpg",
                        1L
                ),
                new DiaryCalendarDTO(
                        3L,
                        "9월 30일 일기",
                        LocalDateTime.of(2025, 9, 30, 20, 45),
                        null, // 썸네일 없음
                        0L
                )
        );

        given(diaryService.findMonthlyDiaries(any(Member.class), eq(year), eq(month)))
                .willReturn(mockDiaries);

        // When & Then
        mockMvc.perform(get("/diary/calendar")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month))
                        .with(user(principal1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].diaryId").value(1L))
                .andExpect(jsonPath("$[0].title").value("9월 1일 일기"))
                .andExpect(jsonPath("$[0].diaryDate").value("2025-09-01"))
                .andExpect(jsonPath("$[0].thumbnailImageUrl").value("https://example.com/thumbnail1.jpg"))
                .andExpect(jsonPath("$[0].imageCount").value(3L))
                .andExpect(jsonPath("$[1].diaryId").value(2L))
                .andExpect(jsonPath("$[1].imageCount").value(1L))
                .andExpect(jsonPath("$[2].diaryId").value(3L))
                .andExpect(jsonPath("$[2].thumbnailImageUrl").doesNotExist()) // null 값
                .andExpect(jsonPath("$[2].imageCount").value(0L));

        verify(diaryService).findMonthlyDiaries(any(Member.class), eq(year), eq(month));
    }

    @Test
    @DisplayName("월별 일기 조회 성공 - 빈 결과")
    void getMonthlyDiaries_emptyResult() throws Exception {
        // Given
        int year = 2025;
        int month = 1;

        given(diaryService.findMonthlyDiaries(any(Member.class), eq(year), eq(month)))
                .willReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/diary/calendar")
                        .param("year", String.valueOf(year))
                        .param("month", String.valueOf(month))
                        .with(user(principal1)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "test1@test.com")
    @DisplayName("월별 일기 조회 실패 - 잘못된 파라미터")
    void getMonthlyDiaries_invalidParameters() throws Exception {
        // When & Then - 잘못된 월
        mockMvc.perform(get("/diary/calendar")
                        .param("year", "2025")
                        .param("month", "13") // 잘못된 월
                        .with(user(principal1)))
                .andExpect(status().isBadRequest());

        // When & Then - 음수 월
        mockMvc.perform(get("/diary/calendar")
                        .param("year", "2025")
                        .param("month", "0") // 잘못된 월
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
