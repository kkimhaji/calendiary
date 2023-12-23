package com.example.board.controller;

import com.example.board.dto.PostListResponseDto;
import com.example.board.dto.PostResponseDto;
import com.example.board.dto.PostSaveRequestDto;
import com.example.board.dto.PostUpdateRequestDto;
import com.example.board.service.PostService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/post")
public class PostController {

    private final PostService postService;

    @PostMapping("/write")
    public Long save(@RequestBody PostSaveRequestDto dto){
        return postService.savePost(dto);
    }

    @PostMapping("/update/{id}")
    public Long update(@RequestBody PostUpdateRequestDto dto,@PathVariable Long id){
        return postService.updatePost(dto, id);
    }

    @DeleteMapping("/delete/{id}")
    public void delete(@PathVariable Long id){
        postService.deletePost(id);
    }

    @GetMapping("/list")
    public List<PostListResponseDto> getPostList(HttpServletRequest request){
        return postService.getPostListByUser(request);
    }

    @GetMapping("/read/{id}")
    public PostResponseDto readPost(@PathVariable Long postId){
        return postService.readPost(postId);
    }

}
