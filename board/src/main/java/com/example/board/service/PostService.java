package com.example.board.service;

import com.example.board.domain.member.Member;
import com.example.board.domain.post.Post;
import com.example.board.domain.post.PostRepository;
import com.example.board.domain.role.TeamRole;
import com.example.board.domain.team.CategoryRepository;
import com.example.board.domain.team.Team;
import com.example.board.domain.team.TeamCategory;
import com.example.board.domain.team.TeamRepository;
import com.example.board.dto.post.*;
import com.example.board.permission.TeamPermission;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.hibernate.action.internal.EntityActionVetoException;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberService memberService;
    private final TeamRepository teamRepository;
    private final CategoryRepository categoryRepository;
    private final CategoryService categoryService;
    private final TeamMemberService teamMemberService;


    @Transactional
    public Post createPost(Long teamId, CreatePostRequest request, Member author) throws AccessDeniedException {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new EntityNotFoundException("Team not found"));

        TeamCategory category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));

        TeamRole memberRole = teamMemberService.getCurrentUserRole(teamId, author);
        //카테고리 권한 검사
        if (!categoryService.checkCategoryPermission(category.getId(), memberRole.getId(), TeamPermission.CREATE_POST)){
            throw new AccessDeniedException("해당 카테고리에 글을 작성할 권한이 없습니다.");
        }

        Post post = Post.builder()
                .title(request.title())
                .content(request.content())
                .team(team)
                .category(category)
                .author(author)
                .build();

        return postRepository.save(post);

    }

    @Transactional(readOnly = true)
    public Page<PostListResponse> getPostsByCategory(Long teamId, Long categoryId, Member member, Pageable pageable){
        TeamRole memberRole = teamMemberService.getCurrentUserRole(teamId, member);

        if (!categoryService.checkCategoryPermission(categoryId, memberRole.getId(), TeamPermission.VIEW_POST)){
            throw new AccessDeniedException("해당 카테고리를 조회할 권한이 없습니다.");
        }
        return postRepository.findAllByTeamAndCategoryWithPaging(teamId, categoryId, pageable)
                .map(PostListResponse::from);
    }

    //게시글 목록 조회
    @Transactional(readOnly = true)
    public Page<PostSummaryDTO> getCategoryPosts(Long categoryId, Pageable pageable){
        return postRepository.findPostSummariesByCategoryId(categoryId, pageable);
    }

    //게시글 상세 조회
    @Transactional(readOnly = true)
    public PostDetailDTO getPostDetail(Long postId, Long categoryId){
        Post post = postRepository.findByIdWithAuthor(postId, categoryId)
                .orElseThrow(()->new EntityNotFoundException("Post not found"));
        return PostDetailDTO.from(post);
    }

    //최근 게시글 조회
    @Transactional(readOnly = true)
    @Cacheable(value = "recentPosts", key = "#categoryId")
    public List<PostSummaryDTO> getRecentPosts(Long categoryId, int limit){
        return postRepository.findRecentPostsByCategoryId(categoryId, PageRequest.of(0, limit));
    }

//    //캐싱
//    @Cacheable(value = "categoryRecentPosts", key = "#categoryId")
//    public List<Post> getRecentPosts(Long categoryId, int limit) {
//        return postRepository.findTopByCategoryId(categoryId, PageRequest.of(0, limit));
//    }

}
