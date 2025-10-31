package com.y11i.springcommddd.common.api;

import com.y11i.springcommddd.common.exception.BaseException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;


@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 우리(도메인/애플리케이션/인프라) 예외
    @ExceptionHandler(BaseException.class)
    public ProblemDetail handleBase(BaseException ex, HttpServletRequest req) {
        var code = ex.getErrorCode();
        return ProblemFactory.of(code, humanize(code), ex.getMessage(), req.getRequestURI());
    }

    // Spring 6: ErrorResponseException (ProblemDetail 내장)
    @ExceptionHandler(ErrorResponseException.class)
    public ProblemDetail handleErrorResponse(ErrorResponseException ex, HttpServletRequest req) {
        ProblemDetail body = ex.getBody();
        if (body.getInstance() == null) body.setInstance(URI.create(req.getRequestURI()));
        if (body.getType() == null) body.setType(URI.create("https://api.springcomm.app/errors/generic.bad_request"));
        if (body.getTitle() == null) body.setTitle(ex.getStatusCode().toString());
        if (body.getDetail() == null) body.setDetail("Request error");
        body.setProperty("timestamp", Instant.now().toString());
        return body;
    }

    // 호환: ResponseStatusException (reason 보존)
    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail handleResponseStatus(ResponseStatusException ex, HttpServletRequest req) {

        // ex.getStatusCode()는 HttpStatusCode 인터페이스지만 보통 HttpStatus로 변환 가능
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        // 1) 어떤 ErrorCode를 쓸지 결정
        ErrorCode code;
        String title;

        if (status.is4xxClientError()) {
            // 여기서 401/403은 권한 문제 → PERMISSION_DENIED
            // 그 외(400 등)는 BAD_REQUEST로 본다.
            if (status == HttpStatus.UNAUTHORIZED || status == HttpStatus.FORBIDDEN) {
                code = ErrorCode.PERMISSION_DENIED;
                title = "Access Denied";
                if (status == HttpStatus.UNAUTHORIZED) {
                    title = "Unauthorized";
                }
            } else {
                code = ErrorCode.BAD_REQUEST;
                title = "Bad Request";
            }
        } else if (status.is5xxServerError()) {
            code = ErrorCode.INTERNAL_ERROR;
            title = "Internal Server Error";
        } else {
            // 2xx, 3xx 같은 비정상 케이스로 ResponseStatusException이 오면? 방어적으로 처리
            code = ErrorCode.BAD_REQUEST;
            title = status.toString();
        }

        // 2) Detail (사람이 알아볼 메시지)
        String detail = (ex.getReason() != null)
                ? ex.getReason()
                : "Request error";

        // 3) 표준 ProblemDetail 생성
        ProblemDetail pd = ProblemFactory.of(
                code,
                title,
                detail,
                req.getRequestURI()
        );

        // 4) ProblemDetail.status 도 실제 status로 맞춰주기
        pd.setStatus(status.value());

        // 5) timestamp 등은 ProblemFactory.of(...) 이미 넣고 있으면 패스.
        //    혹시 of()에서 넣지 않으면 여기서도:
        //    pd.setProperty("timestamp", Instant.now().toString());

        return pd;
    }


    // Bean Validation (DTO @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgInvalid(MethodArgumentNotValidException ex, HttpServletRequest req) {
        var fieldError = ex.getBindingResult().getFieldError();
        String msg = fieldError != null ? fieldError.getDefaultMessage() : "Validation failed";
        var pd = ProblemFactory.of(ErrorCode.BAD_REQUEST, "Validation Failed", msg, req.getRequestURI());
        pd.setProperty("errors", ex.getBindingResult().getAllErrors());
        return pd;
    }

    // Bean Validation (서비스/도메인 @Validated)
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        var pd = ProblemFactory.of(ErrorCode.BAD_REQUEST, "Constraint Violation", ex.getMessage(), req.getRequestURI());
        pd.setProperty("violations", ex.getConstraintViolations());
        return pd;
    }

    // 바인딩/변환 예외
    @ExceptionHandler(BindException.class)
    public ProblemDetail handleBind(BindException ex, HttpServletRequest req) {
        var pd = ProblemFactory.of(ErrorCode.BAD_REQUEST, "Binding Failed", "Invalid request payload", req.getRequestURI());
        pd.setProperty("errors", ex.getAllErrors());
        return pd;
    }

    // 보안
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        return ProblemFactory.of(ErrorCode.PERMISSION_DENIED, "Access Denied", ex.getMessage(), req.getRequestURI());
    }

    // 마지막 안전망
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleOther(Exception ex, HttpServletRequest req) {
        return ProblemFactory.of(ErrorCode.INTERNAL_ERROR, "Internal Server Error", "An unexpected error occurred.", req.getRequestURI());
    }

    private String humanize(ErrorCode code) {
        return switch (code) {
            case POST_NOT_FOUND -> "Post Not Found";
            case POST_TITLE_INVALID -> "Invalid Post Title";
            case POST_CONTENT_INVALID -> "Invalid Post Content";
            case POST_ARCHIVED_MODIFICATION_FORBIDDEN -> "Archived Post Modification Forbidden";
            case POST_STATUS_TRANSITION_FORBIDDEN -> "Post Status Transition Forbidden";
            case COMMENT_BODY_INVALID -> "Invalid Comment Body";
            case COMMENT_DELETED_MODIFICATION_FORBIDDEN -> "Deleted Comment Modification Forbidden";
            case COMMENT_DEPTH_INVALID -> "Invalid Comment Depth";
            case COMMUNITY_NAME_INVALID -> "Invalid Community Name";
            case COMMUNITY_NAME_KEY_INVALID -> "Invalid Community Name Key";
            case COMMUNITY_ARCHIVED_MODIFICATION_FORBIDDEN -> "Archived Community Modification Forbidden";
            case COMMUNITY_STATUS_TRANSITION_FORBIDDEN -> "Community Status Transition Forbidden";
            case IMAGE_URL_INVALID -> "Invalid Image URL";
            case MODERATOR_DUPLICATE -> "Moderator Already Granted";
            case MODERATOR_NOT_FOUND -> "Moderator Not Found";
            case MODERATOR_INVALID -> "Invalid Moderator Assignment";
            case MEMBER_DELETED_MODIFICATION_FORBIDDEN -> "Deleted Member Modification Forbidden";
            case MEMBER_STATUS_TRANSITION_FORBIDDEN -> "Member Status Transition Forbidden";
            case EMAIL_INVALID -> "Invalid Email";
            case DISPLAY_NAME_INVALID -> "Invalid Display Name";
            case PASSWORD_HASH_INVALID -> "Invalid Password Hash";
            case VOTE_VALUE_INVALID -> "Invalid Vote Value";
            case VOTE_DUPLICATE -> "Duplicate Vote";
            case VOTE_NOT_FOUND -> "Vote Not Found";
            case PERMISSION_DENIED -> "Permission Denied";
            case BAD_REQUEST -> "Bad Request";
            default -> "Internal Error";
        };
    }
}
