package com.example.board.teamMember;

import com.example.board.auth.UserPrincipal;
import com.example.board.comment.CommentService;
import com.example.board.comment.dto.MemberCommentResponse;
import com.example.board.common.dto.PageResponse;
import com.example.board.member.dto.AddTeamMemberToRoleDTO;
import com.example.board.post.PostService;
import com.example.board.post.dto.PostListResponse;
import com.example.board.teamMember.dto.ChangeTeamNicknameRequest;
import com.example.board.teamMember.dto.MemberProfileResponse;
import com.example.board.teamMember.dto.TeamMemberInfoListDTO;
import com.example.board.teamMember.dto.TeamNicknameCheckResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/team/{teamId}")
public class TeamMemberController {
    private final TeamMemberService teamMemberService;
    private final PostService postService;
    private final CommentService commentService;

    //팀 정보에서 멤버 리스트 받아올 때
    @GetMapping("/members")
    public ResponseEntity<List<TeamMemberInfoListDTO>> getTeamMembersList(@PathVariable(name = "teamId") Long teamId) {
        return ResponseEntity.ok(teamMemberService.getTeamMembersWithRole(teamId));
    }

    @GetMapping("/get-members")
    public ResponseEntity<PageResponse<AddTeamMemberToRoleDTO>> getTeamMembersWithSearch(@PathVariable("teamId") Long teamId,
                                                                                         @RequestParam(value = "page", defaultValue = "0") int page,
                                                                                         @RequestParam(value = "size", defaultValue = "10") int size,
                                                                                         @RequestParam(value = "keyword", defaultValue = "") String keyword) {
        Page<AddTeamMemberToRoleDTO> result = teamMemberService.getTeamMembers(teamId, page, size, keyword);

        return ResponseEntity.ok(new PageResponse<>(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalPages(),
                result.getTotalElements()
        ));
    }

    @PutMapping("/nickname")
    public ResponseEntity<String> updateTeamNickname(@PathVariable("teamId") Long teamId,
                                                     @AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody ChangeTeamNicknameRequest request) {
        return ResponseEntity.ok(teamMemberService.updateTeamNickname(teamId, userPrincipal.getMember(), request.newNickname()));
    }

    @GetMapping("/nickname/check")
    public ResponseEntity<TeamNicknameCheckResponse> checkTeamNicknameDuplicate(
            @PathVariable("teamId") Long teamId,
            @RequestParam("teamNickname") String teamNickname
    ) {
        boolean isDuplicate = teamMemberService.isTeamNicknameDuplicate(teamId, teamNickname);
        return ResponseEntity.ok(TeamNicknameCheckResponse.of(isDuplicate));
    }

    //팀 멤버의 기본 프로필 받아오기
    @GetMapping("/member/{teamMemberId}")
    public ResponseEntity<MemberProfileResponse> getTeamMemberProfile(
            @PathVariable("teamId") Long teamId, @PathVariable("teamMemberId") Long teamMemberId
    ) {
        return ResponseEntity.ok(teamMemberService.getTeamMemberProfile(teamMemberId));
    }

    //팀 탈퇴
    @PostMapping("/leave")
    public ResponseEntity<Void> leaveTeam(@PathVariable("teamId") Long teamId, @AuthenticationPrincipal UserPrincipal userPrincipal,
                                          @RequestParam(required = false, defaultValue = "false", name = "deleteContents") boolean deleteContents) {
        teamMemberService.leaveTeam(teamId, userPrincipal.getMember(), deleteContents);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/members/{teamMemberId}")
    @PreAuthorize("hasPermission(#teamId, 'Team', T(com.example.board.permission.TeamPermission).MANAGE_MEMBERS)")
    public ResponseEntity<Void> removeMember(
            @PathVariable("teamId") @P("teamId") Long teamId,
            @PathVariable("teamMemberId") Long teamMemberId) {

        teamMemberService.removeMember(teamId, teamMemberId);
        return ResponseEntity.noContent().build();
    }

    //팀에서 작성한 게시글 목록
    @GetMapping("/member/{teamMemberId}/posts")
    public ResponseEntity<PageResponse<PostListResponse>> getMemberPosts(
            @PathVariable("teamId") Long teamId, @PathVariable("teamMemberId") Long teamMemberId,
            @RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "10", name = "size") int size) {
        Page<PostListResponse> postpage = postService.findPostsByTeamAndMember(teamMemberId, page, size);
        return ResponseEntity.ok(PageResponse.from(postpage));
    }

    //팀에서 작성한 댓글 목록
    @GetMapping("/member/{teamMemberId}/comments")
    public ResponseEntity<PageResponse<MemberCommentResponse>> getMemberComments(
            @PathVariable("teamId") Long teamId,
            @PathVariable("teamMemberId") Long teamMemberId,
            @RequestParam(defaultValue = "0", name = "page") int page,
            @RequestParam(defaultValue = "10", name = "size") int size) {
        Page<MemberCommentResponse> commentPage = commentService.findCommentsByTeamAndMember(teamMemberId, page, size);
        return ResponseEntity.ok(PageResponse.from(commentPage));
    }
}
