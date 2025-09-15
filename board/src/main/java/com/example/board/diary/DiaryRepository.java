package com.example.board.diary;

import com.example.board.diary.dto.DiaryCalendarDTO;
import com.example.board.diary.dto.DiaryListDTO;
import com.example.board.diary.dto.DiaryListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    @Query("""
           select new com.example.board.diary.dto.DiaryCalendarDTO(
                   d.id,
                   d.title,
                   d.diaryDate,
                   d.createdDate,
                   d.thumbnailImageUrl,
                   count(i)
           )
           from Diary d
           left join d.images i
           where d.author.memberId = :memberId
             and COALESCE(d.diaryDate, DATE(d.createdDate)) >= :startDate
             and COALESCE(d.diaryDate, DATE(d.createdDate)) <= :endDate
           group by d.id, d.title, d.diaryDate, d.createdDate, d.thumbnailImageUrl
           order by COALESCE(d.diaryDate, DATE(d.createdDate)) asc
           """)
    List<DiaryCalendarDTO> findCalendarData(@Param("memberId") Long memberId,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    // 리스트용 메서드 - diaryDate 기준으로 수정
    @Query("SELECT new com.example.board.diary.dto.DiaryListDTO(d.id, d.title, d.content, d.createdDate, d.diaryDate, CAST(d.visibility AS string)) " +
            "FROM Diary d WHERE d.author.memberId = :authorId " +
            "AND d.diaryDate >= :startDate AND d.diaryDate <= :endDate " +
            "ORDER BY d.diaryDate DESC, d.createdDate DESC")
    List<DiaryListDTO> findDiaryListData(@Param("authorId") Long authorId,
                                         @Param("startDate") LocalDate startDate,
                                         @Param("endDate") LocalDate endDate);

    // Entity를 반환하는 방식 - diaryDate 기준
    @Query("SELECT d FROM Diary d WHERE d.author.memberId = :authorId " +
            "AND d.diaryDate >= :startDate AND d.diaryDate <= :endDate " +
            "ORDER BY d.diaryDate DESC, d.createdDate DESC")
    List<Diary> findDiariesBetweenDates(@Param("authorId") Long authorId,
                                        @Param("startDate") LocalDate startDate,
                                        @Param("endDate") LocalDate endDate);

    // 목록용 - createdDate 기준으로 유지 (실제 작성 순서)
    @Query("""
            select new com.example.board.diary.dto.DiaryListResponse(
                     d.id,
                     d.title,
                     d.createdDate,
                     d.thumbnailImageUrl
            )
            from Diary d
            where d.author.memberId = :memberId
            order by d.createdDate desc
            """)
    Page<DiaryListResponse> findByAuthor(@Param("memberId") Long memberId, Pageable pageable);

}