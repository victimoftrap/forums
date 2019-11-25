package net.thumbtack.forums.exception;

public enum ErrorCode {
    DATABASE_ERROR("Error in database occurred"),
    FORBIDDEN_OPERATION("Operation allowed only for superuser"),
    INVALID_REQUEST_DATA("Request data was invalid"),
    USER_WITH_THIS_NAME_EXISTS("User with requested name are already exists"),
    USER_NOT_FOUND_BY_ID("User not found by requested ID"),
    USER_NOT_FOUND_BY_NAME("User not found by requested name"),
    USER_PASSWORD_NOT_MATCHES("Requested user password not matches"),
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
