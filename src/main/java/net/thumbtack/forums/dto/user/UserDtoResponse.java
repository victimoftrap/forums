package net.thumbtack.forums.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDtoResponse {
    private int id;
    private String name;
    private String email;
    @JsonIgnore
    private String sessionToken;

    public UserDtoResponse() {
    }

    public UserDtoResponse(int id, String name, String email, String sessionToken) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.sessionToken = sessionToken;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDtoResponse)) return false;
        UserDtoResponse that = (UserDtoResponse) o;
        return getId() == that.getId() &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getEmail(), that.getEmail()) &&
                Objects.equals(getSessionToken(), that.getSessionToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getEmail(), getSessionToken());
    }
}
