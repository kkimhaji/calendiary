package com.example.board.common.exception;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenExpired(RefreshTokenExpiredException ex) {
        ErrorResponse errorResponse = new ErrorResponse("REFRESH_TOKEN_EXPIRED", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        List<String> errors = ex.getConstraintViolations()
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toList());

        ErrorResponse error = new ErrorResponse(
                "VALIDATION_ERROR",
                String.join(", ", errors)
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        ErrorResponse error = new ErrorResponse(
                "TYPE_MISMATCH",
                ex.getName() + " 매개변수의 값이 올바르지 않습니다."
        );
        return ResponseEntity.badRequest().body(error);
    }
    @ExceptionHandler(PostValidationException.class)
    public ResponseEntity<ErrorResponse> handlePostValidation(PostValidationException ex) {
        ErrorResponse errorResponse = new ErrorResponse("INVALID_POST_DATA", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    //일반적인 IllegalArgumentException도 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        ErrorResponse errorResponse = new ErrorResponse("INVALID_REQUEST", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFoundException(EntityNotFoundException e) {
        ErrorResponse errorResponse = new ErrorResponse(
                "RESOURCE_NOT_FOUND",
                e.getMessage()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    @ExceptionHandler(TeamNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTeamNotFound(TeamNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse("TEAM_NOT_FOUND", ex.getMessage()); // "team not found"
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(CategoryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCategoryNotFound(CategoryNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse("CATEGORY_NOT_FOUND", ex.getMessage()); // "category not found"
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(PostNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePostNotFound(PostNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse("POST_NOT_FOUND", ex.getMessage()); // "post not found"
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRoleNotFound(RoleNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse("ROLE_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCommentNotFound(CommentNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse("COMMENT_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMemberNotFound(MemberNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse("MEMBER_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(TeamMemberNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleTeamMemberNotFound(TeamMemberNotFoundException ex) {
        ErrorResponse errorResponse = new ErrorResponse("TEAM_MEMBER_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(DiaryNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDiaryNotFound(DiaryNotFoundException ex){
        ErrorResponse errorResponse = new ErrorResponse("DIARY_NOT_FOUND", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(
            NullPointerException ex,
            HttpServletRequest request) {

        // @AuthenticationPrincipal로 인한 null 체크
        if (isAuthenticationPrincipalNull(ex, request)) {
            ErrorResponse errorResponse = new ErrorResponse(
                    "AUTHENTICATION_REQUIRED",
                    "인증이 필요합니다"
            );
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        // 다른 NullPointerException은 500으로 처리
        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_ERROR",
                "내부 서버 오류가 발생했습니다"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private boolean isAuthenticationPrincipalNull(NullPointerException ex, HttpServletRequest request) {
        // 스택 트레이스에서 @AuthenticationPrincipal 관련 null 체크
        String stackTrace = Arrays.toString(ex.getStackTrace());
        return stackTrace.contains("UserPrincipal") ||
                stackTrace.contains("getMember") ||
                request.getRequestURI().contains("/api/teams/create");
    }

}
