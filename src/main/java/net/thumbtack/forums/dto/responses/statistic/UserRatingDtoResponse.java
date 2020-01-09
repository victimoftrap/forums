package net.thumbtack.forums.dto.responses.statistic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class UserRatingDtoResponse {
    private int userId;
    private String name;
    private double rating;
    private int rated;

    @JsonCreator
    public UserRatingDtoResponse(@JsonProperty("id") int userId,
                                 @JsonProperty("name") String name,
                                 @JsonProperty("rating") double rating,
                                 @JsonProperty("rated") int rated) {
        this.userId = userId;
        this.name = name;
        this.rating = rating;
        this.rated = rated;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public double getRating() {
        return rating;
    }

    public int getRated() {
        return rated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRatingDtoResponse)) return false;
        UserRatingDtoResponse that = (UserRatingDtoResponse) o;
        return userId == that.userId &&
                Double.compare(that.rating, rating) == 0 &&
                rated == that.rated &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, name, rating, rated);
    }

    @Override
    public String toString() {
        return "UserRatingDtoResponse{" +
                "userId=" + userId +
                ", name='" + name + '\'' +
                ", rating=" + rating +
                ", rated=" + rated +
                '}';
    }
}
