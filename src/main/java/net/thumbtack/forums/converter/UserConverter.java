package net.thumbtack.forums.converter;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.dto.user.UserDtoResponse;

public class UserConverter {
    public static UserDtoResponse userToUserResponse(final User user, final String sessionToken) {
        return new UserDtoResponse(user.getId(), user.getUsername(), user.getEmail(), sessionToken);
    }
}
