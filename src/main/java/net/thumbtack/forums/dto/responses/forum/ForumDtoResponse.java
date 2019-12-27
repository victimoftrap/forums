package net.thumbtack.forums.dto.responses.forum;

import com.fasterxml.jackson.annotation.JsonInclude;
import net.thumbtack.forums.model.enums.ForumType;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForumDtoResponse {
    private int id;
    private String name;
    private ForumType type;

    public ForumDtoResponse(int id, String name, ForumType type) {
        this.id = id;
        this.name = name;
        this.type = type;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForumDtoResponse)) return false;
        ForumDtoResponse response = (ForumDtoResponse) o;
        return getId() == response.getId() &&
                Objects.equals(getName(), response.getName()) &&
                getType() == response.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getType());
    }
}
