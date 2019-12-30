package net.thumbtack.forums.dto.responses.forum;

import net.thumbtack.forums.model.enums.ForumType;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForumDtoResponse {
    private int id;
    private String name;
    private ForumType type;

    ForumDtoResponse() {
    }

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

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(ForumType type) {
        this.type = type;
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
