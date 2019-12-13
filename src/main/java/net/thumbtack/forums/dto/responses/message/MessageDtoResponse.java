package net.thumbtack.forums.dto.responses.message;

import net.thumbtack.forums.model.enums.MessageState;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class MessageDtoResponse {
    private int id;
    private MessageState state;

    @JsonCreator
    public MessageDtoResponse(@JsonProperty("id") int id,
                              @JsonProperty("state") MessageState state) {
        this.id = id;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageDtoResponse)) return false;
        MessageDtoResponse that = (MessageDtoResponse) o;
        return getId() == that.getId() &&
                getState() == that.getState();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getState());
    }

    public MessageState getState() {
        return state;
    }
}
