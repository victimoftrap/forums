package net.thumbtack.forums.service;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.UserRole;
import net.thumbtack.forums.model.UserSession;
import net.thumbtack.forums.dao.UserDao;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.dto.UserDtoResponse;
import net.thumbtack.forums.dto.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.LoginUserDtoRequest;
import net.thumbtack.forums.dto.UpdatePasswordDtoRequest;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import net.thumbtack.forums.validator.PasswordLengthValidator;
import net.thumbtack.forums.validator.UsernameLengthValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private UserDao userDao;
    private SessionDao sessionDao;
    private UsernameLengthValidator usernameLengthValidator;
    private PasswordLengthValidator passwordLengthValidator;
    private UserService userService;

    @BeforeEach
    void initMocks() {
        userDao = mock(UserDao.class);
        sessionDao = mock(SessionDao.class);
        usernameLengthValidator = mock(UsernameLengthValidator.class);
        passwordLengthValidator = mock(PasswordLengthValidator.class);

        userService = new UserService(userDao, sessionDao, usernameLengthValidator, passwordLengthValidator);
    }

    @Test
    void testRegisterUser() {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "jolybell", "ahoi@jolybell.com", "password123"
        );
        final User createdUser = new User(
                1,
                UserRole.USER,
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                LocalDateTime.now(),
                false
        );
        final UserSession session = new UserSession(createdUser, "token");
        final UserDtoResponse expectedResponse = new UserDtoResponse(
                createdUser.getId(),
                createdUser.getUsername(),
                createdUser.getEmail(),
                session.getToken()
        );

        when(userDao.getByName(anyString(), anyBoolean()))
                .thenReturn(null);
        when(usernameLengthValidator.isValid(anyString()))
                .thenReturn(true);
        when(passwordLengthValidator.isValid(anyString()))
                .thenReturn(true);
        doAnswer(invocationOnMock -> {
            User user = invocationOnMock.getArgument(0);
            user.setId(createdUser.getId());
            return user;
        }).when(userDao)
                .save(any(User.class));
        doNothing()
                .when(sessionDao)
                .upsertSession(any(UserSession.class));

        final UserDtoResponse actualResponse = userService.registerUser(request);
        verify(userDao)
                .getByName(anyString(), anyBoolean());
        verify(usernameLengthValidator)
                .isValid(anyString());
        verify(passwordLengthValidator)
                .isValid(anyString());
        verify(userDao)
                .save(any(User.class));
        verify(sessionDao)
                .upsertSession(any(UserSession.class));

        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getName(), actualResponse.getName());
        assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());
    }

    @Test
    void testRegisterUser_userWithRequestedNameExists_throwsServerException() {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "jolybell",
                "ahoi@jolybell.com",
                "password123"
        );

        when(userDao.getByName(eq(request.getName()), anyBoolean()))
                .thenThrow(new ServerException(ErrorCode.INVALID_REQUEST_DATA));
        when(userDao.getByName(eq(request.getName()), anyBoolean()))
                .thenThrow(new ServerException(ErrorCode.INVALID_REQUEST_DATA));

        assertThrows(ServerException.class, () -> userService.registerUser(request));
        try {
            userService.registerUser(request);
        } catch (ServerException e) {
            assertEquals(ErrorCode.INVALID_REQUEST_DATA, e.getErrorCode());
        }

        verify(userDao, times(2))
                .getByName(eq(request.getName()), anyBoolean());
        verifyZeroInteractions(sessionDao);
    }

    @Test
    void testRegisterUser_usernameTooLarge_throwsServerException() {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "01234567890123456789012345678901234567890123456789.haha",
                "ahoi@jolybell.com",
                "password123"
        );
        when(userDao.getByName(anyString(), anyBoolean()))
                .thenReturn(null);
        when(usernameLengthValidator.isValid(eq(request.getName())))
                .thenReturn(false);

        try {
            userService.registerUser(request);
        } catch (ServerException e) {
            assertEquals(ErrorCode.INVALID_REQUEST_DATA, e.getErrorCode());
        }
    }

    @Test
    void testRegisterUser_passwordTooShort_throwsServerException() {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "jolygolf",
                "ahoi@jolybell.com",
                "a"
        );
        when(userDao.getByName(anyString(), anyBoolean()))
                .thenReturn(null);
        when(usernameLengthValidator.isValid(eq(request.getName())))
                .thenReturn(true);
        when(passwordLengthValidator.isValid(eq(request.getPassword())))
                .thenReturn(false);

        try {
            userService.registerUser(request);
        } catch (ServerException e) {
            assertEquals(ErrorCode.INVALID_REQUEST_DATA, e.getErrorCode());
        }
    }

    @Test
    void testRegisterUser_onGetUserDatabaseError_throwsServerException() {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "jolybell",
                "ahoi@jolybell.com",
                "password123"
        );

        when(userDao.getByName(eq(request.getName()), anyBoolean()))
                .thenThrow(new ServerException(ErrorCode.DATABASE_ERROR));

        try {
            userService.registerUser(request);
        } catch (ServerException e) {
            assertEquals(ErrorCode.DATABASE_ERROR, e.getErrorCode());
        }

        verify(userDao)
                .getByName(anyString(), anyBoolean());
        verify(userDao, never())
                .save(any(User.class));
        verifyZeroInteractions(sessionDao);
    }

    @Test
    void testRegisterUser_onSaveUserDatabaseError_throwsServerException() {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "jolybell",
                "ahoi@jolybell.com",
                "password123"
        );

        when(userDao.getByName(anyString(), anyBoolean()))
                .thenReturn(null);
        when(usernameLengthValidator.isValid(anyString()))
                .thenReturn(true);
        when(passwordLengthValidator.isValid(anyString()))
                .thenReturn(true);
        when(userDao.save(any(User.class)))
                .thenThrow(new ServerException(ErrorCode.DATABASE_ERROR));

        try {
            userService.registerUser(request);
        } catch (ServerException e) {
            assertEquals(ErrorCode.DATABASE_ERROR, e.getErrorCode());
        }

        verify(userDao)
                .getByName(anyString(), anyBoolean());
        verify(usernameLengthValidator)
                .isValid(anyString());
        verify(passwordLengthValidator)
                .isValid(anyString());
        verify(userDao)
                .save(any(User.class));
        verifyZeroInteractions(sessionDao);
    }

    @Test
    void testRegisterUser_onSaveSessionDatabaseError_throwsServerException() {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "jolybell",
                "ahoi@jolybell.com",
                "password123"
        );

        when(userDao.getByName(anyString(), anyBoolean()))
                .thenReturn(null);
        when(usernameLengthValidator.isValid(anyString()))
                .thenReturn(true);
        when(passwordLengthValidator.isValid(anyString()))
                .thenReturn(true);
        when(userDao.save(any(User.class)))
                .thenThrow(new ServerException(ErrorCode.DATABASE_ERROR));
        doThrow(new ServerException(ErrorCode.DATABASE_ERROR))
                .when(sessionDao)
                .upsertSession(any(UserSession.class));
        try {
            userService.registerUser(request);
        } catch (ServerException e) {
            assertEquals(ErrorCode.DATABASE_ERROR, e.getErrorCode());
        }

        verify(userDao)
                .getByName(anyString(), anyBoolean());
        verify(usernameLengthValidator)
                .isValid(anyString());
        verify(passwordLengthValidator)
                .isValid(anyString());
        verify(userDao)
                .save(any(User.class));
    }

    @Test
    void testLoginUser() {
        final LoginUserDtoRequest request = new LoginUserDtoRequest("while", "white_stripes");
        final User user = new User(request.getName(), "white@thirdman.com", request.getPassword());

        when(userDao.getByName(anyString()))
                .thenReturn(user);
        doNothing()
                .when(sessionDao)
                .upsertSession(any(UserSession.class));

        final UserDtoResponse response = userService.login(request);

        verify(userDao)
                .getByName(anyString());
        verify(sessionDao)
                .upsertSession(any(UserSession.class));

        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getName());
        assertEquals(user.getEmail(), response.getEmail());
    }

    @Test
    void testLoginUser_loginWithCaseInsensitiveName_successfullyLogin() {
        final LoginUserDtoRequest request = new LoginUserDtoRequest(
                "kAElThaS", "animation_scientist"
        );
        final User user = new User("kaelthas", "kulthas@gmail.com", request.getPassword());

        when(userDao.getByName(anyString()))
                .thenReturn(user);
        doNothing()
                .when(sessionDao)
                .upsertSession(any(UserSession.class));

        final UserDtoResponse response = userService.login(request);

        verify(userDao)
                .getByName(anyString());
        verify(sessionDao)
                .upsertSession(any(UserSession.class));

        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getName());
        assertEquals(user.getEmail(), response.getEmail());
    }

    @Test
    void testLoginUser_userNotFound_throwsServerException() {
        final LoginUserDtoRequest request = new LoginUserDtoRequest("while", "white_stripes");
        when(userDao.getByName(anyString()))
                .thenReturn(null);
        when(userDao.getByName(anyString()))
                .thenReturn(null);

        assertThrows(ServerException.class,
                () -> userService.login(request)
        );
        try {
            userService.login(request);
        } catch (ServerException e) {
            assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());
        }

        verify(userDao, times(2))
                .getByName(anyString());
        verifyZeroInteractions(sessionDao);
    }

    @Test
    void testLoginUser_requestedPasswordNotMatches_throwsServerException() {
        final LoginUserDtoRequest request = new LoginUserDtoRequest("while", "correct_password");
        final User user = new User(request.getName(), "white@thirdman.com", "INCORRECT_password");

        when(userDao.getByName(anyString()))
                .thenReturn(user);

        try {
            userService.login(request);
        } catch (ServerException e) {
            assertEquals(ErrorCode.INVALID_REQUEST_DATA, e.getErrorCode());
        }

        verify(userDao).getByName(anyString());
        verifyZeroInteractions(sessionDao);
    }

    @Test
    void testLogoutUser() {
        final String sessionToken = "token";
        final User user = new User("alvvays", "aloha@alvvays.ca", "password123");

        when(sessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        doNothing()
                .when(sessionDao)
                .deleteSession(anyString());

        userService.logout(sessionToken);
        verify(sessionDao)
                .getUserByToken(anyString());
        verify(sessionDao)
                .deleteSession(anyString());
    }

    @Test
    void testLogoutUser_userNotFoundByToken_throwsServerException() {
        final String sessionToken = "token";
        when(sessionDao.getUserByToken(anyString())).thenReturn(null);

        try {
            userService.logout(sessionToken);
        } catch (ServerException e) {
            assertEquals(ErrorCode.WRONG_SESSION_TOKEN, e.getErrorCode());
        }
        verify(sessionDao)
                .getUserByToken(anyString());
        verify(sessionDao, never())
                .deleteSession(anyString());
    }

    @Test
    void testLogoutUser_onGetSessionDatabaseError_throwsServerException() {
        final String token = "token";
        when(sessionDao.getUserByToken(anyString()))
                .thenThrow(new ServerException(ErrorCode.DATABASE_ERROR));

        try {
            userService.logout(token);
        } catch (ServerException e) {
            assertEquals(ErrorCode.DATABASE_ERROR, e.getErrorCode());
        }
        verify(sessionDao)
                .getUserByToken(anyString());
        verify(sessionDao, never())
                .deleteSession(anyString());
    }

    @Test
    void testLogoutUser_onDeleteSessionDatabaseError_throwsServerException() {
        final String token = "token";
        final User user = new User(
                "alvvays", "aloha@alvvays.ca", "password123");

        when(sessionDao.getUserByToken(anyString())).thenReturn(user);
        doThrow(new ServerException(ErrorCode.DATABASE_ERROR))
                .when(sessionDao)
                .deleteSession(anyString());

        try {
            userService.logout(token);
        } catch (ServerException e) {
            assertEquals(ErrorCode.DATABASE_ERROR, e.getErrorCode());
        }
        verify(sessionDao).getUserByToken(anyString());
        verify(sessionDao).deleteSession(anyString());
    }

    @Test
    void testUpdatePassword() {
        final String sessionToken = UUID.randomUUID().toString();
        final User user = new User("alvvays", "aloha@alvvays.ca", "password123");
        final UpdatePasswordDtoRequest request = new UpdatePasswordDtoRequest(
                user.getUsername(), user.getPassword(), "new-password-123"
        );

        when(sessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        when(passwordLengthValidator.isValid(eq(request.getPassword())))
                .thenReturn(true);
        doAnswer(invocationOnMock -> {
            User upd = invocationOnMock.getArgument(0);
            upd.setPassword(request.getPassword());
            return upd;
        })
                .when(userDao)
                .update(any(User.class));

        final UserDtoResponse response = userService.updatePassword(sessionToken, request);
        verify(sessionDao)
                .getUserByToken(anyString());
        verify(passwordLengthValidator)
                .isValid(anyString());
        verify(userDao)
                .update(any(User.class));
        assertEquals(
                new UserDtoResponse(user.getId(), user.getUsername(), user.getEmail(), sessionToken),
                response
        );
    }

    @Test
    void testUpdatePassword_userNotFoundByToken_throwsServerException() {
        final String sessionToken = "token";
        when(sessionDao.getUserByToken(anyString())).thenReturn(null);

        try {
            userService.logout(sessionToken);
        } catch (ServerException e) {
            assertEquals(ErrorCode.WRONG_SESSION_TOKEN, e.getErrorCode());
        }
        verify(sessionDao)
                .getUserByToken(anyString());
        verifyZeroInteractions(userDao);
    }

    @Test
    void testUpdatePassword_passwordTooShort_throwsServerException() {
        final String sessionToken = UUID.randomUUID().toString();
        final User user = new User("alvvays", "aloha@alvvays.ca", "password123");
        final UpdatePasswordDtoRequest request = new UpdatePasswordDtoRequest(
                user.getUsername(), user.getPassword(), "short!"
        );

        when(sessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        when(passwordLengthValidator.isValid(eq(request.getPassword())))
                .thenReturn(false);

        try {
            userService.updatePassword(sessionToken, request);
        } catch (ServerException e) {
            assertEquals(ErrorCode.INVALID_REQUEST_DATA, e.getErrorCode());
        }
        verify(sessionDao)
                .getUserByToken(anyString());
        verify(passwordLengthValidator)
                .isValid(anyString());
    }
}