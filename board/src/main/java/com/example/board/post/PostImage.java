package com.example.board.post;

import jakarta.persistence.*;
import lombok.AccessLevel;
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

    private PostImage(Post post, String originalFileName, String storedFileName) {
        this.post = post;
        this.originalFileName = originalFileName;
        this.storedFileName = storedFileName;
    }

    public static PostImage createPostImage(Post post, String originalFileName, String storedFileName){
        return new PostImage(post, originalFileName, storedFileName);
    }

    public String getImageUrl() {
        return "/images/" + this.storedFileName;
    }

    public void setPost(Post post) {
        this.post = post;
    }
}
