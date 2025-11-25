package com.example.board.common.exception;

import com.example.board.auth.UserPrincipal;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
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
    public ResponseEntity<ErrorResponse> handleDiaryNotFound(DiaryNotFoundException ex) {
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

    /**
     * AccessDeniedException 처리 - 권한 부족 시 403 반환
     * 로그아웃되지 않도록 403 상태로 명확히 처리
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {

        log.warn("접근 거부 - URI: {}, 사용자: {}, 메시지: {}",
                request.getRequestURI(),
                getCurrentUsername(),
                ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "ACCESS_DENIED",
                "해당 리소스에 접근할 권한이 없습니다."
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    /**
     * AuthenticationException 처리 - 인증 실패 시 401 반환
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {

        log.warn("인증 실패 - URI: {}, 메시지: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "AUTHENTICATION_FAILED",
                "인증에 실패했습니다. 다시 로그인해주세요."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * InsufficientAuthenticationException 처리 - 인증 정보 부족 시 401 반환
     */
    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientAuthenticationException(
            InsufficientAuthenticationException ex, HttpServletRequest request) {

        log.warn("인증 정보 부족 - URI: {}, 메시지: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "INSUFFICIENT_AUTHENTICATION",
                "완전한 인증이 필요합니다."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * UsernameNotFoundException 처리 - 사용자를 찾을 수 없을 때 401 반환
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
            UsernameNotFoundException ex, HttpServletRequest request) {

        log.warn("사용자를 찾을 수 없음 - URI: {}, 메시지: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "USER_NOT_FOUND",
                "사용자 정보를 찾을 수 없습니다."
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * 권한 검증 중 발생하는 일반적인 SecurityException 처리
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ErrorResponse> handleSecurityException(
            SecurityException ex, HttpServletRequest request) {

        log.error("보안 예외 발생 - URI: {}, 메시지: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                "SECURITY_ERROR",
                "보안 검증 중 오류가 발생했습니다."
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }

    private boolean isAuthenticationPrincipalNull(NullPointerException ex, HttpServletRequest request) {
        // 스택 트레이스에서 @AuthenticationPrincipal 관련 null 체크
        String stackTrace = Arrays.toString(ex.getStackTrace());
        return stackTrace.contains("UserPrincipal") ||
                stackTrace.contains("getMember") ||
                request.getRequestURI().contains("/api/teams/create");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {

        log.error("예상치 못한 예외 발생 - URI: {}, 예외: {}",
                request.getRequestURI(), ex.getClass().getSimpleName(), ex);

        ErrorResponse errorResponse = new ErrorResponse(
                "INTERNAL_SERVER_ERROR",
                "서버 내부 오류가 발생했습니다."
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    private String getCurrentUsername() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof UserPrincipal) {
                return ((UserPrincipal) auth.getPrincipal()).getUsername();
            }
            return "anonymous";
        } catch (Exception e) {
            return "unknown";
        }
    }

    @ExceptionHandler(VerificationCodeExpiredException.class)
    public ResponseEntity<ErrorResponse> handleVerificationCodeExpired(
            VerificationCodeExpiredException ex) {

        log.warn("인증 코드 만료: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "VERIFICATION_CODE_EXPIRED",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.GONE) // 410 Gone
                .body(errorResponse);
    }

    @ExceptionHandler(AccountNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotVerified(
            AccountNotVerifiedException ex) {

        log.warn("계정 미인증 로그인 시도 - email: {}", ex.getEmail());

        ErrorResponse errorResponse = new ErrorResponse(
                "ACCOUNT_NOT_VERIFIED",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN) // 403 Forbidden
                .body(errorResponse);
    }

    @ExceptionHandler(InvalidVerificationCodeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidVerificationCode(
            InvalidVerificationCodeException ex) {

        log.warn("잘못된 인증 코드 입력: {}", ex.getMessage());

        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_VERIFICATION_CODE",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 400 Bad Request
                .body(errorResponse);
    }

    /**
     * Validation 예외 처리 (@Valid, @Validated)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(
            MethodArgumentNotValidException ex,
            WebRequest request) {

        // 모든 필드 에러를 수집
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.warn("Validation 실패 - URI: {}, 에러: {}",
                request.getDescription(false), errorMessage);

        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_FAILED",
                errorMessage
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorResponse);
    }
}