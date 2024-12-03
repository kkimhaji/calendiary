package com.example.board.domain.post;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Post post;

    private String originalFileName;
    private String storedFileName;

//    private String fileUrl;

    @Builder
    public PostImage(Post post, String originalFileName, String storedFileName) {
        this.post = post;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
//        this.fileUrl = fileUrl;
    }

    public String getImageUrl() {
        return "/images/" + this.storedFileName;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
