package net.thumbtack.forums.dto.responses.forum;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class ForumInfoDtoResponse {
    private int id;
    private String name;
    private String type;
    private String creatorName;
    private boolean readonly;
    private int messageCount;
    private int commentCount;

    public ForumInfoDtoResponse() {
    }

    @JsonCreator
    public ForumInfoDtoResponse(@JsonProperty("id") int id,
                                @JsonProperty("name") String name,
                                @JsonProperty("type") String type,
                                @JsonProperty("creator") String creatorName,
                                @JsonProperty("readonly") boolean readonly,
                                @JsonProperty("messageCount") int messageCount,
                                @JsonProperty("commentCount") int commentCount) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.creatorName = creatorName;
        this.readonly = readonly;
        this.messageCount = messageCount;
        this.commentCount = commentCount;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public boolean isReadonly() {
        return readonly;
    }

    public int getMessageCount() {
        return messageCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForumInfoDtoResponse)) return false;
        ForumInfoDtoResponse response = (ForumInfoDtoResponse) o;
        return id == response.id &&
                readonly == response.readonly &&
                messageCount == response.messageCount &&
                commentCount == response.commentCount &&
                Objects.equals(name, response.name) &&
                Objects.equals(type, response.type) &&
                Objects.equals(creatorName, response.creatorName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type, creatorName, readonly, messageCount, commentCount);
    }
}
