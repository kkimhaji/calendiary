package com.example.board.diary;

import com.example.board.common.service.EntityValidationService;
import com.example.board.config.HtmlSanitizer;
import com.example.board.diary.dto.*;
import com.example.board.image.ImageDomain;
import com.example.board.image.ImageService;
import com.example.board.member.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DiaryService {
    private final DiaryRepository diaryRepository;
    private final DiaryImageRepository diaryImageRepository;
    private final ImageService imageService;
    private final HtmlSanitizer htmlSanitizer;
    private final EntityValidationService validationService;

    @Transactional
    public Diary createDiary(CreateDiaryRequest req, Member author) throws IOException {
        String sanitized = htmlSanitizer.sanitize(req.content());
        String processedContent = imageService.processContentImages(sanitized, ImageDomain.DIARY);

        Diary diary = Diary.create(req.title(), processedContent, author, req.visibility());

        registerContentImages(diary, processedContent);

        return diaryRepository.save(diary);
    }

    @Transactional
    public DiaryDetailResponse updateDiary(Long diaryId, UpdateDiaryRequest req, Member author) throws IOException {
        Diary diary = validationService.validateDiaryExists(diaryId);

        if (!diary.getAuthor().getMemberId().equals(author.getMemberId())) {
            throw new AccessDeniedException("본인만 수정할 수 있습니다.");
        }

        String sanitized = htmlSanitizer.sanitize(req.content());
        String processedContent = imageService.processContentImages(sanitized, ImageDomain.DIARY);

        // 삭제할 이미지 처리
        if (!req.deleteImageIds().isEmpty()) {
            deleteImages(diary, req.deleteImageIds());
        }

        // 일기 업데이트
        diary.update(req.title(), processedContent);
        diary.changeVisibility(req.visibility());

        // 새로운 이미지 등록 (증분 방식)
        registerNewContentImages(diary, processedContent);

        return DiaryDetailResponse.from(diary);
    }


    /* ——— CREATE ——— */
//    @Transactional
//    public Diary createDiary(CreateDiaryRequest req, Member author) throws IOException {
//
//        String sanitized = htmlSanitizer.sanitize(req.content());
//        String finalHtml = imageService.processContentImages(sanitized, ImageDomain.DIARY);
//
//        Diary diary = Diary.create(
//                req.title(),
//                finalHtml,
//                author,
//                req.visibility()
//        );
//
//        registerContentImages(diary, finalHtml);
//
//        return diaryRepository.save(diary);
//    }

    /* ——— READ (상세) ——— */
    public DiaryDetailResponse getDiary(Long diaryId, Member requester) {

        Diary diary = validationService.validateDiaryExists(diaryId);

        if (diary.getVisibility() == Visibility.PRIVATE &&
                !diary.getAuthor().getMemberId().equals(requester.getMemberId())) {
            throw new AccessDeniedException("본인만 열람할 수 있습니다.");
        }
        return DiaryDetailResponse.from(diary);
    }

    /* ——— UPDATE ——— */
//    @Transactional
//    public DiaryDetailResponse updateDiary(Long diaryId,
//                                           UpdateDiaryRequest req,
//                                           Member author) throws IOException {
//
//        Diary diary = validationService.validateDiaryExists(diaryId);
//
//        if (!diary.getAuthor().equals(author)) {
//            throw new AccessDeniedException("본인만 수정할 수 있습니다.");
//        }
//
//        String sanitized = htmlSanitizer.sanitize(req.content());
//        String finalHtml = imageService.processContentImages(sanitized, ImageDomain.DIARY);
//
//        if (req.deleteImageIds() != null && !req.deleteImageIds().isEmpty()) {
//            deleteImages(diary, req.deleteImageIds());
//        }
//
//        diary.update(req.title(), finalHtml);
//        diary.changeVisibility(req.visibility());
//
//        registerContentImages(diary, finalHtml);
//
//        return DiaryDetailResponse.from(diary);
//    }

    /* ——— DELETE ——— */
    @Transactional
    public void deleteDiary(Long diaryId, Member author) throws IOException {

        Diary diary = validationService.validateDiaryExists(diaryId);

        if (!diary.getAuthor().equals(author)) {
            throw new AccessDeniedException("본인만 삭제할 수 있습니다.");
        }

        for (DiaryImage img : diary.getImages()) {
            imageService.deleteImage(img.getStoredFileName(), ImageDomain.DIARY);
        }
        diary.clearImages();

        diaryRepository.delete(diary);
    }

    /* ——— MONTHLY CALENDAR ——— */
    public List<DiaryCalendarDTO> findMonthlyDiaries(Member author, int year, int month) {
        // 월의 시작과 끝을 LocalDateTime으로 설정
        LocalDateTime startDateTime = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusMonths(1); // 다음 달 1일 00:00:00

        return diaryRepository.findCalendarData(
                author.getMemberId(),
                startDateTime,
                endDateTime
        );
    }

    // ✅ 특정 날짜의 일기 리스트 조회 (년&월&일)
    public List<DiaryListDTO> findDiariesByDate(Member author, int year, int month, int day) {
        // 해당 날짜의 시작과 끝 시간 설정
        LocalDateTime startDateTime = LocalDateTime.of(year, month, day, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusDays(1); // 다음 날 00:00:00

        return diaryRepository.findDiaryListData(
                author.getMemberId(),
                startDateTime,
                endDateTime
        );
    }

    // ✅ 월별 일기 리스트 조회 (년&월)
    public List<DiaryListDTO> findDiariesByMonth(Member author, int year, int month) {
        // 월의 시작과 끝 시간 설정
        LocalDateTime startDateTime = LocalDateTime.of(year, month, 1, 0, 0, 0);
        LocalDateTime endDateTime = startDateTime.plusMonths(1); // 다음 달 1일 00:00:00

        return diaryRepository.findDiaryListData(
                author.getMemberId(),
                startDateTime,
                endDateTime
        );
    }

    public Page<DiaryListResponse> findByAuthor(Member author, Pageable pageable) {
        return diaryRepository.findByAuthor(author.getMemberId(), pageable);
    }

    //내부 이미지 처리
    private void registerContentImages(Diary diary, String html) {

        List<String> permUrls = imageService.extractImageUrlsFromContent(html).stream()
                .filter(u -> u.startsWith(ImageDomain.DIARY.permPrefix()))
                .toList();

        for (String url : permUrls) {
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            DiaryImage img = DiaryImage.create(diary, fileName, fileName);
            diary.addImage(img);
        }

        permUrls.stream().findFirst().ifPresent(diary::setThumbnail);
    }

    private void deleteImages(Diary diary, List<Long> imageIds) throws IOException {

        List<DiaryImage> images = diaryImageRepository.findAllByIdIn(imageIds);

        for (DiaryImage img : images) {
            if (!img.getDiary().getId().equals(diary.getId())) {
                throw new IllegalArgumentException("잘못된 이미지 ID입니다.");
            }
            imageService.deleteImage(img.getStoredFileName(), ImageDomain.DIARY);
            diary.removeImage(img);
            diaryImageRepository.delete(img);
        }
    }

    private void registerNewContentImages(Diary diary, String html) {
        List<String> newUrls = imageService.extractImageUrlsFromContent(html).stream()
                .filter(u -> u.startsWith(ImageDomain.DIARY.permPrefix()))
                .filter(u -> diary.getImages().stream()
                        .noneMatch(img -> u.endsWith(img.getStoredFileName())))
                .toList();

        for (String url : newUrls) {
            String fileName = url.substring(url.lastIndexOf('/') + 1);
            DiaryImage img = DiaryImage.create(diary, fileName, fileName);
            diary.addImage(img);
        }

        // 썸네일 재설정
        List<String> allUrls = imageService.extractImageUrlsFromContent(html).stream()
                .filter(u -> u.startsWith(ImageDomain.DIARY.permPrefix()))
                .toList();
        allUrls.stream().findFirst().ifPresent(diary::setThumbnail);
    }

}
