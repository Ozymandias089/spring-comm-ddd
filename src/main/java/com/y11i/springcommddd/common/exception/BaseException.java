package com.y11i.springcommddd.common.exception;

import com.y11i.springcommddd.common.api.ErrorCode;

public abstract class BaseException extends RuntimeException {
    private final ErrorCode errorCode;
    protected BaseException(ErrorCode errorCode, String message) { super(message); this.errorCode = errorCode; }
    protected BaseException(ErrorCode errorCode, String message, Throwable cause) { super(message, cause); this.errorCode = errorCode; }
    public ErrorCode getErrorCode() { return errorCode; }
}
