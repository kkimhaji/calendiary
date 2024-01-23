package com.example.board.controller;

import com.example.board.dto.post.PostListResponseDto;
import com.example.board.dto.post.PostResponseDto;
import com.example.board.dto.post.PostSaveRequestDto;
import com.example.board.dto.post.PostUpdateRequestDto;
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
    public Long save(@RequestBody PostSaveRequestDto dto, HttpServletRequest request){
        return postService.savePost(request, dto);
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
    public PostResponseDto readPost(@PathVariable Long id){
        return postService.readPost(id);
    }

}
