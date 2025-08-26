package com.example.board.image;

public enum ImageDomain {
    POST("/post-images/", "/post-temp-images/"),
    DIARY("/diary-images/", "/diary-temp-images/");

    private final String permPrefix;   // CKEditor 본문에 삽입될 최종 URL
    private final String tempPrefix;   // 에디터 임시 URL

    ImageDomain(String permPrefix, String tempPrefix) {
        this.permPrefix = permPrefix;
        this.tempPrefix = tempPrefix;
    }
    public String permPrefix() { return permPrefix; }
    public String tempPrefix() { return tempPrefix; }
}
