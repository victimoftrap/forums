package net.thumbtack.forums.service;

import net.thumbtack.forums.dao.*;

import net.thumbtack.forums.dto.message.CreateCommentDtoRequest;
import net.thumbtack.forums.dto.message.CreateMessageDtoRequest;
import net.thumbtack.forums.dto.message.MessageDtoResponse;
import net.thumbtack.forums.model.*;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.model.enums.MessagePriority;
import net.thumbtack.forums.model.enums.MessageState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MessageServiceTest {
    private SessionDao mockSessionDao;
    private ForumDao mockForumDao;
    private MessageTreeDao mockMessageTreeDao;
    private MessageDao mockMessageDao;
    private MessageHistoryDao mockMessageHistoryDao;
    private MessageService messageService;

    @BeforeEach
    void initMocks() {
        mockSessionDao = mock(SessionDao.class);
        mockForumDao = mock(ForumDao.class);
        mockMessageTreeDao = mock(MessageTreeDao.class);
        mockMessageDao = mock(MessageDao.class);
        mockMessageHistoryDao = mock(MessageHistoryDao.class);

        messageService = new MessageService(mockSessionDao, mockForumDao,
                mockMessageTreeDao, mockMessageDao, mockMessageHistoryDao
        );
    }

    @Test
    void testCreateMessage() {
        final User user = new User("MarvinGaye", "gaye@motown.com", "whatsGoingOn");
        final Forum forum = new Forum(ForumType.UNMODERATED, user,
                "Soul Music", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        final String token = "token";
        final int forumId = 1939;
        final CreateMessageDtoRequest request = new CreateMessageDtoRequest(
                "My new album", "I wrote new album",
                MessagePriority.HIGH, null
        );
        final int messageId = 9391;
        final MessageDtoResponse expectedResponse = new MessageDtoResponse(messageId, MessageState.PUBLISHED);

        when(mockSessionDao.getUserByToken(anyString())).thenReturn(user);
        when(mockForumDao.getById(anyInt())).thenReturn(forum);
        doAnswer(invocationOnMock -> {
            MessageTree tree = invocationOnMock.getArgument(0);
            tree.getRootMessage().setId(messageId);
            return tree;
        })
                .when(mockMessageTreeDao)
                .saveMessageTree(any(MessageTree.class));

        final MessageDtoResponse actualResponse = messageService.addMessage(token, forumId, request);
        assertEquals(expectedResponse, actualResponse);

        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockForumDao).getById(anyInt());
        verify(mockMessageTreeDao).saveMessageTree(any(MessageTree.class));
    }

    @Test
    void testCreateComment() {
        final User user = new User("MarvinGaye", "gaye@motown.com", "whatsGoingOn");
        final Forum forum = new Forum(ForumType.UNMODERATED, user,
                "Soul Music", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "I made new album", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                user, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                forum, "My new album", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final User other = new User("MickFleetwood", "fleetwood@fw.com", "thunderOnlyHappens");
        final String token = "token";
        final int parentMessageId = 527;
        final int childMessageId = 528;
        final CreateCommentDtoRequest request = new CreateCommentDtoRequest("I prefer Fleetwood Mac");

        when(mockSessionDao.getUserByToken(anyString())).thenReturn(other);
        when(mockMessageDao.getMessageById(anyInt())).thenReturn(parentMessage);
        doAnswer(invocationOnMock -> {
            MessageItem item1 = invocationOnMock.getArgument(0);
            item1.setId(childMessageId);
            return item1;
        })
                .when(mockMessageDao)
                .saveMessageItem(any(MessageItem.class));

        final MessageDtoResponse response = messageService.addComment(token, parentMessageId, request);
        assertEquals(childMessageId, response.getId());
        assertEquals(MessageState.PUBLISHED, response.getState());

        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockMessageDao).getMessageById(anyInt());
        verify(mockMessageDao).saveMessageItem(any(MessageItem.class));
    }
}