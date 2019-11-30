package net.thumbtack.forums.service;

import net.thumbtack.forums.dao.ForumDao;
import net.thumbtack.forums.dao.SessionDao;

import net.thumbtack.forums.dto.forum.CreateForumDtoRequest;
import net.thumbtack.forums.dto.forum.ForumDtoResponse;
import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.ForumType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

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
        final CreateForumDtoRequest request = new CreateForumDtoRequest("test-forum", ForumType.UNMODERATED);
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
}