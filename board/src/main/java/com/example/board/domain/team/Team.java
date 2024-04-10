package com.example.board.domain.team;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
//@NoArgsConstructor
public class Team {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long teamId;
    private String teamName;
    private Long teamLeader;
    @ElementCollection(fetch = FetchType.EAGER)
    @Builder.Default
    private List<Long> memberList = new ArrayList<>();

    
}
