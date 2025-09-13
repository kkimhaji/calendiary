package com.example.board.diary;

import com.example.board.common.domain.BaseContentEntity;
import com.example.board.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "diary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Diary extends BaseContentEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Visibility visibility = Visibility.PRIVATE;  // 공개 범위

    @Column(name = "thumbnail_image_url")
    private String thumbnailImageUrl;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<DiaryImage> images = new ArrayList<>();

    //일기 날짜(작성 날짜와 구분)
    @Column(name = "diary_date", nullable = false)
    private LocalDate diaryDate;

    private Diary(String title,
                  String content,
                  Member author,
                  Visibility visibility,
                  LocalDate diaryDate) {

        super(title, content, author);
        this.visibility = visibility;
        this.diaryDate = diaryDate != null ? diaryDate : LocalDate.now(); // null이면 오늘 날짜
    }

    public static Diary create(String title,
                               String content,
                               Member author,
                               Visibility visibility, LocalDate diaryDate) {

        return new Diary(title, content, author, visibility, diaryDate);
    }

    public static Diary create(String title,
                               String content,
                               Member author,
                               Visibility visibility) {
        return new Diary(title, content, author, visibility, LocalDate.now());
    }

    public void update(String title, String content, LocalDate diaryDate) {
        changeTitleAndContent(title, content);
        this.diaryDate = diaryDate != null ? diaryDate : this.diaryDate;
    }

    public void update(String title, String content) {
        changeTitleAndContent(title, content);
    }

    public void addImage(DiaryImage image) {
        images.add(image);
        if (image.getDiary() != this) {
            image.setDiary(this);
        }
    }

    public void changeDiaryDate(LocalDate diaryDate) {
        this.diaryDate = diaryDate;
    }

    public void removeImage(DiaryImage image) {
        images.remove(image);
        image.setDiary(null);
    }

    public void clearImages() {
        images.forEach(img -> img.setDiary(null));
        images.clear();
    }

    public void changeVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public void setThumbnail(String imageUrl) {
        this.thumbnailImageUrl = imageUrl;
    }
}

