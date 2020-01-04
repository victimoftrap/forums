package net.thumbtack.forums.exception;

public enum ErrorCode {
    DATABASE_ERROR("Error in database occurred"),
    INVALID_REQUEST_DATA("Request data was invalid"),
    FORBIDDEN_OPERATION("Operation allowed only for superuser"),
    WRONG_SESSION_TOKEN("Wrong session token"),
    FORUM_NOT_FOUND("Forum not found"),
    FORUM_READ_ONLY("Forum closed, it's only read-only now"),
    FORUM_NAME_ALREADY_USED("Forum name already used"),
    MESSAGE_NOT_FOUND("Message not found"),
    MESSAGE_NOT_PUBLISHED("Message not already published"),
    MESSAGE_HAS_COMMENTS("Unable to delete message because it has comments"),
    MESSAGE_REJECTED("Message rejected for publication"),
    MESSAGE_ALREADY_PUBLISHED("Message already published"),
    MESSAGE_ALREADY_BRANCH("Message already branch in forum"),
    USER_NAME_ALREADY_USED("Username already used in server"),
    USER_NOT_FOUND("User not found"),
    USER_BANNED("Not available operation while user banned"),
    USER_PERMANENTLY_BANNED("User got a permanent ban");

    private String message;

    ErrorCode() {
    }

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
