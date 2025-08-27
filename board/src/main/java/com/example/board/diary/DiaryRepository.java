package com.example.board.diary;

import com.example.board.diary.dto.DiaryCalendarDTO;
import com.example.board.diary.dto.DiaryListResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    /* 달력 뷰용 – 특정 기간(월) 내 데이터 조회 */
    @Query("""
       select new com.example.board.diary.dto.DiaryCalendarDTO(
               d.id,
               date(d.createdDate),
               d.thumbnailImageUrl,
               count(i)
       )
       from Diary d
       left join d.images i
       where d.author.memberId = :memberId
         and date(d.createdDate) between :start and :end
       group by d.id, date(d.createdDate), d.thumbnailImageUrl
       """)
    List<DiaryCalendarDTO> findCalendarData(Long memberId,
                                            LocalDate start,
                                            LocalDate end);

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
    Page<DiaryListResponse> findByAuthor(Long memberId, Pageable pageable);
}