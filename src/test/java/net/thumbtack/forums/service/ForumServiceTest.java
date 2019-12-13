package net.thumbtack.forums.service;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.dao.ForumDao;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.dto.requests.forum.CreateForumDtoRequest;
import net.thumbtack.forums.dto.responses.forum.ForumDtoResponse;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import net.thumbtack.forums.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ForumServiceTest {
    private ForumDao mockForumDao;
    private SessionDao mockSessionDao;
    private ForumService forumService;

    @BeforeEach
    void initMocks() {
        mockForumDao = mock(ForumDao.class);
        mockSessionDao = mock(SessionDao.class);
        forumService = new ForumService(mockForumDao, mockSessionDao);
    }

    @Test
    void testCreateForum() {
        final String token = "token";
        final CreateForumDtoRequest request = new CreateForumDtoRequest("testForum", ForumType.UNMODERATED);
        final User user = new User("user", "user@email.com", "password=pass");
        final Forum forum = new Forum(
                123, request.getType(), user, request.getName(),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );
        final ForumDtoResponse expectedResponse = new ForumDtoResponse(
                forum.getId(), request.getName(), request.getType()
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        doAnswer(invocationOnMock -> {
            Forum arg = invocationOnMock.getArgument(0);
            arg.setId(forum.getId());
            return arg;
        })
                .when(mockForumDao)
                .save(any(Forum.class));

        final ForumDtoResponse actualResponse = forumService.createForum(token, request);
        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockForumDao).save(any(Forum.class));
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void testCreateForum_userNotFoundByToken_shouldThrowException() {
        final String token = "token";
        final CreateForumDtoRequest request = new CreateForumDtoRequest("testForum", ForumType.UNMODERATED);
        when(mockSessionDao.getUserByToken(anyString()))
                .thenThrow(new ServerException(ErrorCode.WRONG_SESSION_TOKEN));

        try {
            forumService.createForum(token, request);
        } catch (ServerException ex) {
            assertEquals(ErrorCode.WRONG_SESSION_TOKEN, ex.getErrorCode());
        }
        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockForumDao, never()).save(any(Forum.class));
    }

    @Test
    void testCreateForum_userBanned_shouldThrowException() {
        final String token = "token";
        final CreateForumDtoRequest request = new CreateForumDtoRequest("testForum", ForumType.UNMODERATED);
        final User user = new User("user", "user@email.com", "password=pass");
        user.setBannedUntil(LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS));
        user.setBanCount(1);

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(user);

        try {
            forumService.createForum(token, request);
        } catch (ServerException ex) {
            assertEquals(ErrorCode.USER_BANNED, ex.getErrorCode());
        }
        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockForumDao, never()).save(any(Forum.class));
    }

    @Test
    void testDeleteForum() {
        final String token = "token";
        final User user = new User("user", "user@email.com", "password=pass");
        final Forum forum = new Forum(
                123, ForumType.UNMODERATED, user, "name",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        when(mockForumDao.getById(anyInt()))
                .thenReturn(forum);
        doNothing()
                .when(mockForumDao)
                .deleteById(anyInt());

        forumService.deleteForum(token, forum.getId());
        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockForumDao).getById(anyInt());
        verify(mockForumDao).deleteById(anyInt());
    }

    @Test
    void testDeleteForum_userNotForumOwner_shouldThrowException() {
        final String token = "token";
        final User user = new User("user", "user@email.com", "password=pass");
        final User forumOwner = new User("owner", "owner@email.com", "owner_passwd");
        final Forum forum = new Forum(
                123, ForumType.UNMODERATED, forumOwner, "name",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        when(mockForumDao.getById(anyInt()))
                .thenReturn(forum);

        try {
            forumService.deleteForum(token, forum.getId());
        } catch (ServerException ex) {
            assertEquals(ErrorCode.FORBIDDEN_OPERATION, ex.getErrorCode());
        }

        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockForumDao).getById(anyInt());
        verify(mockForumDao, never()).deleteById(anyInt());
    }

    @Test
    void testDeleteForum_userHasRoleSuperuser_shouldDeleteForum() {
        final String token = "token";
        final User user = new User("super", "super@email.com", "password=pass");
        user.setRole(UserRole.SUPERUSER);

        final User forumOwner = new User("owner", "owner@email.com", "owner_passwd");
        final Forum forum = new Forum(
                123, ForumType.UNMODERATED, forumOwner, "name",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        when(mockForumDao.getById(anyInt()))
                .thenReturn(forum);

        forumService.deleteForum(token, forum.getId());
        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockForumDao).getById(anyInt());
        verify(mockForumDao).deleteById(anyInt());
    }
}