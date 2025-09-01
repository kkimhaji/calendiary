package com.example.board.diary;

import com.example.board.auth.UserPrincipal;
import com.example.board.diary.dto.CreateDiaryRequest;
import com.example.board.diary.dto.DiaryCalendarDTO;
import com.example.board.diary.dto.DiaryDetailResponse;
import com.example.board.diary.dto.UpdateDiaryRequest;
import com.example.board.image.ImageDomain;
import com.example.board.image.ImageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/diary")
@RequiredArgsConstructor
public class DiaryController {
    private final DiaryService diaryService;
    private final ImageService imageService;

//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<Long> createDiary(@Valid @ModelAttribute CreateDiaryRequest request,
//                                            @AuthenticationPrincipal UserPrincipal user) throws IOException {
//        Diary diary = diaryService.createDiary(request, user.getMember());
//        return ResponseEntity.ok(diary.getId());
//    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
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

//    @PutMapping("/{diaryId}")
//    public ResponseEntity<DiaryDetailResponse> updateDiary(@PathVariable("diaryId") Long diaryId,
//                                                           @Valid @ModelAttribute UpdateDiaryRequest request,
//                                                           @AuthenticationPrincipal UserPrincipal user) throws IOException {
//        return ResponseEntity.ok(diaryService.updateDiary(diaryId, request, user.getMember()));
//    }

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
    public ResponseEntity<String> uploadTempImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = imageService.uploadTempImage(file, ImageDomain.DIARY);
            return ResponseEntity.ok(imageUrl);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<DiaryCalendarDTO>> getMonthlyDiaries(
            @RequestParam("year") int year,
            @RequestParam("month") int month,
            @AuthenticationPrincipal UserPrincipal user) {

        return ResponseEntity.ok(diaryService.findMonthlyDiaries(user.getMember(), year, month));
    }
}
