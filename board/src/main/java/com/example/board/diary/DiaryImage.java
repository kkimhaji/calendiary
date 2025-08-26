package com.example.board.diary;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DiaryImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Diary diary;

    private String originalFileName;
    private String storedFileName;

    private DiaryImage(Diary diary,
                       String originalFileName,
                       String storedFileName) {
        this.diary = diary;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
    }

    public static DiaryImage create(Diary diary,
                                    String originalFileName,
                                    String storedFileName) {
        return new DiaryImage(diary, originalFileName, storedFileName);
    }

    public String getImageUrl() {
        return "/diary-images/" + storedFileName;
    }

    /* Diary와의 양방향 연관관계 편의 메서드 */
    public void setDiary(Diary diary) {
        this.diary = diary;
    }
}

