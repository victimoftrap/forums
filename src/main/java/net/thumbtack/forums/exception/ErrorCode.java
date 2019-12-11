package net.thumbtack.forums.exception;

public enum ErrorCode {
    DATABASE_ERROR("Error in database occurred"),
    FORBIDDEN_OPERATION("Operation allowed only for superuser"),
    FORUM_NOT_FOUND("Forum not found"),
    INVALID_REQUEST_DATA("Request data was invalid"),
    MESSAGE_NOT_FOUND("Message not found"),
    MESSAGE_NOT_PUBLISHED("Message not already published"),
    MESSAGE_NOT_DELETED("Unable to delete message because it has comments"),
    MESSAGE_REJECTED("Message rejected for publication"),
    MESSAGE_ALREADY_PUBLISHED("Message rejected for publication"),
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
