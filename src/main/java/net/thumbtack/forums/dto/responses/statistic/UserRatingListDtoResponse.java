package net.thumbtack.forums.dto.responses.statistic;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Objects;

public class UserRatingListDtoResponse {
    private List<UserRatingDtoResponse> users;

    @JsonCreator
    public UserRatingListDtoResponse(@JsonProperty("usersRatings") List<UserRatingDtoResponse> users) {
        this.users = users;
    }

    public List<UserRatingDtoResponse> getUsers() {
        return users;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRatingListDtoResponse)) return false;
        UserRatingListDtoResponse that = (UserRatingListDtoResponse) o;
        return Objects.equals(users, that.users);
    }

    @Override
    public int hashCode() {
        return Objects.hash(users);
    }
}
