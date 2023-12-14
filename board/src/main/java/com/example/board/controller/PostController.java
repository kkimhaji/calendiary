package com.example.board.controller;

import com.example.board.dto.PostSaveRequestDto;
import com.example.board.dto.PostUpdateRequestDto;
import com.example.board.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping("/post")
    public Long save(@RequestBody PostSaveRequestDto dto){
        return postService.savePost(dto);
    }

    @PostMapping("/update")
    public Long update(@RequestBody PostUpdateRequestDto dto, Long id){
        return postService.updatePost(dto, id);
    }
}
