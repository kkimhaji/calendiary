package com.example.board.dto.post;

public record ImageResponse(
        String url,
        String fileName,
        String uploaded //업로드 성공 url
)  {
    public ImageResponse(String error){
        this(null, null, error);
    }

    //에러 응답용
    public static ImageResponse error(String message){
        return new ImageResponse(null, null, message);
    }
}
