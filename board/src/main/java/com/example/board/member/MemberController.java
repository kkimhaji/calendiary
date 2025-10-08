package com.example.board.member;

import com.example.board.auth.UserPrincipal;
import com.example.board.comment.CommentService;
import com.example.board.comment.dto.MemberCommentResponse;
import com.example.board.common.dto.PageResponse;
import com.example.board.member.dto.MemberInfoResponse;
import com.example.board.member.dto.MemberInfoSummaryResponse;
import com.example.board.member.dto.PasswordChangeRequest;
import com.example.board.member.dto.VerifyPasswordRequest;
import com.example.board.post.PostService;
import com.example.board.post.dto.PostListResponse;
import com.example.board.team.dto.TeamInfoResponse;
import com.example.board.team.dto.TeamListDTO;
import com.example.board.teamMember.dto.MemberProfileResponse;
import com.example.board.teamMember.TeamMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;
    private final TeamMemberService teamMemberService;
    private final PostService postService;
    private final CommentService commentService;

    @GetMapping("/getprincipal")
    public ResponseEntity<?> getPrincipal(@AuthenticationPrincipal UserPrincipal member){
        return ResponseEntity.ok(member);
    }

    @GetMapping("/get_teams")
    public ResponseEntity<List<TeamListDTO>> getTeams(@AuthenticationPrincipal UserPrincipal member){
        return ResponseEntity.ok(teamMemberService.getTeams(member.getMember()));
    }

    @GetMapping("/get-info")
    public ResponseEntity<MemberInfoSummaryResponse> getMemberInfo(@AuthenticationPrincipal UserPrincipal userPrincipal){
        return ResponseEntity.ok(memberService.getMemberInfoSummary(userPrincipal));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody PasswordChangeRequest request){
        memberService.updatePassword(userPrincipal.getMember(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/account-info")
    public ResponseEntity<MemberInfoResponse> getAccountInfo(@AuthenticationPrincipal UserPrincipal userPrincipal){
        return ResponseEntity.ok(memberService.getInfoForAccountPage(userPrincipal));
    }

    @GetMapping("/team-list")
    public ResponseEntity<List<TeamInfoResponse>> getTeamInfo(@AuthenticationPrincipal UserPrincipal userPrincipal){
        return ResponseEntity.ok(teamMemberService.getTeamInfoWithTeamNickname(userPrincipal.getMember().getMemberId()));
    }

    @PostMapping("/verify-password")
    public ResponseEntity<Boolean> verifyPassword(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestBody VerifyPasswordRequest request){
        return ResponseEntity.ok(memberService.checkPassword(userPrincipal.getMember(), request.currentPassword()));
    }

    @PutMapping("/update-name")
    public ResponseEntity<String> updateNickname(@AuthenticationPrincipal UserPrincipal userPrincipal, @RequestParam("newNickname") String newNickname){
        return ResponseEntity.ok(memberService.updateMemberName(userPrincipal.getMember(), newNickname));
    }

    @PostMapping("/{teamId}/leave")
    public ResponseEntity<Void> leaveTeam(@PathVariable("teamId") Long teamId, @AuthenticationPrincipal UserPrincipal userPrincipal,
                                          @RequestParam(required = false, defaultValue = "false", name = "deleteContents") boolean deleteContents){
        teamMemberService.leaveTeam(teamId, userPrincipal.getMember(), deleteContents);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/teams/{teamId}/{teamMemberId}/posts")
    public ResponseEntity<PageResponse<PostListResponse>> getMemberPosts(
            @PathVariable("teamId") Long teamId, @PathVariable("teamMemberId") Long teamMemberId,
            @RequestParam(defaultValue = "0", name="page") int page,
            @RequestParam(defaultValue = "10", name = "size") int size){
            Page<PostListResponse> postpage = postService.findPostsByTeamAndMember(teamMemberId, page, size);
        return ResponseEntity.ok(PageResponse.from(postpage));
    }

    @GetMapping("/teams/{teamId}/{teamMemberId}/comments")
    public ResponseEntity<PageResponse<MemberCommentResponse>> getMemberComments(
            @PathVariable("teamId") Long teamId,
            @PathVariable("teamMemberId") Long teamMemberId,
            @RequestParam(defaultValue = "0", name="page") int page,
            @RequestParam(defaultValue = "10", name="size") int size) {
            Page<MemberCommentResponse> commentPage = commentService.findCommentsByTeamAndMember(teamMemberId, page, size);
        return ResponseEntity.ok(PageResponse.from(commentPage));
    }

    @GetMapping("/teams/{teamId}/member/{teamMemberId}")
    public ResponseEntity<MemberProfileResponse> getTeamMemberProfile(
            @PathVariable("teamId") Long teamId, @PathVariable("teamMemberId") Long teamMemberId
    ){
        return ResponseEntity.ok(teamMemberService.getTeamMemberProfile(teamMemberId));
    }
}
