package net.thumbtack.forums.exception;

public enum ErrorCode {
    DATABASE_ERROR("Error in database occurred", "database"),
    INVALID_REQUEST_DATA("Request data was invalid", "request"),
    NO_USER_SESSION("User has no session on server", "session"),
    FORUM_NOT_FOUND("Forum not found", "forumId"),
    FORUM_READ_ONLY("Forum closed, it's only read-only now", "forum"),
    FORUM_NAME_ALREADY_USED("Forum name already used", "name"),
    MESSAGE_NOT_FOUND("Message not found", "messageId"),
    MESSAGE_NOT_PUBLISHED("Message not published", "message"),
    MESSAGE_HAS_COMMENTS("Unable to delete message because it has comments", "message"),
    MESSAGE_REJECTED("Message rejected for publication", "message"),
    MESSAGE_ALREADY_PUBLISHED("Message already published", "message"),
    MESSAGE_ALREADY_BRANCH("Message already branch in forum", "message"),
    MESSAGE_CREATOR_RATES_HIS_MESSAGE("Author haven't permission to rate his messages", "user"),
    UNABLE_OPERATION_FOR_COMMENT("Operation available only for messages", "messageId"),
    USER_NAME_ALREADY_USED("Username already used in server", "username"),
    INVALID_PASSWORD("Invalid password", "password"),
    USER_NOT_FOUND("User not found", "user"),
    FORBIDDEN_OPERATION("Operation not allowed for this user", "role"),
    USER_BANNED("Not available operation while user banned", "user"),
    USER_PERMANENTLY_BANNED("User got a permanent ban", "user");

    private String message;
    private String errorCause;

    ErrorCode() {
    }

    ErrorCode(String message) {
        this.message = message;
        this.errorCause = "";
    }

    ErrorCode(String message, String errorCause) {
        this.message = message;
        this.errorCause = errorCause;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCauseField() {
        return errorCause;
    }
}
