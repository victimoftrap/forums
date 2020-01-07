package net.thumbtack.forums.view;

import java.util.Objects;

public class UserRatingView {
    private int userId;
    private String username;
    private double userRating;

    public UserRatingView(int userId, String username, double userRating) {
        this.userId = userId;
        this.username = username;
        this.userRating = userRating;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getUserRating() {
        return userRating;
    }

    public void setUserRating(double userRating) {
        this.userRating = userRating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRatingView)) return false;
        UserRatingView that = (UserRatingView) o;
        return userId == that.userId &&
                Double.compare(that.userRating, userRating) == 0 &&
                Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, userRating);
    }

    @Override
    public String toString() {
        return "UserRatingView{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", userRating=" + userRating +
                '}';
    }
}
