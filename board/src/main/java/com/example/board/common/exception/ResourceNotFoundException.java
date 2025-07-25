package com.example.board.common.exception;

public class ResourceNotFoundException extends RuntimeException {
  protected ResourceNotFoundException(String message) {
    super(message);
  }
}
