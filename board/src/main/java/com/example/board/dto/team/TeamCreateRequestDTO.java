package com.example.board.dto.team;

import com.example.board.domain.member.Member;
import lombok.Data;

@Data
public class TeamCreateRequestDTO {

    private Member member;
    private String teamName;
    private String description;

}
