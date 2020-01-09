package net.thumbtack.forums.dto.responses.statistic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class CommentsCountDtoResponse {
    private int commentsCount;

    @JsonCreator
    public CommentsCountDtoResponse(@JsonProperty("commentsCount") int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommentsCountDtoResponse)) return false;
        CommentsCountDtoResponse that = (CommentsCountDtoResponse) o;
        return commentsCount == that.commentsCount;
    }

    @Override
    public int hashCode() {
        return Objects.hash(commentsCount);
    }
}
