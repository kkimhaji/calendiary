package com.example.board.domain.team.enums;

public enum UserTeamStatus {
    TEAM_MEMBER,        // 팀 멤버
    VALID_INVITE,       // 유효한 초대 코드가 있음
    NO_ACCESS           // 팀원이 아니며 유효한 초대도 없음
}