package net.thumbtack.forums.view;

import java.util.Objects;

public class UserRatingView {
    private int userId;
    private String username;
    private double rating;
    private int rated;

    public UserRatingView(int userId, String username, double rating, int rated) {
        this.userId = userId;
        this.username = username;
        this.rating = rating;
        this.rated = rated;
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

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getRated() {
        return rated;
    }

    public void setRated(int rated) {
        this.rated = rated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRatingView)) return false;
        UserRatingView that = (UserRatingView) o;
        return userId == that.userId &&
                Double.compare(that.rating, rating) == 0 &&
                rated == that.rated &&
                Objects.equals(username, that.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, rating, rated);
    }

    @Override
    public String toString() {
        return "UserRatingView{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", rating=" + rating +
                ", rated=" + rated +
                '}';
    }
}
