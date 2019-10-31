package net.thumbtack.forums.model;

import java.util.Objects;

public class UserSession {
    private Integer userId;
    private String token;

    public UserSession(Integer userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserSession)) return false;
        UserSession that = (UserSession) o;
        return Objects.equals(getUserId(), that.getUserId()) &&
                Objects.equals(getToken(), that.getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getToken());
    }
}
