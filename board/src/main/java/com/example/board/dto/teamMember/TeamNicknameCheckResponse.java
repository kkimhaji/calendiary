package com.example.board.dto.teamMember;

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
