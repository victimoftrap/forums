package net.thumbtack.forums.dto.responses.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class ListMessageInfoDtoResponse {
    private List<MessageInfoDtoResponse> messages;

    @JsonCreator
    public ListMessageInfoDtoResponse(@JsonProperty("messages") List<MessageInfoDtoResponse> messages) {
        this.messages = messages;
    }

    public List<MessageInfoDtoResponse> getMessages() {
        return messages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListMessageInfoDtoResponse)) return false;
        ListMessageInfoDtoResponse that = (ListMessageInfoDtoResponse) o;
        return Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messages);
    }

    @Override
    public String toString() {
        return "ListMessageInfoDtoResponse{" +
                "messages=" + messages +
                '}';
    }
}
