package com.example.board.service;

import com.example.board.domain.member.Member;
import com.example.board.domain.post.Post;
import com.example.board.domain.post.PostRepository;
import com.example.board.domain.team.TeamRepository;
import com.example.board.dto.post.PostListResponseDto;
import com.example.board.dto.post.PostResponseDto;
import com.example.board.dto.post.PostSaveRequestDto;
import com.example.board.dto.post.PostUpdateRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberService memberService;
    private final TeamRepository teamRepository;

}
