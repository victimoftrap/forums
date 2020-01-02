package net.thumbtack.forums.service;

import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.UserRole;
import net.thumbtack.forums.dao.ForumDao;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.dto.requests.forum.CreateForumDtoRequest;
import net.thumbtack.forums.dto.responses.forum.ForumDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumInfoDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumInfoListDtoResponse;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ForumServiceTest {
    private ForumDao mockForumDao;
    private SessionDao mockSessionDao;
    private ForumService forumService;
    private ServerConfigurationProperties mockServerProperties;

    @BeforeEach
    void initMocks() {
        mockForumDao = mock(ForumDao.class);
        mockSessionDao = mock(SessionDao.class);
        mockServerProperties = mock(ServerConfigurationProperties.class);
        forumService = new ForumService(mockForumDao, mockSessionDao, mockServerProperties);
    }

    @Test
    void testCreateForum() throws ServerException {
        final String token = "token";
        final CreateForumDtoRequest request = new CreateForumDtoRequest(
                "testForum", ForumType.UNMODERATED.name()
        );
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
    void testCreateForum_userNotFoundByToken_shouldThrowException() throws ServerException {
        final String token = "token";
        final CreateForumDtoRequest request = new CreateForumDtoRequest(
                "testForum", ForumType.UNMODERATED.name()
        );
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
    void testCreateForum_userBanned_shouldThrowException() throws ServerException {
        final String token = "token";
        final CreateForumDtoRequest request = new CreateForumDtoRequest(
                "testForum", ForumType.UNMODERATED.name()
        );
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
    void testDeleteForum_userForumOwner_shouldDeleteForum() throws ServerException {
        final int maxBanCount = 5;
        final String token = "token";
        final User forumOwner = new User(
                "user", "user@email.com", "password=pass"
        );
        final Forum forum = new Forum(
                123, ForumType.UNMODERATED, forumOwner, "name",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(forumOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockForumDao.getById(anyInt()))
                .thenReturn(forum);
        doNothing()
                .when(mockForumDao)
                .deleteById(anyInt());

        forumService.deleteForum(token, forum.getId());
        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockServerProperties).getMaxBanCount();
        verify(mockForumDao).getById(anyInt());
        verify(mockForumDao).deleteById(anyInt());
    }

    @Test
    void testDeleteForum_userHasRoleSuperuser_shouldDeleteForum() throws ServerException {
        final int maxBanCount = 5;
        final String token = "token";
        final User superuser = new User(
                "super", "super@email.com", "password=pass"
        );
        superuser.setRole(UserRole.SUPERUSER);

        final User forumOwner = new User(
                "owner", "owner@email.com", "owner_passwd"
        );
        final Forum forum = new Forum(
                123, ForumType.UNMODERATED, forumOwner, "name",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(superuser);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockForumDao.getById(anyInt()))
                .thenReturn(forum);

        forumService.deleteForum(token, forum.getId());
        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockServerProperties).getMaxBanCount();
        verify(mockForumDao).getById(anyInt());
        verify(mockForumDao).deleteById(anyInt());
    }

    @Test
    void testDeleteForum_userNotForumOwner_shouldThrowException() throws ServerException {
        final int maxBanCount = 5;
        final String token = "token";
        final User user = new User("user", "user@email.com", "password=pass");
        final User forumOwner = new User("owner", "owner@email.com", "owner_passwd");
        final Forum forum = new Forum(
                123, ForumType.UNMODERATED, forumOwner, "name",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockForumDao.getById(anyInt()))
                .thenReturn(forum);

        try {
            forumService.deleteForum(token, forum.getId());
        } catch (ServerException ex) {
            assertEquals(ErrorCode.FORBIDDEN_OPERATION, ex.getErrorCode());
        }

        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockServerProperties).getMaxBanCount();
        verify(mockForumDao).getById(anyInt());
        verify(mockForumDao, never()).deleteById(anyInt());
    }

    @Test
    void testDeleteForum_userPermanentlyBanned_shouldThrowException() throws ServerException {
        final String token = "token";
        final int maxBanCount = 5;
        final User forumOwner = new User("owner", "owner@email.com", "owner_passwd");
        forumOwner.setBanCount(maxBanCount);
        final Forum forum = new Forum(
                123, ForumType.UNMODERATED, forumOwner, "name",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS), false
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(forumOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);

        try {
            forumService.deleteForum(token, forum.getId());
        } catch (ServerException ex) {
            assertEquals(ErrorCode.USER_PERMANENTLY_BANNED, ex.getErrorCode());
        }

        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockForumDao, never()).getById(anyInt());
        verify(mockForumDao, never()).deleteById(anyInt());
    }

    @Test
    void testGetForumById() throws ServerException {
        final String token = "token";
        final int forumId = 12020;

        final User requesterUser = new User(
                "requester", "user@mail.com", "weryHArdtoMakePa55w0rd"
        );
        final User forumOwner = new User(
                "forumOwner", "forumOwner@mail.com", "weryHArdtoMakePa55w0rd"
        );
        final Forum forum = new Forum(
                ForumType.UNMODERATED, forumOwner, "ForumName",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(requesterUser);
        when(mockForumDao.getById(anyInt()))
                .thenReturn(forum);

        final ForumInfoDtoResponse response = forumService.getForum(token, forumId);
        assertEquals(forum.getId(), response.getId());
        assertEquals(forum.getName(), response.getName());
        assertEquals(forum.getType().name(), response.getType());
        assertEquals(forum.getOwner().getUsername(), response.getCreatorName());
        assertEquals(forum.isReadonly(), response.isReadonly());

        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockForumDao).getById(anyInt());
    }

    @Test
    void testGetForumById_userNotFound_shouldThrowException() throws ServerException {
        final String token = "token";
        final int forumId = 12020;
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(null);

        try {
            forumService.getForum(token, forumId);
        } catch (ServerException se) {
            assertEquals(ErrorCode.WRONG_SESSION_TOKEN, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockForumDao, never())
                .getById(anyInt());
    }

    @Test
    void testGetForumById_forumNotFound_shouldThrowException() throws ServerException {
        final String token = "token";
        final int forumId = 12020;
        final User requesterUser = new User(
                "requester", "user@mail.com", "weryHArdtoMakePa55w0rd"
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(requesterUser);
        when(mockForumDao.getById(anyInt()))
                .thenReturn(null);

        try {
            forumService.getForum(token, forumId);
        } catch (ServerException se) {
            assertEquals(ErrorCode.FORUM_NOT_FOUND, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockForumDao)
                .getById(anyInt());
    }

    @Test
    void testGetForumList() throws ServerException {
        final String token = "token";
        final User requesterUser = new User(
                "requester", "user@mail.com", "weryHArdtoMakePa55w0rd"
        );

        final User forumOwner1 = new User(
                "forumOwner1", "forumOwner1@mail.com", "weryHArdtoMakePa55w0rd"
        );
        final Forum forum1 = new Forum(
                ForumType.UNMODERATED, forumOwner1, "ForumName1",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        final User forumOwner2 = new User(
                "forumOwner2", "forumOwner2@mail.com", "weryHArdtoMakePa55w0rd"
        );
        final Forum forum2 = new Forum(
                ForumType.MODERATED, forumOwner2, "ForumName2",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        final User forumOwner3 = new User(
                "forumOwner3", "forumOwner3@mail.com", "weryHArdtoMakePa55w0rd"
        );
        final Forum forum3 = new Forum(
                ForumType.MODERATED, forumOwner3, "ForumName3",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        forum3.setReadonly(true);
        final List<Forum> forumList = Arrays.asList(forum1, forum2, forum3);

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(requesterUser);
        when(mockForumDao.getAll())
                .thenReturn(forumList);

        final ForumInfoListDtoResponse response = forumService.getForums(token);
        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockForumDao).getAll();

        final List<ForumInfoDtoResponse> forumListResponse = response.getForums();
        assertEquals(forum1.getName(), forumListResponse.get(0).getName());
        assertEquals(forum1.getOwner().getUsername(), forumListResponse.get(0).getCreatorName());
        assertEquals(forum1.getType().name(), forumListResponse.get(0).getType());
        assertEquals(forum1.isReadonly(), forumListResponse.get(0).isReadonly());

        assertEquals(forum2.getName(), forumListResponse.get(1).getName());
        assertEquals(forum2.getOwner().getUsername(), forumListResponse.get(1).getCreatorName());
        assertEquals(forum2.getType().name(), forumListResponse.get(1).getType());
        assertEquals(forum2.isReadonly(), forumListResponse.get(1).isReadonly());

        assertEquals(forum3.getName(), forumListResponse.get(2).getName());
        assertEquals(forum3.getOwner().getUsername(), forumListResponse.get(2).getCreatorName());
        assertEquals(forum3.getType().name(), forumListResponse.get(2).getType());
        assertEquals(forum3.isReadonly(), forumListResponse.get(2).isReadonly());
    }

    @Test
    void testGetForumList_noForumsFound_shouldReturnEmptyList() throws ServerException {
        final String token = "token";
        final User requesterUser = new User(
                "requester", "user@mail.com", "weryHArdtoMakePa55w0rd"
        );
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(requesterUser);
        when(mockForumDao.getAll())
                .thenReturn(Collections.emptyList());

        final ForumInfoListDtoResponse response = forumService.getForums(token);
        assertEquals(0, response.getForums().size());

        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockForumDao)
                .getAll();
    }

    @Test
    void testGetForumList_userNotFound_shouldThrowException() throws ServerException {
        final String token = "token";
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(null);

        try {
            forumService.getForums(token);
        } catch (ServerException se) {
            assertEquals(ErrorCode.WRONG_SESSION_TOKEN, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockForumDao, never())
                .getAll();
    }
}