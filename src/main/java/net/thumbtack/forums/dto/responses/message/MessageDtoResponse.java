package net.thumbtack.forums.dto.responses.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class MessageDtoResponse {
    private int id;
    private String state;

    @JsonCreator
    public MessageDtoResponse(@JsonProperty("id") int id,
                              @JsonProperty("state") String state) {
        this.id = id;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public String getState() {
        return state;
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
}
