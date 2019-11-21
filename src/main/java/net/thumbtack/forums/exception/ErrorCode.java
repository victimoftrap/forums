package net.thumbtack.forums.exception;

public enum ErrorCode {
    DATABASE_ERROR("Error in database occurred");

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
