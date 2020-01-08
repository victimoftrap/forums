package net.thumbtack.forums.dto.responses.statistic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class MessageRatingDtoResponse {
    private int messageId;
    private String messageType;
    private double rating;
    private int rated;

    @JsonCreator
    public MessageRatingDtoResponse(@JsonProperty("id") int messageId,
                                    @JsonProperty("type") String messageType,
                                    @JsonProperty("rating") double rating,
                                    @JsonProperty("rated") int rated) {
        this.messageId = messageId;
        this.messageType = messageType;
        this.rating = rating;
        this.rated = rated;
    }

    public int getMessageId() {
        return messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public double getRating() {
        return rating;
    }

    public int getRated() {
        return rated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageRatingDtoResponse)) return false;
        MessageRatingDtoResponse that = (MessageRatingDtoResponse) o;
        return messageId == that.messageId &&
                Double.compare(that.rating, rating) == 0 &&
                rated == that.rated &&
                Objects.equals(messageType, that.messageType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, messageType, rating, rated);
    }

    @Override
    public String toString() {
        return "MessageRatingDtoResponse{" +
                "messageId=" + messageId +
                ", messageType='" + messageType + '\'' +
                ", rating=" + rating +
                ", rated=" + rated +
                '}';
    }
}
