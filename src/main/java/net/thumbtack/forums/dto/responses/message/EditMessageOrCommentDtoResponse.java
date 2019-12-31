package net.thumbtack.forums.dto.responses.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class EditMessageOrCommentDtoResponse {
    private String state;

    @JsonCreator
    public EditMessageOrCommentDtoResponse(@JsonProperty("state") String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EditMessageOrCommentDtoResponse)) return false;
        EditMessageOrCommentDtoResponse that = (EditMessageOrCommentDtoResponse) o;
        return state == that.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(state);
    }
}
