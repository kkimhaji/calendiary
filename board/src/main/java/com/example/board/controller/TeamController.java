package com.example.board.controller;

import com.example.board.domain.member.Member;
import com.example.board.domain.team.Team;
import com.example.board.dto.team.TeamCreateRequestDTO;
import com.example.board.service.MemberService;
import com.example.board.service.TeamService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/team")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    private final MemberService memberService;

    @PostMapping("/create")
    public ResponseEntity<Team> createTeam(HttpServletRequest request, @RequestBody TeamCreateRequestDTO dto){
        var loginMember = memberService.getMember(request).get();
        System.out.println("in controller: "+dto.getTeamName()+" / " + dto.getDescription());
        return ResponseEntity.ok(teamService.createTeam(loginMember, dto));
    }

}
