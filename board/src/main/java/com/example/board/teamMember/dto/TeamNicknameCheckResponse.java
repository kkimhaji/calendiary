package com.example.board.teamMember.dto;

public record TeamNicknameCheckResponse(
        boolean isDuplicate,
        String message
) {
    public static TeamNicknameCheckResponse of(boolean isDuplicate){
        return new TeamNicknameCheckResponse(isDuplicate, null);
    }

    public static TeamNicknameCheckResponse error(String message){
        return new TeamNicknameCheckResponse(false, message);
    }
}
