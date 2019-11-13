package net.thumbtack.forums.model;

import java.util.Objects;

public class UserSession {
    private User user;
    private String token;

    public UserSession(User user, String token) {
        this.user = user;
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
        return Objects.equals(getUser(), that.getUser()) &&
                Objects.equals(getToken(), that.getToken());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUser(), getToken());
    }
}
