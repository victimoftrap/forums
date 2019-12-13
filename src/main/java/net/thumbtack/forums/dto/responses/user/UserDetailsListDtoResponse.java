package net.thumbtack.forums.dto.responses.user;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserDetailsListDtoResponse {
    private List<UserDetailsDtoResponse> users;

    public UserDetailsListDtoResponse() {
        users = new ArrayList<>();
    }

    public UserDetailsListDtoResponse(List<UserDetailsDtoResponse> users) {
        this.users = users;
    }

    public List<UserDetailsDtoResponse> getUsers() {
        return users;
    }

    public void setUsers(List<UserDetailsDtoResponse> users) {
        this.users = users;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserDetailsListDtoResponse)) return false;
        UserDetailsListDtoResponse response = (UserDetailsListDtoResponse) o;
        return Objects.equals(getUsers(), response.getUsers());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsers());
    }
}
