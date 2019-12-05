package net.thumbtack.forums.service;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.UserRole;
import net.thumbtack.forums.model.UserSession;
import net.thumbtack.forums.dto.user.*;
import net.thumbtack.forums.dto.EmptyDtoResponse;
import net.thumbtack.forums.converter.UserConverter;
import net.thumbtack.forums.dao.UserDao;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.RequestFieldName;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;

@Service("userService")
public class UserService {
    private final UserDao userDao;
    private final SessionDao sessionDao;
    private final ServerConfigurationProperties properties;

    @Autowired
    public UserService(final UserDao userDao,
                       final SessionDao sessionDao,
                       final ServerConfigurationProperties properties) {
        this.userDao = userDao;
        this.sessionDao = sessionDao;
        this.properties = properties;
    }

    private User getUserBySessionOrThrowException(final String token) {
        return Optional
                .ofNullable(sessionDao.getUserByToken(token))
                .orElseThrow(() -> new ServerException(ErrorCode.WRONG_SESSION_TOKEN));
    }

    private User getUserByIdOrThrowException(final int id) {
        return Optional
                .ofNullable(userDao.getById(id))
                .orElseThrow(() -> new ServerException(ErrorCode.USER_NOT_FOUND));
    }

    public UserDtoResponse registerUser(final RegisterUserDtoRequest request) {
    	// REVU см. комментарий в скайп-чате
        if (userDao.getByName(request.getName(), true) != null) {
            throw new ServerException(ErrorCode.INVALID_REQUEST_DATA, RequestFieldName.USERNAME);
        }

        final User user = new User(request.getName(), request.getEmail(), request.getPassword());
        final UserSession session = new UserSession(user, UUID.randomUUID().toString());
        userDao.save(user, session);
        return UserConverter.userToUserResponse(user, session.getToken());
    }

    public EmptyDtoResponse deleteUser(final String sessionToken) {
        final User user = getUserBySessionOrThrowException(sessionToken);

        // TODO made readonly user forums
        user.setDeleted(true);
        userDao.deactivateById(user.getId());
        return new EmptyDtoResponse();
    }

    public UserDtoResponse login(final LoginUserDtoRequest request) {
        final User user = userDao.getByName(request.getName());
        if (user == null) {
            throw new ServerException(ErrorCode.USER_NOT_FOUND, RequestFieldName.USERNAME);
        }
        if (!user.getPassword().equals(request.getPassword())) {
            throw new ServerException(ErrorCode.INVALID_REQUEST_DATA, RequestFieldName.PASSWORD);
        }

        final UserSession session = new UserSession(user, UUID.randomUUID().toString());
        sessionDao.upsertSession(session);
        return UserConverter.userToUserResponse(user, session.getToken());
    }

    public EmptyDtoResponse logout(final String sessionToken) {
        getUserBySessionOrThrowException(sessionToken);
        sessionDao.deleteSession(sessionToken);
        return new EmptyDtoResponse();
    }

    public UserDtoResponse updatePassword(final String sessionToken, final UpdatePasswordDtoRequest request) {
        final User user = getUserBySessionOrThrowException(sessionToken);
        if (!user.getPassword().equals(request.getOldPassword())) {
            throw new ServerException(ErrorCode.INVALID_REQUEST_DATA, RequestFieldName.OLD_PASSWORD);
        }

        user.setPassword(request.getPassword());
        userDao.update(user);
        return UserConverter.userToUserResponse(user, sessionToken);
    }

    public EmptyDtoResponse madeSuperuser(final String sessionToken, final int newSuperUserId) {
        final User user = getUserBySessionOrThrowException(sessionToken);
        if (user.getRole() != UserRole.SUPERUSER) {
            throw new ServerException(ErrorCode.FORBIDDEN_OPERATION);
        }

        final User newSuperUser = getUserByIdOrThrowException(newSuperUserId);
        newSuperUser.setRole(UserRole.SUPERUSER);
        newSuperUser.setBannedUntil(null);
        userDao.update(newSuperUser);
        return new EmptyDtoResponse();
    }

    public UserDetailsListDtoResponse getUsers(final String sessionToken) {
        final User requestingUser = getUserBySessionOrThrowException(sessionToken);
        final List<UserSession> usersWithSessions = userDao.getAllWithSessions();
        return UserConverter.usersWithSessionsToResponse(usersWithSessions, requestingUser.getRole());
    }

    public EmptyDtoResponse banUser(final String sessionToken, final int restrictedUserId) {
        final User user = getUserBySessionOrThrowException(sessionToken);
        if (user.getRole() != UserRole.SUPERUSER) {
            throw new ServerException(ErrorCode.FORBIDDEN_OPERATION);
        }

        final User restrictedUser = getUserByIdOrThrowException(restrictedUserId);
        if (restrictedUser.getRole() == UserRole.SUPERUSER) {
            throw new ServerException(ErrorCode.FORBIDDEN_OPERATION);
        }

        final LocalDateTime banTime;
        final int banCount;
        if (restrictedUser.getBanCount() < properties.getMaxBanCount() - 1) {
            banTime = LocalDateTime.of(
                    LocalDate.now().plus(properties.getBanTime(), ChronoUnit.DAYS),
                    LocalTime.of(0, 0)
            )
                    .truncatedTo(ChronoUnit.SECONDS);
            banCount = restrictedUser.getBanCount() + 1;
        } else {
            banTime = LocalDateTime
                    .of(9999, Month.JANUARY, 1, 0, 0, 0)
                    .truncatedTo(ChronoUnit.SECONDS);
            banCount = properties.getMaxBanCount();
        }
        restrictedUser.setBannedUntil(banTime);
        restrictedUser.setBanCount(banCount);
        userDao.update(restrictedUser);
        return new EmptyDtoResponse();
    }
}
