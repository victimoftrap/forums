package net.thumbtack.forums.service;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.UserRole;
import net.thumbtack.forums.model.UserSession;
import net.thumbtack.forums.dao.UserDao;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.dto.user.UserDtoResponse;
import net.thumbtack.forums.dto.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.user.LoginUserDtoRequest;
import net.thumbtack.forums.dto.user.UpdatePasswordDtoRequest;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.time.Month;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;

class UserServiceTest {
    private UserDao userDao;
    private SessionDao sessionDao;
    private ServerConfigurationProperties properties;
    private UserService userService;

    @BeforeEach
    void initMocks() {
        userDao = mock(UserDao.class);
        sessionDao = mock(SessionDao.class);
        properties = mock(ServerConfigurationProperties.class);
        userService = new UserService(userDao, sessionDao, properties);
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
        verify(userDao)
                .save(any(User.class));
        verify(sessionDao)
                .upsertSession(any(UserSession.class));

        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getName(), actualResponse.getName());
        assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());
    }

    @Test
    void testRegisterUser_userWithRequestedNameExists_shouldThrowException() {
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
    void testRegisterUser_onGetUserDatabaseError_shouldThrowException() {
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
    void testRegisterUser_onSaveUserDatabaseError_shouldThrowException() {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "jolybell",
                "ahoi@jolybell.com",
                "password123"
        );

        when(userDao.getByName(anyString(), anyBoolean()))
                .thenReturn(null);
        when(userDao.save(any(User.class)))
                .thenThrow(new ServerException(ErrorCode.DATABASE_ERROR));

        try {
            userService.registerUser(request);
        } catch (ServerException e) {
            assertEquals(ErrorCode.DATABASE_ERROR, e.getErrorCode());
        }

        verify(userDao)
                .getByName(anyString(), anyBoolean());
        verify(userDao)
                .save(any(User.class));
        verifyZeroInteractions(sessionDao);
    }

    @Test
    void testRegisterUser_onSaveSessionDatabaseError_shouldThrowException() {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "jolybell",
                "ahoi@jolybell.com",
                "password123"
        );

        when(userDao.getByName(anyString(), anyBoolean()))
                .thenReturn(null);
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
        verify(userDao)
                .save(any(User.class));
    }

    @Test
    void testDeleteUser() {
        final String token = "token";
        final User user = new User(456, UserRole.USER,
                "user", "user@forums.ca", "userpass456",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );

        when(sessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        doNothing()
                .when(sessionDao)
                .deleteSession(eq(token));
        doNothing()
                .when(userDao)
                .deactivateById(anyInt());

        userService.deleteUser(token);

        verify(sessionDao)
                .getUserByToken(eq(token));
        verify(sessionDao)
                .deleteSession(eq(token));
        verify(userDao)
                .deactivateById(anyInt());
    }

    @Test
    void testDeleteUser_userNotFoundByToken_shouldThrowException() {
        final String token = "token";
        when(sessionDao.getUserByToken(anyString()))
                .thenReturn(null);
        try {
            userService.deleteUser(token);
        } catch (ServerException e) {
            assertEquals(ErrorCode.WRONG_SESSION_TOKEN, e.getErrorCode());
        }

        verify(sessionDao)
                .getUserByToken(eq(token));
        verify(sessionDao, never())
                .deleteSession(eq(token));
        verify(userDao, never())
                .deactivateById(anyInt());
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
    void testLoginUser_loginWithCaseInsensitiveName_shouldSuccessfullyLogin() {
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
    void testLoginUser_userNotFound_shouldThrowException() {
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
    void testLoginUser_requestedPasswordNotMatches_shouldThrowException() {
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
    void testLogoutUser_userNotFoundByToken_shouldThrowException() {
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
    void testLogoutUser_onGetSessionDatabaseError_shouldThrowException() {
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
    void testLogoutUser_onDeleteSessionDatabaseError_shouldThrowException() {
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
        verify(userDao)
                .update(any(User.class));
        assertEquals(
                new UserDtoResponse(user.getId(), user.getUsername(), user.getEmail(), sessionToken),
                response
        );
    }

    @Test
    void testUpdatePassword_userNotFoundByToken_shouldThrowException() {
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
    void testUpdatePassword_passwordTooShort_shouldThrowException() {
        final String sessionToken = UUID.randomUUID().toString();
        final User user = new User("alvvays", "aloha@alvvays.ca", "password123");
        final UpdatePasswordDtoRequest request = new UpdatePasswordDtoRequest(
                user.getUsername(), user.getPassword(), "short!"
        );

        when(sessionDao.getUserByToken(anyString()))
                .thenReturn(user);

        try {
            userService.updatePassword(sessionToken, request);
        } catch (ServerException e) {
            assertEquals(ErrorCode.INVALID_REQUEST_DATA, e.getErrorCode());
        }
        verify(sessionDao)
                .getUserByToken(anyString());
    }

    @Test
    void testMadeSuperuser() {
        final String sessionToken = UUID.randomUUID().toString();
        final User superuser = new User(123, UserRole.SUPERUSER,
                "superuser", "super@forums.ca", "superpass123",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );
        final User user = new User(456, UserRole.USER,
                "user", "user@forums.ca", "userpass456",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );

        when(sessionDao.getUserByToken(eq(sessionToken)))
                .thenReturn(superuser);
        when(userDao.getById(anyInt()))
                .thenReturn(user);
        doAnswer(invocationOnMock -> {
            User upd = invocationOnMock.getArgument(0);
            upd.setRole(UserRole.SUPERUSER);
            upd.setBannedUntil(null);
            return upd;
        })
                .when(userDao)
                .update(any(User.class));

        userService.madeSuperuser(sessionToken, user.getId());
        assertEquals(UserRole.SUPERUSER, user.getRole());

        verify(sessionDao)
                .getUserByToken(eq(sessionToken));
        verify(userDao)
                .getById(anyInt());
        verify(userDao)
                .update(any(User.class));
    }

    @Test
    void testMadeSuperuser_userWasBanned_shouldUnbanUserAndMadeSuperuser() {
        final String sessionToken = UUID.randomUUID().toString();
        final User superuser = new User(123, UserRole.SUPERUSER,
                "superuser", "super@forums.ca", "superpass123",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );
        final User user = new User(456, UserRole.USER,
                "user", "user@forums.ca", "userpass456",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false,
                LocalDateTime.now().plus(3, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS),
                2
        );

        when(sessionDao.getUserByToken(eq(sessionToken)))
                .thenReturn(superuser);
        when(userDao.getById(anyInt()))
                .thenReturn(user);
        doAnswer(invocationOnMock -> {
            User upd = invocationOnMock.getArgument(0);
            upd.setRole(UserRole.SUPERUSER);
            upd.setBannedUntil(null);
            return upd;
        })
                .when(userDao)
                .update(any(User.class));

        userService.madeSuperuser(sessionToken, user.getId());
        assertEquals(UserRole.SUPERUSER, user.getRole());
        assertNull(user.getBannedUntil());

        verify(sessionDao)
                .getUserByToken(eq(sessionToken));
        verify(userDao)
                .getById(anyInt());
        verify(userDao)
                .update(any(User.class));
    }

    @Test
    void testMadeSuperuser_userNotFoundByToken_shouldThrowException() {
        final String sessionToken = UUID.randomUUID().toString();
        when(sessionDao.getUserByToken(eq(sessionToken)))
                .thenReturn(null);

        try {
            userService.madeSuperuser(sessionToken, 456);
        } catch (ServerException e) {
            assertEquals(ErrorCode.WRONG_SESSION_TOKEN, e.getErrorCode());
        }

        verify(sessionDao)
                .getUserByToken(eq(sessionToken));
        verify(userDao, never())
                .getById(anyInt());
        verify(userDao, never())
                .update(any(User.class));
    }

    @Test
    void testMadeSuperuser_requestFromRegularUser_shouldThrowException() {
        final String sessionToken = UUID.randomUUID().toString();
        final User regularUser = new User(123, UserRole.USER,
                "regular_user", "regular@forums.ca", "regularpass123",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );
        when(sessionDao.getUserByToken(eq(sessionToken)))
                .thenReturn(regularUser);

        try {
            userService.madeSuperuser(sessionToken, 456);
        } catch (ServerException e) {
            assertEquals(ErrorCode.FORBIDDEN_OPERATION, e.getErrorCode());
        }

        verify(sessionDao)
                .getUserByToken(eq(sessionToken));
        verify(userDao, never())
                .getById(anyInt());
        verify(userDao, never())
                .update(any(User.class));
    }

    @Test
    void testMadeSuperuser_userNotFoundById_shouldThrowException() {
        final String sessionToken = UUID.randomUUID().toString();
        final User superuser = new User(123, UserRole.SUPERUSER,
                "superuser", "super@forums.ca", "superpass123",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );

        when(sessionDao.getUserByToken(eq(sessionToken)))
                .thenReturn(superuser);
        when(userDao.getById(anyInt()))
                .thenReturn(null);

        try {
            userService.madeSuperuser(sessionToken, 456);
        } catch (ServerException e) {
            assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());
        }

        verify(sessionDao)
                .getUserByToken(eq(sessionToken));
        verify(userDao)
                .getById(anyInt());
        verify(userDao, never())
                .update(any(User.class));
    }

    @Test
    void testBanUser() {
        final String token = "token";
        final int banTime = 7;
        final User superuser = new User(123, UserRole.SUPERUSER,
                "superuser", "super@forums.ca", "superpass123",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );
        final User bannedUser = new User(456, UserRole.USER,
                "user", "user@forums.ca", "userpass456",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );

        when(sessionDao.getUserByToken(anyString()))
                .thenReturn(superuser);
        when(userDao.getById(anyInt()))
                .thenReturn(bannedUser);
        when(properties.getMaxBanCount())
                .thenReturn(5);
        when(properties.getBanTime())
                .thenReturn(banTime);
        doNothing()
                .when(userDao)
                .update(any(User.class));

        userService.banUser(token, bannedUser.getId());
        assertEquals(1, bannedUser.getBanCount());
        assertNotNull(bannedUser.getBannedUntil());

        verify(sessionDao).getUserByToken(anyString());
        verify(userDao).getById(anyInt());
        verify(properties).getMaxBanCount();
        verify(properties).getBanTime();
        verify(userDao).update(any(User.class));
    }

    @Test
    void testBanUser_restrictedUserAreSuperuser_shouldThrowException() {
        final String token = "token";
        final int banTime = 7;
        final User superuser = new User(123, UserRole.SUPERUSER,
                "superuser", "super@forums.ca", "superpass123",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );
        final User bannedUser = new User(456, UserRole.SUPERUSER,
                "user", "user@forums.ca", "userpass456",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );

        when(sessionDao.getUserByToken(anyString()))
                .thenReturn(superuser);
        when(userDao.getById(anyInt()))
                .thenReturn(bannedUser);
        try {
            userService.banUser(token, bannedUser.getId());
        } catch (ServerException e) {
            assertEquals(ErrorCode.FORBIDDEN_OPERATION, e.getErrorCode());
        }

        verify(sessionDao).getUserByToken(anyString());
        verify(userDao).getById(anyInt());
        verifyZeroInteractions(properties);
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void testBanUser_userWasBannedTooMuch_shouldBanPermanently() {
        final String token = "token";
        final int banTime = 7;
        final int maxBanCount = 5;
        final User superuser = new User(123, UserRole.SUPERUSER,
                "superuser", "super@forums.ca", "superpass123",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );
        final User bannedUser = new User(456, UserRole.USER,
                "user", "user@forums.ca", "userpass456",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false,
                null, 4
        );

        when(sessionDao.getUserByToken(anyString()))
                .thenReturn(superuser);
        when(userDao.getById(anyInt())).thenReturn(bannedUser);
        when(properties.getMaxBanCount()).thenReturn(maxBanCount);
        when(properties.getMaxBanCount()).thenReturn(maxBanCount);
        doNothing()
                .when(userDao)
                .update(any(User.class));

        userService.banUser(token, bannedUser.getId());
        assertEquals(maxBanCount, bannedUser.getBanCount());
        assertNotNull(bannedUser.getBannedUntil());
        assertEquals(
                LocalDateTime.of(9999, Month.JANUARY, 1, 0, 0, 0),
                bannedUser.getBannedUntil()
        );

        verify(sessionDao).getUserByToken(anyString());
        verify(userDao).getById(anyInt());
        verify(properties, times(2)).getMaxBanCount();
        verify(userDao).update(any(User.class));
    }

    @Test
    void testBanUser_userNotFoundByToken_shouldThrowException() {
        final String sessionToken = UUID.randomUUID().toString();
        when(sessionDao.getUserByToken(eq(sessionToken)))
                .thenReturn(null);

        try {
            userService.banUser(sessionToken, 456);
        } catch (ServerException e) {
            assertEquals(ErrorCode.WRONG_SESSION_TOKEN, e.getErrorCode());
        }

        verify(sessionDao).getUserByToken(eq(sessionToken));
        verify(userDao, never()).getById(anyInt());
        verifyZeroInteractions(properties);
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void testBanUser_requestFromRegularUser_shouldThrowException() {
        final String sessionToken = UUID.randomUUID().toString();
        final User regularUser = new User(123, UserRole.USER,
                "regular_user", "regular@forums.ca", "regularpass123",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );
        when(sessionDao.getUserByToken(eq(sessionToken)))
                .thenReturn(regularUser);

        try {
            userService.banUser(sessionToken, 456);
        } catch (ServerException e) {
            assertEquals(ErrorCode.FORBIDDEN_OPERATION, e.getErrorCode());
        }

        verify(sessionDao)
                .getUserByToken(eq(sessionToken));
        verify(userDao, never())
                .getById(anyInt());
        verifyZeroInteractions(properties);
        verify(userDao, never())
                .update(any(User.class));
    }

    @Test
    void testBanUser_userNotFoundById_shouldThrowException() {
        final String sessionToken = UUID.randomUUID().toString();
        final User superuser = new User(123, UserRole.SUPERUSER,
                "superuser", "super@forums.ca", "superpass123",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );

        when(sessionDao.getUserByToken(eq(sessionToken)))
                .thenReturn(superuser);
        when(userDao.getById(anyInt()))
                .thenReturn(null);

        try {
            userService.banUser(sessionToken, 456);
        } catch (ServerException e) {
            assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());
        }

        verify(sessionDao)
                .getUserByToken(eq(sessionToken));
        verify(userDao)
                .getById(anyInt());
        verifyZeroInteractions(properties);
        verify(userDao, never())
                .update(any(User.class));
    }
}