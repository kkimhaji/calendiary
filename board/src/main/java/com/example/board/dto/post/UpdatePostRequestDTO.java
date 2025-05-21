package com.example.board.dto.post;

import com.example.board.domain.post.Post;
import com.example.board.domain.category.TeamCategory;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record UpdatePostRequestDTO(
        String title,
        String content,
        List<MultipartFile> images,
        List<Long> deleteImageIds
) {
    public Post toEntity(Post post, TeamCategory category, String processedContent){
        post.update(this.title, processedContent, category);

        return post;
    }
}
