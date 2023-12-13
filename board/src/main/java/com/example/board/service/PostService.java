package com.example.board.service;

import com.example.board.domain.post.PostRepository;
import com.example.board.dto.PostSaveRequestDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    @Transactional
    public Long savePost(PostSaveRequestDto requestDto){
        return postRepository.save(requestDto.toEntity()).getPostId();
    }
}
