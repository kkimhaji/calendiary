package com.example.board.diary;

import com.example.board.diary.dto.DiaryCalendarDTO;
import com.example.board.diary.dto.DiaryListDTO;
import com.example.board.diary.dto.DiaryListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    /* 달력 뷰용 – 특정 기간(월) 내 데이터 조회 */
    @Query("""
       select new com.example.board.diary.dto.DiaryCalendarDTO(
               d.id,
               d.createdDate,
               d.thumbnailImageUrl,
               count(i)
       )
       from Diary d
       left join d.images i
       where d.author.memberId = :memberId
         and d.createdDate >= :startDateTime
         and d.createdDate < :endDateTime
       group by d.id, d.createdDate, d.thumbnailImageUrl
       """)
    List<DiaryCalendarDTO> findCalendarData(@Param("memberId") Long memberId,
                                            @Param("startDateTime") LocalDateTime startDateTime,
                                            @Param("endDateTime") LocalDateTime endDateTime);


    /* 목록(무한 스크롤) */
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

    // ✅ 리스트용 메서드 (상세 정보 포함)
    @Query("SELECT new com.example.board.diary.dto.DiaryListDTO(d.id, d.title, d.content, d.createdDate, d.visibility) " +
            "FROM Diary d WHERE d.author.memberId = :authorId " +
            "AND d.createdDate >= :startDate AND d.createdDate < :endDate " +
            "ORDER BY d.createdDate DESC")
    List<DiaryListDTO> findDiaryListData(@Param("authorId") Long authorId,
                                         @Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    // ✅ 또는 Entity를 반환하는 방식
    @Query("SELECT d FROM Diary d WHERE d.author.memberId = :authorId " +
            "AND d.createdDate >= :startDate AND d.createdDate < :endDate " +
            "ORDER BY d.createdDate DESC")
    List<Diary> findDiariesBetweenDates(@Param("authorId") Long authorId,
                                        @Param("startDate") LocalDateTime startDate,
                                        @Param("endDate") LocalDateTime endDate);
}