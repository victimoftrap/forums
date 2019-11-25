package net.thumbtack.forums.service;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.UserRole;
import net.thumbtack.forums.model.UserSession;
import net.thumbtack.forums.dto.*;
import net.thumbtack.forums.dao.UserDao;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import at.favre.lib.crypto.bcrypt.BCrypt;
import net.thumbtack.forums.validator.PasswordLengthValidator;
import net.thumbtack.forums.validator.UsernameLengthValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("userService")
public class UserService {
    private final UserDao userDao;
    private final SessionDao sessionDao;
    private final UsernameLengthValidator usernameLengthValidator;
    private final PasswordLengthValidator passwordLengthValidator;

    @Autowired
    public UserService(final UserDao userDao,
                       final SessionDao sessionDao,
                       final UsernameLengthValidator usernameLengthValidator,
                       final PasswordLengthValidator passwordLengthValidator) {
        this.userDao = userDao;
        this.sessionDao = sessionDao;
        this.usernameLengthValidator = usernameLengthValidator;
        this.passwordLengthValidator = passwordLengthValidator;
    }

    public UserDtoResponse registerUser(final RegisterUserDtoRequest request) {
        if (userDao.getByName(request.getName(), true) != null) {
            throw new ServerException(ErrorCode.USER_WITH_THIS_NAME_EXISTS);
        }
        if (!usernameLengthValidator.isValid(request.getName()) ||
                !passwordLengthValidator.isValid(request.getPassword())) {
            throw new ServerException(ErrorCode.INVALID_REQUEST_DATA);
        }

        final User user = new User(request.getName(), request.getEmail(), request.getPassword());
        userDao.save(user);

        final UserSession session = new UserSession(user, UUID.randomUUID().toString());
        sessionDao.upsertSession(session);
        return new UserDtoResponse(user.getId(), user.getUsername(), user.getEmail(), session.getToken());
    }

    public UserDtoResponse login(final LoginUserDtoRequest request) {
        final User user = userDao.getByName(request.getName());
        if (user == null) {
            throw new ServerException(ErrorCode.USER_NOT_FOUND_BY_NAME);
        }
        if (!user.getPassword().equals(request.getPassword())) {
            throw new ServerException(ErrorCode.USER_PASSWORD_NOT_MATCHES);
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
        if (!passwordLengthValidator.isValid(request.getPassword())) {
            throw new ServerException(ErrorCode.INVALID_REQUEST_DATA);
        }
        if (!user.getPassword().equals(request.getOldPassword())) {
            throw new ServerException(ErrorCode.USER_PASSWORD_NOT_MATCHES);
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
            throw new ServerException(ErrorCode.USER_NOT_FOUND_BY_ID);
        }

        dependentUser.setRole(UserRole.SUPERUSER);
        userDao.update(dependentUser);
        return new EmptyDtoResponse();
    }
}
