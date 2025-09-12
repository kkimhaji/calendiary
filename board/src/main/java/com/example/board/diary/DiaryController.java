package com.example.board.diary;

import com.example.board.auth.UserPrincipal;
import com.example.board.diary.dto.*;
import com.example.board.image.ImageDomain;
import com.example.board.image.ImageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/diary")
@RequiredArgsConstructor
@Validated
public class DiaryController {
    private final DiaryService diaryService;
    private final ImageService imageService;

    @PostMapping
    public ResponseEntity<Long> createDiary(
            @RequestBody @Valid CreateDiaryRequest request,
            @AuthenticationPrincipal UserPrincipal user) throws IOException {

        Diary diary = diaryService.createDiary(request, user.getMember());
        return ResponseEntity.ok(diary.getId());
    }

    @GetMapping("/{diaryId}")
    public ResponseEntity<DiaryDetailResponse> getDiary(@PathVariable("diaryId") Long diaryId,
                                                        @AuthenticationPrincipal UserPrincipal user) {
        return ResponseEntity.ok(diaryService.getDiary(diaryId, user.getMember()));
    }

    @GetMapping("/list/daily")
    public ResponseEntity<List<DiaryListDTO>> getDiariesByDate(
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @RequestParam("day") int day,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {
        return ResponseEntity.ok(diaryService.findDiariesByDate(userPrincipal.getMember(), year, month, day));
//        try {
//            List<DiaryListDTO> diaries = diaryService.findDiariesByDate(userPrincipal.getMember(), year, month, day);
//
//            return ResponseEntity.ok(diaries);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.emptyList());
//        }
    }

    @GetMapping("/list/monthly")
    public ResponseEntity<List<DiaryListDTO>> getDiariesByMonth(
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @AuthenticationPrincipal UserPrincipal userPrincipal) {

        return ResponseEntity.ok(diaryService.findDiariesByMonth(userPrincipal.getMember(), year, month));
//        try {
//            List<DiaryListDTO> diaries = diaryService.findDiariesByMonth(userPrincipal.getMember(), year, month);
//
//            return ResponseEntity.ok(diaries);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body(Collections.emptyList());
//        }
    }

    @PutMapping("/{diaryId}")
    public ResponseEntity<DiaryDetailResponse> updateDiary(
            @PathVariable("diaryId") Long diaryId,
            @RequestBody @Valid UpdateDiaryRequest request,
            @AuthenticationPrincipal UserPrincipal user) throws IOException {

        return ResponseEntity.ok(diaryService.updateDiary(diaryId, request, user.getMember()));
    }

    @DeleteMapping("/{diaryId}")
    public ResponseEntity<Void> deleteDiary(@PathVariable("diaryId") Long diaryId,
                                            @AuthenticationPrincipal UserPrincipal user) throws IOException {
        diaryService.deleteDiary(diaryId, user.getMember());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/images/temp-upload")
    public ResponseEntity<String> uploadTempImage(@RequestParam("file") MultipartFile file) throws IOException {
        return ResponseEntity.ok(imageService.uploadTempImage(file, ImageDomain.DIARY));
//        try {
//            String imageUrl = imageService.uploadTempImage(file, ImageDomain.DIARY);
//            return ResponseEntity.ok(imageUrl);
//        } catch (IOException e) {
//            return ResponseEntity.internalServerError().build();
//        }
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<DiaryCalendarDTO>> getMonthlyDiaries(
            @RequestParam("year") int year,
            @RequestParam("month") @Min(1) @Max(12) int month,
            @AuthenticationPrincipal UserPrincipal user) {

        return ResponseEntity.ok(diaryService.findMonthlyDiaries(user.getMember(), year, month));
    }


}