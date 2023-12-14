package com.example.board.service;

import com.example.board.domain.post.Post;
import com.example.board.domain.post.PostRepository;
import com.example.board.dto.PostSaveRequestDto;
import com.example.board.dto.PostUpdateRequestDto;
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

    @Transactional
    public Long updatePost(PostUpdateRequestDto requestDto, Long postId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id="+postId));;

        post.update(requestDto.getTitle(), requestDto.getContent());
        return postId;
    }
}
