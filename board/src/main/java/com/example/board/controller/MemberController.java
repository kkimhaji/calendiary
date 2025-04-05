package com.example.board.controller;

import com.example.board.auth.UserPrincipal;
import com.example.board.dto.member.MemberInfoResponse;
import com.example.board.dto.member.MemberInfoSummaryResponse;
import com.example.board.dto.member.PasswordChangeRequest;
import com.example.board.dto.team.TeamInfoResponse;
import com.example.board.dto.team.TeamListDTO;
import com.example.board.service.MemberService;
import com.example.board.service.TeamMemberService;
import lombok.RequiredArgsConstructor;
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

    @PutMapping("/change-password")
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
}
