package com.example.board.teamInvite.dto;

public record TeamJoinRequest(
        String code,
        String teamNickname
) {
    public void validate() {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("초대 코드가 필요합니다");
        }

        if (teamNickname == null || teamNickname.trim().isEmpty()) {
            throw new IllegalArgumentException("팀 닉네임이 필요합니다");
        }

        if (teamNickname.length() < 2 || teamNickname.length() > 20) {
            throw new IllegalArgumentException("팀 닉네임은 2-20글자여야 합니다");
        }

        if (!teamNickname.matches("^[가-힣a-zA-Z0-9\\s]*$")) {
            throw new IllegalArgumentException("팀 닉네임에는 한글, 영문, 숫자만 사용할 수 있습니다");
        }
    }
}