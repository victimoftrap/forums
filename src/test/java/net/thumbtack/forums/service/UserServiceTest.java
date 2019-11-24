package net.thumbtack.forums.service;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.UserRole;
import net.thumbtack.forums.model.UserSession;
import net.thumbtack.forums.dao.UserDao;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.dto.UserDtoResponse;
import net.thumbtack.forums.dto.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.LoginUserDtoRequest;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceTest {
    private UserDao userDao;
    private SessionDao sessionDao;
    private PasswordEncoder passwordEncoder;
    private UserService userService;

    @BeforeEach
    void initMocks() {
        userDao = mock(UserDao.class);
        sessionDao = mock(SessionDao.class);
        passwordEncoder = mock(PasswordEncoder.class);

        userService = new UserService(userDao, sessionDao, passwordEncoder);
    }

    @Test
    void testRegisterUser() throws Exception {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "jolybell",
                "ahoi@jolybell.com",
                "password123"
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
        when(passwordEncoder.encode(anyString()))
                .thenReturn(request.getPassword());
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

        verify(userDao, times(1))
                .getByName(anyString(), anyBoolean());
        verify(passwordEncoder, times(1))
                .encode(anyString());
        verify(userDao, times(1))
                .save(any(User.class));
        verify(sessionDao, times(1))
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
                .thenThrow(new ServerException(ErrorCode.USER_WITH_THIS_NAME_EXISTS));
        when(userDao.getByName(eq(request.getName()), anyBoolean()))
                .thenThrow(new ServerException(ErrorCode.USER_WITH_THIS_NAME_EXISTS));

        assertThrows(ServerException.class,
                () -> userService.registerUser(request)
        );

        try {
            userService.registerUser(request);
        } catch (ServerException e) {
            assertEquals(ErrorCode.USER_WITH_THIS_NAME_EXISTS, e.getErrorCode());
        }

        verify(userDao, times(2))
                .getByName(eq(request.getName()), anyBoolean());
        verifyZeroInteractions(passwordEncoder);
        verifyZeroInteractions(sessionDao);
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

        verify(userDao, times(1))
                .getByName(anyString(), anyBoolean());
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
        when(userDao.save(any(User.class)))
                .thenThrow(new ServerException(ErrorCode.DATABASE_ERROR));

        try {
            userService.registerUser(request);
        } catch (ServerException e) {
            assertEquals(ErrorCode.DATABASE_ERROR, e.getErrorCode());
        }

        verify(userDao, times(1))
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
    }

    @Test
    void testLoginUser() {
        final LoginUserDtoRequest request = new LoginUserDtoRequest(
                "while",
                "thirdman"
        );
        final User user = new User(
                request.getName(),
                "white@thirdman.com",
                request.getPassword()
        );

        when(userDao.getByName(anyString()))
                .thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(true);
        doNothing()
                .when(sessionDao)
                .upsertSession(any(UserSession.class));

        final UserDtoResponse response = userService.login(request);

        verify(userDao).getByName(anyString());
        verify(passwordEncoder).matches(anyString(), anyString());
        verify(sessionDao).upsertSession(any(UserSession.class));

        assertEquals(user.getId(), response.getId());
        assertEquals(user.getUsername(), response.getName());
        assertEquals(user.getEmail(), response.getEmail());
    }

    @Test
    void testLoginUser_userNotFound_throwsException() {
        final LoginUserDtoRequest request = new LoginUserDtoRequest(
                "while",
                "thirdman"
        );
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
            assertEquals(ErrorCode.USER_NOT_FOUND_BY_NAME, e.getErrorCode());
        }

        verify(userDao, times(2)).getByName(anyString());
        verifyZeroInteractions(passwordEncoder);
        verifyZeroInteractions(sessionDao);
    }

    @Test
    void testLoginUser_requestedPasswordNotMatches_throwsException() {
        final LoginUserDtoRequest request = new LoginUserDtoRequest(
                "while",
                "thirdman"
        );
        final User user = new User(
                request.getName(),
                "white@thirdman.com",
                request.getPassword()
        );

        when(userDao.getByName(anyString()))
                .thenReturn(user);
        when(passwordEncoder.matches(anyString(), anyString()))
                .thenReturn(false);

        try {
            userService.login(request);
        } catch (ServerException e) {
            assertEquals(ErrorCode.USER_PASSWORD_NOT_MATCHES, e.getErrorCode());
        }

        verify(userDao).getByName(anyString());
        verify(passwordEncoder).matches(anyString(), anyString());
        verifyZeroInteractions(sessionDao);
    }

    @Test
    void testLogoutUser() {
        final String sessionToken = "token";
        final User user = new User(
                "alvvays",
                "aloha@alvvays.ca",
                "dives"
        );

        when(sessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        doNothing()
                .when(sessionDao)
                .deleteSession(anyString());

        userService.logout(sessionToken);

        verify(sessionDao, times(1))
                .getUserByToken(anyString());
        verify(sessionDao, times(1))
                .deleteSession(anyString());
    }
}