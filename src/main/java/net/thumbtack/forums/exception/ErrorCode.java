package net.thumbtack.forums.exception;

public enum ErrorCode {
    DATABASE_ERROR("Error in database occurred"),
    FORBIDDEN_OPERATION("Operation allowed only for superuser"),
    INVALID_REQUEST_DATA("Request data was invalid"),
    USER_NOT_FOUND("User not found"),
    USER_BANNED("Not available operation while user banned"),
    WRONG_SESSION_TOKEN("Wrong session token")
    ;

    private String message;

    ErrorCode() {
    }

    private ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
