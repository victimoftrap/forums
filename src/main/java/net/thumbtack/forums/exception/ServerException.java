package net.thumbtack.forums.exception;

public class ServerException extends Exception {
    private ErrorCode errorCode;
    private RequestFieldName errorField;

    public ServerException(ErrorCode errorCode) {
        this(errorCode, RequestFieldName.NONE);
    }

    public ServerException(ErrorCode errorCode, RequestFieldName errorField) {
        this.errorCode = errorCode;
        this.errorField = errorField;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public RequestFieldName getErrorField() {
        return errorField;
    }

    @Override
    public String getMessage() {
        return errorCode.getMessage();
    }
}
