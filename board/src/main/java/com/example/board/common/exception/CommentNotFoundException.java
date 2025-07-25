package com.example.board.common.exception;

public class CommentNotFoundException extends ResourceNotFoundException {
  private static final String DEFAULT_MESSAGE = "comment not found";

  public CommentNotFoundException() {
    super(DEFAULT_MESSAGE);
  }

  public CommentNotFoundException(String message) {
    super(message);
  }

  public static CommentNotFoundException defaultException() {
    return new CommentNotFoundException();
  }

  public static CommentNotFoundException withMessage(String message) {
    return new CommentNotFoundException(message);
  }
}
