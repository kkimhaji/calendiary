package com.example.board.post;

import com.example.board.category.TeamCategory;
import com.example.board.config.HtmlSanitizer;
import com.example.board.image.ImageService;
import com.example.board.post.dto.CreatePostRequest;
import com.example.board.role.TeamRole;
import com.example.board.support.AbstractTestSupport;
import com.example.board.support.TestDataBuilder;
import com.example.board.support.TestDataFactory;
import com.example.board.team.Team;
import com.example.board.teamMember.TeamMember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PostCreateTest extends AbstractTestSupport {
    @Autowired
    private PostService postService;
    @Autowired
    private TestDataBuilder builder;
    @Autowired
    private TestDataFactory factory;
    private Team testTeam;
    private TeamMember teamMember;
    private TeamCategory testCategory;
    private TeamRole role;
    @MockBean
    private HtmlSanitizer htmlSanitizer;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private ImageService imageService;

    @BeforeEach
    void init(){
        testTeam = builder.createTeam(member1);
        teamMember = builder.addMemberToTeam(member2, testTeam.getId());
        role = builder.createNewRole(testTeam.getId(), "test role");
        testCategory = builder.createCategory(role.getId(), testTeam.getId(), "test category", new HashSet<>());
    }

    @Test
    void createPostTest() throws IOException {
        // given
        String title = "Test Post Title";
        String content = "Test post content without images";
        String sanitizedContent = "Sanitized test post content";

        when(htmlSanitizer.sanitize(content)).thenReturn(content);
        CreatePostRequest request = new CreatePostRequest(title, content, Collections.emptyList());
        Post createdPost = postService.createPost(testTeam.getId(), testCategory.getId(), request, member2);
        // then
        assertThat(createdPost).isNotNull();
        assertThat(createdPost.getTitle()).isEqualTo(title);
        assertThat(createdPost.getContent()).isEqualTo(content);
        assertThat(createdPost.getAuthor()).isEqualTo(member2);
        assertThat(createdPost.getCategory()).isEqualTo(testCategory);
        assertThat(createdPost.getTeam()).isEqualTo(testTeam);
        assertThat(createdPost.getTeamMember()).isEqualTo(teamMember);
        assertThat(createdPost.getImages()).isEmpty();

        // Repository에 저장되었는지 확인
        Optional<Post> savedPost = postRepository.findById(createdPost.getId());
        assertThat(savedPost).isPresent();
        verify(htmlSanitizer).sanitize(content);
    }
}
