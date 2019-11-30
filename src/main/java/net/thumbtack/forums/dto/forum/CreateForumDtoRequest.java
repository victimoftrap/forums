package net.thumbtack.forums.dto.forum;

import net.thumbtack.forums.validator.ForumNamePattern;
import net.thumbtack.forums.model.enums.ForumType;

import javax.validation.constraints.NotNull;

public class CreateForumDtoRequest {
    @ForumNamePattern
    private String name;
    @NotNull
    private ForumType type;

    public CreateForumDtoRequest(String name, ForumType type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public ForumType getType() {
        return type;
    }
}
