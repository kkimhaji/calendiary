package com.example.board.dto.post;

import com.example.board.domain.post.Comment;
import com.example.board.domain.post.Post;
import com.example.board.domain.post.PostImage;
import com.example.board.dto.comment.CommentResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.board.dto.comment.CommentResponse.convertToResponse;

public record PostDetailDTO(
        Long id,
        String title,
        String content,
        AuthorDTO author,
        String categoryName,
        LocalDateTime createdDate,
        List<String> imageUrls,
        List<CommentResponse> comments
) {
    public static PostDetailDTO from(Post post){
        List<Comment> allComments = post.getComments();

        // 최상위 댓글만 필터링
        List<Comment> rootComments = allComments.stream()
                .filter(comment -> comment.getParent() == null)
                .collect(Collectors.toList());

        // 계층 구조 생성
        List<CommentResponse> commentResponses = rootComments.stream()
                .map(comment -> convertToResponse(comment, allComments))
                .collect(Collectors.toList());


        return new PostDetailDTO(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                AuthorDTO.from(post.getAuthor()),
                post.getCategory().getName(),
                post.getCreatedDate(),
                post.getImages().stream()
                                .map(PostImage::getImageUrl)
                                        .collect(Collectors.toList()),
//                post.getComments().stream()
//                        .filter(comment -> comment.getParent() == null)
//                        .map(CommentResponse::from)
//                        .collect(Collectors.toList())
                commentResponses
        );
    }
}
