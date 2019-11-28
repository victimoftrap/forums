package net.thumbtack.forums.service;

import net.thumbtack.forums.exception.RequestFieldName;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.UserRole;
import net.thumbtack.forums.model.UserSession;
import net.thumbtack.forums.dto.*;
import net.thumbtack.forums.dao.UserDao;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import at.favre.lib.crypto.bcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.Month;

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

    public UserDtoResponse registerUser(final RegisterUserDtoRequest request) {
        if (userDao.getByName(request.getName(), true) != null) {
            throw new ServerException(ErrorCode.INVALID_REQUEST_DATA, RequestFieldName.USERNAME);
        }

        final User user = new User(request.getName(), request.getEmail(), request.getPassword());
        userDao.save(user);

        final UserSession session = new UserSession(user, UUID.randomUUID().toString());
        sessionDao.upsertSession(session);
        return new UserDtoResponse(user.getId(), user.getUsername(), user.getEmail(), session.getToken());
    }

    public EmptyDtoResponse deleteUser(final String sessionToken) {
        final User user = sessionDao.getUserByToken(sessionToken);
        if (user == null) {
            throw new ServerException(ErrorCode.WRONG_SESSION_TOKEN);
        }

        // TODO made readonly user forums
        user.setDeleted(true);
        sessionDao.deleteSession(sessionToken);
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
        return new UserDtoResponse(user.getId(), user.getUsername(), user.getEmail(), session.getToken());
    }

    public EmptyDtoResponse logout(final String sessionToken) {
        final User user = sessionDao.getUserByToken(sessionToken);
        if (user == null) {
            throw new ServerException(ErrorCode.WRONG_SESSION_TOKEN);
        }
        sessionDao.deleteSession(sessionToken);
        return new EmptyDtoResponse();
    }

    public UserDtoResponse updatePassword(final String sessionToken, final UpdatePasswordDtoRequest request) {
        final User user = sessionDao.getUserByToken(sessionToken);
        if (user == null) {
            throw new ServerException(ErrorCode.WRONG_SESSION_TOKEN);
        }
        if (!user.getPassword().equals(request.getOldPassword())) {
            throw new ServerException(ErrorCode.INVALID_REQUEST_DATA, RequestFieldName.OLD_PASSWORD);
        }

        user.setPassword(request.getPassword());
        userDao.update(user);
        return new UserDtoResponse(user.getId(), user.getUsername(), user.getEmail(), sessionToken);
    }

    public EmptyDtoResponse madeSuperuser(final String sessionToken, final int userId) {
        final User user = sessionDao.getUserByToken(sessionToken);
        if (user == null) {
            throw new ServerException(ErrorCode.WRONG_SESSION_TOKEN);
        }
        if (user.getRole() != UserRole.SUPERUSER) {
            throw new ServerException(ErrorCode.FORBIDDEN_OPERATION);
        }

        final User dependentUser = userDao.getById(userId);
        if (dependentUser == null) {
            throw new ServerException(ErrorCode.USER_NOT_FOUND);
        }

        dependentUser.setRole(UserRole.SUPERUSER);
        dependentUser.setBannedUntil(null);
        userDao.update(dependentUser);
        return new EmptyDtoResponse();
    }

    public EmptyDtoResponse banUser(final String sessionToken, final int restrictedUserId) {
        final User user = sessionDao.getUserByToken(sessionToken);
        if (user == null) {
            throw new ServerException(ErrorCode.WRONG_SESSION_TOKEN);
        }
        if (user.getRole() != UserRole.SUPERUSER) {
            throw new ServerException(ErrorCode.FORBIDDEN_OPERATION);
        }

        final User restrictedUser = userDao.getById(restrictedUserId);
        if (restrictedUser == null) {
            throw new ServerException(ErrorCode.USER_NOT_FOUND);
        }
        if (restrictedUser.getRole() == UserRole.SUPERUSER) {
            throw new ServerException(ErrorCode.FORBIDDEN_OPERATION);
        }

        final LocalDateTime banTime;
        final int banCount;
        if (restrictedUser.getBanCount() < properties.getMaxBanCount()) {
            banTime = LocalDateTime.now().plus(properties.getBanTime(), ChronoUnit.DAYS);
            banCount = user.getBanCount() + 1;
        } else {
            banTime = LocalDateTime.of(9999, Month.JANUARY, 1, 0, 0, 0);
            banCount = user.getBanCount();
        }
        user.setBannedUntil(banTime);
        user.setBanCount(banCount);
        userDao.update(user);
        return new EmptyDtoResponse();
    }
}
