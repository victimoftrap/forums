package net.thumbtack.forums.dto.forum;

import net.thumbtack.forums.model.enums.ForumType;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ForumInfoDtoResponse {
    private int id;
    private String name;
    private ForumType type;
    private String creatorName;
    private boolean readonly;
    private int messageCount;
    private int commentCount;

    public ForumInfoDtoResponse() {
    }

    @JsonCreator
    public ForumInfoDtoResponse(@JsonProperty("id") int id,
                                @JsonProperty("name") String name,
                                @JsonProperty("type") ForumType type,
                                @JsonProperty("creator") String creatorName,
                                @JsonProperty("readonly") boolean readonly,
                                @JsonProperty("messageCount") int messageCount,
                                @JsonProperty("commentCount") int commentCount) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.creatorName = creatorName;
        this.readonly = readonly;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ForumType getType() {
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
}
