package com.example.board.domain.comment;

import com.example.board.domain.BaseTimeEntity;
import com.example.board.domain.member.Member;
import com.example.board.domain.post.Post;
import com.example.board.domain.teamMember.TeamMember;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.parameters.P;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Comment extends BaseTimeEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Member author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "team_member_id")
    private TeamMember teamMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comment parent;

    private int depth;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    @OrderBy("createdDate ASC")
    private List<Comment> replies = new ArrayList<>();

    private boolean isDeleted = false;

    private Comment(String content, Post post, Member author, TeamMember teamMember, Comment parent) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Content cannot be null or empty");
        }
        if (post == null) {
            throw new IllegalArgumentException("Post cannot be null");
        }
        if (author == null) {
            throw new IllegalArgumentException("Author cannot be null");
        }

        this.content = content;
        this.post = post;
        this.author = author;
        this.teamMember = teamMember; // null 허용
        this.parent = parent; // null 허용

        // depth 계산: 부모가 있으면 부모 depth + 1, 없으면 0
        this.depth = (parent != null) ? parent.getDepth() + 1 : 0;

        // 기본값 설정
        this.isDeleted = false;
        this.replies = new ArrayList<>();
    }

    public void setPost(Post post){
        this.post = post;
    }

    public void deleteByAuthor(){
        this.isDeleted = true;
        this.content = "작성자에 의해 삭제된 댓글입니다.";
    }

    public void delete(){
        this.isDeleted = true;
        this.content = "삭제된 댓글입니다.";
    }

    public static Comment createComment(String content,Post post, Member author, TeamMember teamMember, Comment parent){
        return new Comment(content, post, author, teamMember, parent);
    }

    /**
     * 답글 추가 - 양방향 관계 자동 설정[1]
     */
    public void addReply(Comment reply) {
        if (reply == null) {
            throw new IllegalArgumentException("Reply cannot be null");
        }

        // 깊이 제한 검사 (예: 5단계까지)
        if (this.depth >= 4) {
            throw new IllegalArgumentException("Maximum reply depth exceeded");
        }

        this.replies.add(reply);
        reply.setParentInternal(this);
    }

    /**
     * 답글 제거 - 양방향 관계 자동 해제[1]
     */
    public void removeReply(Comment reply) {
        if (reply != null) {
            this.replies.remove(reply);
            reply.setParentInternal(null);
        }
    }

    /**
     * 부모 설정 (내부 메서드)
     */
    protected void setParentInternal(Comment parent) {
        this.parent = parent;
        // depth 자동 계산
        this.depth = (parent != null) ? parent.getDepth() + 1 : 0;
    }

    /**
     * 모든 답글 제거
     */
    public void clearReplies() {
        this.replies.forEach(reply -> reply.setParentInternal(null));
        this.replies.clear();
    }

    /**
     * 계층 구조의 모든 댓글 수집 (재귀적)
     */
    public List<Comment> getAllDescendants() {
        List<Comment> descendants = new ArrayList<>();
        for (Comment reply : replies) {
            descendants.add(reply);
            descendants.addAll(reply.getAllDescendants());
        }
        return descendants;
    }

    /**
     * 특정 깊이의 답글들만 조회
     */
    public List<Comment> getRepliesByDepth(int targetDepth) {
        return replies.stream()
                .filter(reply -> reply.getDepth() == targetDepth)
                .collect(Collectors.toList());
    }

    /**
     * 삭제된 댓글인지 확인하면서 답글 수 계산
     */
    public long getActiveRepliesCount() {
        return replies.stream()
                .filter(reply -> !reply.isDeleted())
                .count();
    }
}
