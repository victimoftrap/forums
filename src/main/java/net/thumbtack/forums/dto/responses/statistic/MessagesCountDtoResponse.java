package net.thumbtack.forums.dto.responses.statistic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class MessagesCountDtoResponse {
    private int messagesCount;

    @JsonCreator
    public MessagesCountDtoResponse(@JsonProperty("messagesCount") int messagesCount) {
        this.messagesCount = messagesCount;
    }

    public int getMessagesCount() {
        return messagesCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessagesCountDtoResponse)) return false;
        MessagesCountDtoResponse that = (MessagesCountDtoResponse) o;
        return messagesCount == that.messagesCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(messagesCount);
    }
}
