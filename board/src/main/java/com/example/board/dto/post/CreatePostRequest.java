package com.example.board.dto.post;

import com.example.board.domain.member.Member;
import com.example.board.domain.post.Post;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamCategory;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record CreatePostRequest(
        String title,
        String content,
        List<MultipartFile> images
) {
    public Post toEntity(Team team, TeamCategory category, Member author){
        return Post.builder()
                .title(title)
                .content(content)
                .team(team)
                .category(category)
                .author(author)
                .build();
    }
}
