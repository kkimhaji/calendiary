package com.example.board.service;

import com.example.board.domain.member.Member;
import com.example.board.domain.post.Post;
import com.example.board.domain.post.PostRepository;
import com.example.board.dto.PostListResponseDto;
import com.example.board.dto.PostResponseDto;
import com.example.board.dto.PostSaveRequestDto;
import com.example.board.dto.PostUpdateRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberService memberService;

    @Transactional
    public Long savePost(HttpServletRequest request, PostSaveRequestDto requestDto){
        Member loginUser = memberService.getMember(request);
        return postRepository.save(requestDto.toEntity(loginUser)).getPostId();
    }

    @Transactional
    public Long updatePost(PostUpdateRequestDto requestDto, Long postId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id="+postId));;

        post.update(requestDto.getTitle(), requestDto.getContent());
        return postId;
    }

    @Transactional
    public void deletePost(Long postId){
        Post post = postRepository.findById(postId).orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다."));
        postRepository.delete(post);
    }

    public List<PostListResponseDto> getPostListByUser(HttpServletRequest request){
        Member loginUser = memberService.getMember(request);
        return postRepository.findAllByAuthor(loginUser).stream()
                .map(PostListResponseDto::new).collect(Collectors.toList());
    }

    public PostResponseDto readPost(Long postId){
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 없습니다. id="+postId));

        return new PostResponseDto(post);
    }

}
