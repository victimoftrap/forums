package net.thumbtack.forums.dto.exception;

import net.thumbtack.forums.exception.ErrorCode;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionDtoResponse {
    private ErrorCode errorCode;
    private String field;
    private String message;

    public ExceptionDtoResponse(ErrorCode errorCode, String field, String message) {
        this.errorCode = errorCode;
        this.field = field;
        this.message = message;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }
}
