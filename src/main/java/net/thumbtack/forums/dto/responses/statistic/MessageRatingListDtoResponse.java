package net.thumbtack.forums.dto.responses.statistic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class MessageRatingListDtoResponse {
    private List<MessageRatingDtoResponse> messages;

    @JsonCreator
    public MessageRatingListDtoResponse(
            @JsonProperty("messagesRatings") List<MessageRatingDtoResponse> messages) {
        this.messages = messages;
    }

    public List<MessageRatingDtoResponse> getMessages() {
        return messages;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageRatingListDtoResponse)) return false;
        MessageRatingListDtoResponse that = (MessageRatingListDtoResponse) o;
        return Objects.equals(messages, that.messages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messages);
    }
}
