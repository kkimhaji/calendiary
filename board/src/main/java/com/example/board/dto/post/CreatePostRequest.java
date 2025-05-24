package com.example.board.dto.post;

import com.example.board.domain.member.Member;
import com.example.board.domain.post.Post;
import com.example.board.domain.team.Team;
import com.example.board.domain.category.TeamCategory;
import com.example.board.domain.teamMember.TeamMember;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public record CreatePostRequest(
        String title,
        String content,
        List<MultipartFile> images
) {
    public Post toEntity(String safeContent, Team team, TeamCategory category, Member author, TeamMember teamMember){
        return Post.create(title, safeContent, author, category, team, teamMember);
    }
}