package com.example.board.exception;

public class RefreshTokenExpiredException extends RuntimeException{
    public RefreshTokenExpiredException(String message){
        super (message);
    }
}
