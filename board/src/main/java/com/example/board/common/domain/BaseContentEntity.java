package com.example.board.common.domain;

import com.example.board.member.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseContentEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Lob
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    // 자식 클래스에서만 호출하도록 보호 수준을 PROTECTED 로 유지
    protected BaseContentEntity(String title, String content, Member author) {
        this.title   = title;
        this.content = content;
        this.author  = author;
    }

    // 제목·본문 수정 시 사용
    protected void changeTitleAndContent(String title, String content) {
        this.title   = title;
        this.content = content;
    }
}