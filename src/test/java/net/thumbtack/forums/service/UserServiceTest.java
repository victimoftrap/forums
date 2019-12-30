package net.thumbtack.forums.service;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.UserRole;
import net.thumbtack.forums.model.UserSession;
import net.thumbtack.forums.dao.ForumDao;
import net.thumbtack.forums.dao.UserDao;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.dto.requests.user.LoginUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.UpdatePasswordDtoRequest;
import net.thumbtack.forums.dto.responses.user.*;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.configuration.ConstantsProperties;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
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
    private ForumDao mockForumDao;
    private ServerConfigurationProperties mockConfigurationProperties;
    private ConstantsProperties mockConstantsProperties;
    private UserService userService;

    @BeforeEach
    void initMocks() {
        userDao = mock(UserDao.class);
        sessionDao = mock(SessionDao.class);
        mockForumDao = mock(ForumDao.class);
        mockConstantsProperties = mock(ConstantsProperties.class);
        mockConfigurationProperties = mock(ServerConfigurationProperties.class);

        userService = new UserService(
                userDao, sessionDao, mockForumDao,
                mockConstantsProperties, mockConfigurationProperties
        );
    }

    @Test
    void testRegisterUser() throws ServerException {
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
            return invocationOnMock.getArgument(1);
        })
                .when(userDao)
                .save(any(User.class), any(UserSession.class));

        final UserDtoResponse actualResponse = userService.registerUser(request);
        verify(userDao)
                .getByName(anyString(), anyBoolean());
        verify(userDao)
                .save(any(User.class), any(UserSession.class));
        verifyZeroInteractions(sessionDao);

        assertEquals(expectedResponse.getId(), actualResponse.getId());
        assertEquals(expectedResponse.getName(), actualResponse.getName());
        assertEquals(expectedResponse.getEmail(), actualResponse.getEmail());
    }

    @Test
    void testRegisterUser_userWithRequestedNameExists_shouldThrowException() throws ServerException {
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
    void testRegisterUser_onGetUserDatabaseError_shouldThrowException() throws ServerException {
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
    void testRegisterUser_onSaveUserDatabaseError_shouldThrowException() throws ServerException {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "jolybell",
                "ahoi@jolybell.com",
                "password123"
        );

        when(userDao.getByName(anyString(), anyBoolean()))
                .thenReturn(null);
        when(userDao.save(any(User.class), any(UserSession.class)))
                .thenThrow(new ServerException(ErrorCode.DATABASE_ERROR));

        try {
            userService.registerUser(request);
        } catch (ServerException e) {
            assertEquals(ErrorCode.DATABASE_ERROR, e.getErrorCode());
        }

        verify(userDao)
                .getByName(anyString(), anyBoolean());
        verify(userDao)
                .save(any(User.class), any(UserSession.class));
        verifyZeroInteractions(sessionDao);
    }

    @Test
    void testDeleteUser() throws ServerException {
        final String token = "token";
        final User user = new User(456, UserRole.USER,
                "user", "user@forums.ca", "userpass456",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );

        when(sessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        doNothing()
                .when(userDao)
                .deactivateById(anyInt());

        userService.deleteUser(token);

        verify(sessionDao)
                .getUserByToken(eq(token));
        verify(userDao)
                .deactivateById(anyInt());
        verifyZeroInteractions(sessionDao);
    }

    @Test
    void testDeleteUser_userNotFoundByToken_shouldThrowException() throws ServerException {
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
    void testLoginUser() throws ServerException {
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
    void testLoginUser_loginWithCaseInsensitiveName_shouldSuccessfullyLogin() throws ServerException {
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
    void testLoginUser_userNotFound_shouldThrowException() throws ServerException {
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
    void testLoginUser_requestedPasswordNotMatches_shouldThrowException() throws ServerException {
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
    void testLogoutUser() throws ServerException {
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
    void testLogoutUser_userNotFoundByToken_shouldThrowException() throws ServerException {
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
    void testLogoutUser_onGetSessionDatabaseError_shouldThrowException() throws ServerException {
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
    void testLogoutUser_onDeleteSessionDatabaseError_shouldThrowException() throws ServerException {
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
    void testUpdatePassword() throws ServerException {
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
    void testUpdatePassword_userNotFoundByToken_shouldThrowException() throws ServerException {
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
    void testUpdatePassword_passwordTooShort_shouldThrowException() throws ServerException {
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
    void testMadeSuperuser() throws ServerException {
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
        when(userDao.getById(anyInt(), anyBoolean()))
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
                .getById(anyInt(), anyBoolean());
        verify(userDao)
                .update(any(User.class));
    }

    @Test
    void testMadeSuperuser_userWasBanned_shouldUnbanUserAndMadeSuperuser() throws ServerException {
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
        when(userDao.getById(anyInt(), anyBoolean()))
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
                .getById(anyInt(), anyBoolean());
        verify(userDao)
                .update(any(User.class));
    }

    @Test
    void testMadeSuperuser_userNotFoundByToken_shouldThrowException() throws ServerException {
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
                .getById(anyInt(), anyBoolean());
        verify(userDao, never())
                .update(any(User.class));
    }

    @Test
    void testMadeSuperuser_requestFromRegularUser_shouldThrowException() throws ServerException {
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
                .getById(anyInt(), anyBoolean());
        verify(userDao, never())
                .update(any(User.class));
    }

    @Test
    void testMadeSuperuser_userNotFoundOrDeletedById_shouldThrowException() throws ServerException {
        final String sessionToken = UUID.randomUUID().toString();
        final User superuser = new User(123, UserRole.SUPERUSER,
                "superuser", "super@forums.ca", "superpass123",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );

        when(sessionDao.getUserByToken(eq(sessionToken)))
                .thenReturn(superuser);
        when(userDao.getById(anyInt(), anyBoolean()))
                .thenReturn(null);

        try {
            userService.madeSuperuser(sessionToken, 456);
        } catch (ServerException e) {
            assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());
        }

        verify(sessionDao)
                .getUserByToken(eq(sessionToken));
        verify(userDao)
                .getById(anyInt(), anyBoolean());
        verify(userDao, never())
                .update(any(User.class));
    }

    @Test
    void testBanUser_userReceivedBan_bannedForSomeDays() throws ServerException {
        final String token = "token";
        final int banDays = 7;
        final int maxBanCount = 5;
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
        when(userDao.getById(anyInt(), anyBoolean()))
                .thenReturn(bannedUser);
        when(mockConfigurationProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockConfigurationProperties.getBanTime())
                .thenReturn(banDays);
        doNothing()
                .when(userDao)
                .update(any(User.class));

        userService.banUser(token, bannedUser.getId());
        assertEquals(1, bannedUser.getBanCount());
        assertNotNull(bannedUser.getBannedUntil());

        verify(sessionDao)
                .getUserByToken(anyString());
        verify(userDao)
                .getById(anyInt(), anyBoolean());
        verify(mockConfigurationProperties)
                .getMaxBanCount();
        verify(mockConfigurationProperties)
                .getBanTime();
        verify(userDao)
                .update(any(User.class));
        verify(mockConstantsProperties, never())
                .getDatetimePattern();
        verify(mockConstantsProperties, never())
                .getPermanentBanDatetime();
    }

    @Test
    void testBanUser_userReceivedLastBan_bannedPermanently() throws ServerException {
        final String token = "token";
        final int maxBanCount = 5;
        final String datetimePattern = "yyyy-MM-dd HH:mm:ss";
        final String permanentDatetime = "9999-01-01 00:00:00";
        final User superuser = new User(123, UserRole.SUPERUSER,
                "superuser", "super@forums.ca", "superpass123",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );
        final User bannedUser = new User(456, UserRole.USER,
                "user", "user@forums.ca", "userpass456",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );
        bannedUser.setBanCount(4);

        when(sessionDao.getUserByToken(anyString()))
                .thenReturn(superuser);
        when(userDao.getById(anyInt(), anyBoolean()))
                .thenReturn(bannedUser);
        when(mockConfigurationProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockConstantsProperties.getDatetimePattern())
                .thenReturn(datetimePattern);
        when(mockConstantsProperties.getPermanentBanDatetime())
                .thenReturn(permanentDatetime);
        doNothing()
                .when(userDao)
                .update(any(User.class));

        userService.banUser(token, bannedUser.getId());
        assertEquals(maxBanCount, bannedUser.getBanCount());
        assertNotNull(bannedUser.getBannedUntil());

        verify(sessionDao)
                .getUserByToken(anyString());
        verify(userDao)
                .getById(anyInt(), anyBoolean());
        verify(mockConfigurationProperties)
                .getMaxBanCount();
        verify(mockConstantsProperties)
                .getDatetimePattern();
        verify(mockConstantsProperties)
                .getPermanentBanDatetime();
        verify(userDao)
                .update(any(User.class));
        verify(mockConfigurationProperties, never())
                .getBanTime();
    }

    @Test
    void testBanUser_restrictedUserAreSuperuser_shouldThrowException() throws ServerException {
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
        when(userDao.getById(anyInt(), anyBoolean()))
                .thenReturn(bannedUser);
        try {
            userService.banUser(token, bannedUser.getId());
        } catch (ServerException e) {
            assertEquals(ErrorCode.FORBIDDEN_OPERATION, e.getErrorCode());
        }

        verify(sessionDao).getUserByToken(anyString());
        verify(userDao).getById(anyInt(), anyBoolean());
        verifyZeroInteractions(mockConfigurationProperties);
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void testBanUser_userWasBannedTooMuch_shouldBanPermanently() throws ServerException {
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
        when(userDao.getById(anyInt(), anyBoolean()))
                .thenReturn(bannedUser);
        when(mockConfigurationProperties.getMaxBanCount()).thenReturn(maxBanCount);
        when(mockConfigurationProperties.getMaxBanCount()).thenReturn(maxBanCount);
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
        verify(userDao).getById(anyInt(), anyBoolean());
        verify(mockConfigurationProperties, times(2)).getMaxBanCount();
        verify(userDao).update(any(User.class));
    }

    @Test
    void testBanUser_userNotFoundByToken_shouldThrowException() throws ServerException {
        final String sessionToken = UUID.randomUUID().toString();
        when(sessionDao.getUserByToken(eq(sessionToken)))
                .thenReturn(null);

        try {
            userService.banUser(sessionToken, 456);
        } catch (ServerException e) {
            assertEquals(ErrorCode.WRONG_SESSION_TOKEN, e.getErrorCode());
        }

        verify(sessionDao).getUserByToken(eq(sessionToken));
        verify(userDao, never()).getById(anyInt(), anyBoolean());
        verifyZeroInteractions(mockConfigurationProperties);
        verify(userDao, never()).update(any(User.class));
    }

    @Test
    void testBanUser_requestFromRegularUser_shouldThrowException() throws ServerException {
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
                .getById(anyInt(), anyBoolean());
        verifyZeroInteractions(mockConfigurationProperties);
        verify(userDao, never())
                .update(any(User.class));
    }

    @Test
    void testBanUser_userNotFoundOrDeletedById_shouldThrowException() throws ServerException {
        final String sessionToken = UUID.randomUUID().toString();
        final User superuser = new User(123, UserRole.SUPERUSER,
                "superuser", "super@forums.ca", "superpass123",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );

        when(sessionDao.getUserByToken(eq(sessionToken)))
                .thenReturn(superuser);
        when(userDao.getById(anyInt(), anyBoolean()))
                .thenReturn(null);

        try {
            userService.banUser(sessionToken, 456);
        } catch (ServerException e) {
            assertEquals(ErrorCode.USER_NOT_FOUND, e.getErrorCode());
        }

        verify(sessionDao)
                .getUserByToken(eq(sessionToken));
        verify(userDao)
                .getById(anyInt(), anyBoolean());
        verifyZeroInteractions(mockConfigurationProperties);
        verify(userDao, never())
                .update(any(User.class));
    }

    @Test
    void testGetAllUsers_requestFromRegularUser_shouldReturnNotAllFields() throws ServerException {
        final String sessionToken = UUID.randomUUID().toString();
        final User user0 = new User("user0", "user0@gamil.com", "user_passwd_0");
        final User user1 = new User("user1", "user1@gamil.com", "user_passwd_1");
        final User user2 = new User(
                UserRole.USER,
                "user2", "user2@gamil.com", "user_passwd_2",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), true,
                null, 3
        );
        final User superuser = new User(
                UserRole.SUPERUSER,
                "superuser", "superuser@gamil.com", "super_passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );
        final List<UserSession> sessions = Arrays.asList(
                new UserSession(user0, UUID.randomUUID().toString()),
                new UserSession(user1, null),
                new UserSession(user2, UUID.randomUUID().toString()),
                new UserSession(superuser, UUID.randomUUID().toString())
        );

        final UserDetailsDtoResponse response0 = new UserDetailsDtoResponse(
                user0.getId(), user0.getUsername(), null, user0.getRegisteredAt(),
                true, user0.isDeleted(), null, UserStatus.FULL,
                user0.getBannedUntil(), user0.getBanCount()
        );
        final UserDetailsDtoResponse response1 = new UserDetailsDtoResponse(
                user1.getId(), user1.getUsername(), null, user1.getRegisteredAt(),
                false, user1.isDeleted(), null, UserStatus.FULL,
                user1.getBannedUntil(), user1.getBanCount()
        );
        final UserDetailsDtoResponse response2 = new UserDetailsDtoResponse(
                user2.getId(), user2.getUsername(), null, user2.getRegisteredAt(),
                true, user2.isDeleted(), null, UserStatus.FULL,
                user2.getBannedUntil(), user2.getBanCount()
        );
        final UserDetailsDtoResponse response3 = new UserDetailsDtoResponse(
                superuser.getId(), superuser.getUsername(), null, superuser.getRegisteredAt(),
                true, superuser.isDeleted(), null, UserStatus.FULL,
                superuser.getBannedUntil(), superuser.getBanCount()
        );
        final UserDetailsListDtoResponse expectedResponse = new UserDetailsListDtoResponse(
                Arrays.asList(response0, response1, response2, response3)
        );

        when(sessionDao.getUserByToken(anyString()))
                .thenReturn(user0);
        when(userDao.getAllWithSessions())
                .thenReturn(sessions);

        final UserDetailsListDtoResponse actualResponse = userService.getUsers(sessionToken);

        verify(sessionDao).getUserByToken(anyString());
        verify(userDao).getAllWithSessions();
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void testGetAllUsers_requestFromSuperuser_shouldReturnAllFields() throws ServerException {
        final String sessionToken = UUID.randomUUID().toString();
        final User user0 = new User("user0", "user0@gamil.com", "user_passwd_0");
        final User user1 = new User("user1", "user1@gamil.com", "user_passwd_1");
        final User user2 = new User(
                UserRole.USER,
                "user2", "user2@gamil.com", "user_passwd_2",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), true,
                null, 3
        );
        final User superuser = new User(
                UserRole.SUPERUSER,
                "superuser", "superuser@gamil.com", "super_passwd",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );
        final List<UserSession> sessions = Arrays.asList(
                new UserSession(user0, UUID.randomUUID().toString()),
                new UserSession(user1, null),
                new UserSession(user2, UUID.randomUUID().toString()),
                new UserSession(superuser, UUID.randomUUID().toString())
        );

        final UserDetailsDtoResponse response0 = new UserDetailsDtoResponse(
                user0.getId(), user0.getUsername(), user0.getEmail(), user0.getRegisteredAt(),
                true, user0.isDeleted(), false, UserStatus.FULL,
                user0.getBannedUntil(), user0.getBanCount()
        );
        final UserDetailsDtoResponse response1 = new UserDetailsDtoResponse(
                user1.getId(), user1.getUsername(), user1.getEmail(), user1.getRegisteredAt(),
                false, user1.isDeleted(), false, UserStatus.FULL,
                user1.getBannedUntil(), user1.getBanCount()
        );
        final UserDetailsDtoResponse response2 = new UserDetailsDtoResponse(
                user2.getId(), user2.getUsername(), user2.getEmail(), user2.getRegisteredAt(),
                true, user2.isDeleted(), false, UserStatus.FULL,
                user2.getBannedUntil(), user2.getBanCount()
        );
        final UserDetailsDtoResponse response3 = new UserDetailsDtoResponse(
                superuser.getId(), superuser.getUsername(), superuser.getEmail(), superuser.getRegisteredAt(),
                true, superuser.isDeleted(), true, UserStatus.FULL,
                superuser.getBannedUntil(), superuser.getBanCount()
        );
        final UserDetailsListDtoResponse expectedResponse = new UserDetailsListDtoResponse(
                Arrays.asList(response0, response1, response2, response3)
        );

        when(sessionDao.getUserByToken(anyString()))
                .thenReturn(superuser);
        when(userDao.getAllWithSessions())
                .thenReturn(sessions);

        final UserDetailsListDtoResponse actualResponse = userService.getUsers(sessionToken);

        verify(sessionDao).getUserByToken(anyString());
        verify(userDao).getAllWithSessions();
        assertEquals(expectedResponse, actualResponse);
    }
}