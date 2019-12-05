package net.thumbtack.forums.converter;

import net.thumbtack.forums.dto.user.UserDetailsDtoResponse;
import net.thumbtack.forums.dto.user.UserDetailsListDtoResponse;
import net.thumbtack.forums.dto.user.UserStatus;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.dto.user.UserDtoResponse;
import net.thumbtack.forums.model.UserSession;
import net.thumbtack.forums.model.enums.UserRole;

import java.util.ArrayList;
import java.util.List;

public class UserConverter {
    public static UserDtoResponse userToUserResponse(final User user, final String sessionToken) {
        return new UserDtoResponse(user.getId(), user.getUsername(), user.getEmail(), sessionToken);
    }

    public static UserDetailsDtoResponse userToUserDetailsResponse(final UserSession userWithSession,
                                                                   final UserRole requestingUserRole) {
        final User user = userWithSession.getUser();
        final boolean isOnline = userWithSession.getToken() != null;
        final boolean isSuper = UserRole.SUPERUSER == user.getRole();
        final UserStatus status = user.getBannedUntil() == null ? UserStatus.FULL : UserStatus.LIMITED;

        return new UserDetailsDtoResponse(
                user.getId(),
                user.getUsername(),
                requestingUserRole == UserRole.SUPERUSER ? user.getEmail() : null,
                user.getRegisteredAt(),
                isOnline,
                user.isDeleted(),
                requestingUserRole == UserRole.SUPERUSER ? isSuper : null,
                status,
                user.getBannedUntil(),
                user.getBanCount()
        );
    }

    public static UserDetailsListDtoResponse usersWithSessionsToResponse(final List<UserSession> users,
                                                                         final UserRole requestingUserRole) {
        final List<UserDetailsDtoResponse> usersResponse = new ArrayList<>();
        for (final UserSession user : users) {
            usersResponse.add(userToUserDetailsResponse(user, requestingUserRole));
        }
        return new UserDetailsListDtoResponse(usersResponse);
    }
}
