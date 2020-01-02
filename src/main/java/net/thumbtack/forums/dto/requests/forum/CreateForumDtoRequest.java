package net.thumbtack.forums.dto.requests.forum;

import net.thumbtack.forums.validator.forum.ForumNamePattern;
import net.thumbtack.forums.validator.forum.AvailableForumType;

public class CreateForumDtoRequest {
    @ForumNamePattern
    private String name;

    @AvailableForumType
    private String type;

    public CreateForumDtoRequest(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
