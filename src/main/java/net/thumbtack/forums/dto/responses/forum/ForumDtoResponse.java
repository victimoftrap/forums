package net.thumbtack.forums.dto.responses.forum;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForumDtoResponse {
    private int id;
    private String name;
    private String type;

    ForumDtoResponse() {
    }

    public ForumDtoResponse(int id, String name, String type) {
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

    public String getType() {
        return type;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(String type) {
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
