package net.thumbtack.forums.service;

import net.thumbtack.forums.dto.requests.user.LoginUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.UpdatePasswordDtoRequest;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.UserRole;
import net.thumbtack.forums.model.UserSession;
import net.thumbtack.forums.dto.responses.user.*;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
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

    // REVU да просто getUserBySession
    // тут почти все методы бросают исключение, если что-то не так
    // не будем же в имени каждого метода прописывать, что он бросает исключение
    private User getUserBySession(final String token) throws ServerException {
        final User user = sessionDao.getUserByToken(token);
        if (user == null) {
            throw new ServerException(ErrorCode.WRONG_SESSION_TOKEN);
        }
        return user;
    }

    private User getUserById(final int id) throws ServerException {
        final User user = userDao.getById(id, false);
        if (user == null) {
            throw new ServerException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    public UserDtoResponse registerUser(final RegisterUserDtoRequest request) throws ServerException {
        // REVU см. комментарий в скайп-чате
        if (userDao.getByName(request.getName(), true) != null) {
            throw new ServerException(ErrorCode.INVALID_REQUEST_DATA, RequestFieldName.USERNAME);
        }

        final User user = new User(request.getName(), request.getEmail(), request.getPassword());
        final UserSession session = new UserSession(user, UUID.randomUUID().toString());
        userDao.save(user, session);
        return UserConverter.userToUserResponse(user, session.getToken());
    }

    public EmptyDtoResponse deleteUser(final String sessionToken) throws ServerException {
        final User user = getUserBySession(sessionToken);

        // TODO made readonly user forums
        user.setDeleted(true);
        userDao.deactivateById(user.getId());
        return new EmptyDtoResponse();
    }

    public UserDtoResponse login(final LoginUserDtoRequest request) throws ServerException {
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

    public EmptyDtoResponse logout(final String sessionToken) throws ServerException {
        getUserBySession(sessionToken);
        sessionDao.deleteSession(sessionToken);
        return new EmptyDtoResponse();
    }

    public UserDtoResponse updatePassword(
            final String sessionToken, final UpdatePasswordDtoRequest request
    ) throws ServerException {
        final User user = getUserBySession(sessionToken);
        if (!user.getPassword().equals(request.getOldPassword())) {
            throw new ServerException(ErrorCode.INVALID_REQUEST_DATA, RequestFieldName.OLD_PASSWORD);
        }

        user.setPassword(request.getPassword());
        userDao.update(user);
        return UserConverter.userToUserResponse(user, sessionToken);
    }

    public EmptyDtoResponse madeSuperuser(
            final String sessionToken, final int newSuperUserId
    ) throws ServerException {
        final User user = getUserBySession(sessionToken);
        if (user.getRole() != UserRole.SUPERUSER) {
            throw new ServerException(ErrorCode.FORBIDDEN_OPERATION);
        }

        final User newSuperUser = getUserById(newSuperUserId);
        newSuperUser.setRole(UserRole.SUPERUSER);
        newSuperUser.setBannedUntil(null);
        userDao.update(newSuperUser);
        return new EmptyDtoResponse();
    }

    public UserDetailsListDtoResponse getUsers(
            final String sessionToken
    ) throws ServerException {
        final User requestingUser = getUserBySession(sessionToken);
        final List<UserSession> usersWithSessions = userDao.getAllWithSessions();
        return UserConverter.usersWithSessionsToResponse(usersWithSessions, requestingUser.getRole());
    }

    public EmptyDtoResponse banUser(
            final String sessionToken, final int restrictedUserId
    ) throws ServerException {
        final User user = getUserBySession(sessionToken);
        if (user.getRole() != UserRole.SUPERUSER) {
            throw new ServerException(ErrorCode.FORBIDDEN_OPERATION);
        }

        final User restrictedUser = getUserById(restrictedUserId);
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
