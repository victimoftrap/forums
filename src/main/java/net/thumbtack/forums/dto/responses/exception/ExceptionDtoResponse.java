package net.thumbtack.forums.dto.responses.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExceptionDtoResponse {
    private String errorCode;
    private String field;
    private String message;

    @JsonCreator
    public ExceptionDtoResponse(
            @JsonProperty("errorCode") String errorCode,
            @JsonProperty("field") String field,
            @JsonProperty("message") String message) {
        this.errorCode = errorCode;
        this.field = field;
        this.message = message;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExceptionDtoResponse)) return false;
        ExceptionDtoResponse that = (ExceptionDtoResponse) o;
        return Objects.equals(errorCode, that.errorCode) &&
                Objects.equals(field, that.field) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorCode, field, message);
    }
}
