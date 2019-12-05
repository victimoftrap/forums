package net.thumbtack.forums.dto.message;

import net.thumbtack.forums.model.enums.MessageState;

import java.util.Objects;

public class EditMessageOrCommentDtoResponse {
    private MessageState state;

    public EditMessageOrCommentDtoResponse(MessageState state) {
        this.state = state;
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
