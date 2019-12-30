package net.thumbtack.forums.service;

import net.thumbtack.forums.model.*;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.model.enums.MessageState;
import net.thumbtack.forums.model.enums.MessagePriority;
import net.thumbtack.forums.dao.*;
import net.thumbtack.forums.dto.requests.message.*;
import net.thumbtack.forums.dto.responses.message.MessageDtoResponse;
import net.thumbtack.forums.dto.responses.message.EditMessageOrCommentDtoResponse;
import net.thumbtack.forums.dto.responses.message.MadeBranchFromCommentDtoResponse;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MessageServiceTest {
    private SessionDao mockSessionDao;
    private ForumDao mockForumDao;
    private MessageTreeDao mockMessageTreeDao;
    private MessageDao mockMessageDao;
    private MessageHistoryDao mockMessageHistoryDao;
    private RatingDao mockRatingDao;
    private MessageService messageService;
    private ServerConfigurationProperties mockServerProperties;

    @BeforeEach
    void initMocks() {
        mockSessionDao = mock(SessionDao.class);
        mockForumDao = mock(ForumDao.class);
        mockMessageTreeDao = mock(MessageTreeDao.class);
        mockMessageDao = mock(MessageDao.class);
        mockMessageHistoryDao = mock(MessageHistoryDao.class);
        mockRatingDao = mock(RatingDao.class);
        mockServerProperties = mock(ServerConfigurationProperties.class);

        messageService = new MessageService(
                mockSessionDao, mockForumDao,
                mockMessageTreeDao, mockMessageDao, mockMessageHistoryDao,
                mockRatingDao, mockServerProperties
        );
    }

    @Test
    void testCreateMessage() throws ServerException {
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
    void testCreateMessage_creatorAreBanned_shouldThrowException() throws ServerException {
        final User bannedUser = new User(
                "BannedCreator", "BannedCreator@email.com", "passwordOfBannedUser"
        );
        bannedUser.setBannedUntil(LocalDateTime.now().plus(1, ChronoUnit.DAYS));

        final String token = "token";
        final int forumId = 1939;
        final CreateMessageDtoRequest request = new CreateMessageDtoRequest(
                "Message Subject", "Message Body",
                MessagePriority.HIGH, null
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(bannedUser);

        try {
            messageService.addMessage(token, forumId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.USER_BANNED, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockForumDao, never())
                .getById(anyInt());
        verify(mockMessageTreeDao, never())
                .saveMessageTree(any(MessageTree.class));
    }

    @Test
    void testCreateMessage_forumReadOnly_shouldThrowException() throws ServerException {
        final User user = new User("TestUser", "user@mail.com", "whatsGoingOn");
        final Forum forum = new Forum(
                ForumType.MODERATED, user, "ForumTitle",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        forum.setReadonly(true);

        final String token = "token";
        final int forumId = 1939;
        final CreateMessageDtoRequest request = new CreateMessageDtoRequest(
                "Subject", "New message body",
                MessagePriority.HIGH, null
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        when(mockForumDao.getById(anyInt()))
                .thenReturn(forum);

        try {
            messageService.addMessage(token, forumId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.FORUM_READ_ONLY, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockForumDao)
                .getById(anyInt());
        verify(mockMessageTreeDao, never())
                .saveMessageTree(any(MessageTree.class));
    }

    @Test
    void testCreateComment() throws ServerException {
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

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(other);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);
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

    @Test
    void testCreateComment_creatorAreBanned_shouldThrowException() throws ServerException {
        final User bannedUser = new User(
                "BannedCreator", "BannedCreator@email.com", "passwordOfBannedUser"
        );
        bannedUser.setBannedUntil(LocalDateTime.now().plus(1, ChronoUnit.DAYS));

        final String token = "token";
        final int parentMessageId = 1939;
        final CreateCommentDtoRequest request = new CreateCommentDtoRequest("Comment Body");

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(bannedUser);

        try {
            messageService.addComment(token, parentMessageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.USER_BANNED, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockMessageDao, never())
                .getMessageById(anyInt());
        verify(mockMessageDao, never())
                .saveMessageItem(any(MessageItem.class));
    }

    @Test
    void testCreateComment_parentMessageNotPublished_shouldThrowException() throws ServerException {
        final User user = new User("MarvinGaye", "gaye@motown.com", "whatsGoingOn");
        final Forum forum = new Forum(ForumType.UNMODERATED, user,
                "Soul Music", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "I made new album", MessageState.UNPUBLISHED,
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

        final String token = "token";
        final int parentMessageId = 527;
        final CreateCommentDtoRequest request = new CreateCommentDtoRequest("I prefer Fleetwood Mac");

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(user);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);

        try {
            messageService.addComment(token, parentMessageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.MESSAGE_NOT_PUBLISHED, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockMessageDao)
                .getMessageById(anyInt());
        verify(mockMessageDao, never())
                .saveMessageItem(any(MessageItem.class));
    }

    @Test
    void testCreateComment_forumReadOnly_shouldThrowException() throws ServerException {
        final User user = new User("User", "user@testmail.ca", "whatsGoingOn");
        final Forum readOnlyForum = new Forum(ForumType.UNMODERATED, user,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        readOnlyForum.setReadonly(true);
        final HistoryItem parentHistory = new HistoryItem(
                "Message Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                user, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                readOnlyForum, "Tree Subject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final User other = new User(
                "OtherUser", "other@minessota.online", "thunderOnlyHappens"
        );
        final String token = "token";
        final int parentMessageId = 527;
        final CreateCommentDtoRequest request = new CreateCommentDtoRequest("Comment Body");

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(other);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);

        try {
            messageService.addComment(token, parentMessageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.FORUM_READ_ONLY, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockMessageDao)
                .getMessageById(anyInt());
        verify(mockMessageTreeDao, never())
                .saveMessageTree(any(MessageTree.class));
    }

    @Test
    void testDeleteMessage_itsRootMessageTree_successfullyDeleted() throws ServerException {
        final int maxBanCount = 5;
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final Forum forum = new Forum(ForumType.UNMODERATED, messageOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final String token = "token";
        final int messageId = 951;

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(messageOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);
        doNothing()
                .when(mockMessageTreeDao)
                .deleteTreeById(anyInt());

        messageService.deleteMessage(token, messageId);

        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties)
                .getMaxBanCount();
        verify(mockMessageDao)
                .getMessageById(anyInt());
        verify(mockMessageTreeDao)
                .deleteTreeById(anyInt());
        verify(mockMessageDao, never())
                .deleteMessageById(anyInt());
    }

    @Test
    void testDeleteMessage_itsCommentForMessage_successfullyDeleted() throws ServerException {
        final int maxBanCount = 5;
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final Forum forum = new Forum(ForumType.UNMODERATED, messageOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", null, MessagePriority.NORMAL
        );

        final HistoryItem comment1History = new HistoryItem(
                "Comment 1 Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem comment1 = new MessageItem(
                messageOwner, tree, null,
                Collections.singletonList(comment1History),
                comment1History.getCreatedAt(), comment1History.getCreatedAt()
        );
        final List<MessageItem> comments = Arrays.asList(comment1);

        final HistoryItem parentHistory = new HistoryItem(
                "Parent Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, tree, null,
                comments, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt(), 0
        );
        comment1.setParentMessage(parentMessage);
        tree.setRootMessage(parentMessage);

        final String token = "token";
        final int messageId = 951;

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(messageOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(comment1);
        doNothing()
                .when(mockMessageDao)
                .deleteMessageById(anyInt());

        messageService.deleteMessage(token, messageId);

        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockServerProperties).getMaxBanCount();
        verify(mockMessageDao).getMessageById(anyInt());
        verify(mockMessageDao).deleteMessageById(anyInt());
        verify(mockMessageTreeDao, never()).deleteTreeById(anyInt());
    }

    @Test
    void testDeleteMessage_requestFromNotMessageCreator_shouldThrowException() throws ServerException {
        final int maxBanCount = 5;
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final Forum forum = new Forum(ForumType.UNMODERATED, messageOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final User requesterUser = new User(
                "SomeRandomUser", "SomeRandomUser@email.com", "whoIsIt?_Bruh"
        );

        final String token = "token";
        final int messageId = 951;

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(requesterUser);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);

        try {
            messageService.deleteMessage(token, messageId);
        } catch (ServerException se) {
            assertEquals(ErrorCode.FORBIDDEN_OPERATION, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties)
                .getMaxBanCount();
        verify(mockMessageDao)
                .getMessageById(anyInt());
        verify(mockMessageTreeDao, never())
                .deleteTreeById(anyInt());
        verify(mockMessageDao, never())
                .deleteMessageById(anyInt());
    }

    @Test
    void testDeleteRootMessage_messageHasComments_shouldThrowException() throws ServerException {
        final int maxBanCount = 5;
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final Forum forum = new Forum(ForumType.UNMODERATED, messageOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", null, MessagePriority.NORMAL
        );

        final HistoryItem comment1History = new HistoryItem(
                "Comment 1 Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem comment1 = new MessageItem(
                messageOwner, tree, null,
                Collections.singletonList(comment1History),
                comment1History.getCreatedAt(), comment1History.getCreatedAt()
        );
        final List<MessageItem> comments = Arrays.asList(comment1);

        final HistoryItem parentHistory = new HistoryItem(
                "Parent Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, tree, null,
                comments, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt(), 0
        );
        comment1.setParentMessage(parentMessage);
        tree.setRootMessage(parentMessage);

        final String token = "token";
        final int messageId = 951;

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(messageOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);

        try {
            messageService.deleteMessage(token, messageId);
        } catch (ServerException se) {
            assertEquals(ErrorCode.MESSAGE_NOT_DELETED, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties)
                .getMaxBanCount();
        verify(mockMessageDao)
                .getMessageById(anyInt());
        verify(mockMessageDao, never())
                .deleteMessageById(anyInt());
        verify(mockMessageTreeDao, never())
                .deleteTreeById(anyInt());
    }

    @Test
    void testDeleteMessage_userPermanentlyBanned_shouldThrowException() throws ServerException {
        final int maxBanCount = 5;
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        messageOwner.setBanCount(maxBanCount);

        final String token = "token";
        final int messageId = 951;

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(messageOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);

        try {
            messageService.deleteMessage(token, messageId);
        } catch (ServerException se) {
            assertEquals(ErrorCode.USER_PERMANENTLY_BANNED, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties)
                .getMaxBanCount();
        verify(mockMessageDao, never())
                .getMessageById(anyInt());
        verify(mockMessageTreeDao, never())
                .deleteTreeById(anyInt());
        verify(mockMessageDao, never())
                .deleteMessageById(anyInt());
    }

    @Test
    void testDeleteMessage_forumReadOnly_shouldThrowException() throws ServerException {
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final Forum readOnlyForum = new Forum(ForumType.UNMODERATED, messageOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        readOnlyForum.setReadonly(true);
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                readOnlyForum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final String token = "token";
        final int messageId = 951;

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(messageOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(5);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);

        try {
            messageService.deleteMessage(token, messageId);
        } catch (ServerException se) {
            assertEquals(ErrorCode.FORUM_READ_ONLY, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockMessageDao)
                .getMessageById(anyInt());
        verify(mockServerProperties)
                .getMaxBanCount();
        verify(mockMessageTreeDao, never())
                .deleteTreeById(anyInt());
        verify(mockMessageDao, never())
                .deleteMessageById(anyInt());
    }

    static Stream<Arguments> userAndForumParams() {
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        return Stream.of(
                Arguments.arguments(messageOwner, forumOwner, ForumType.UNMODERATED, MessageState.PUBLISHED),
                Arguments.arguments(messageOwner, forumOwner, ForumType.MODERATED, MessageState.UNPUBLISHED),
                Arguments.arguments(forumOwner, forumOwner, ForumType.UNMODERATED, MessageState.PUBLISHED),
                Arguments.arguments(forumOwner, forumOwner, ForumType.MODERATED, MessageState.PUBLISHED)
        );
    }

    @ParameterizedTest
    @MethodSource("userAndForumParams")
    void testEditPublishedMessage(User messageOwner, User forumOwner, ForumType forumType, MessageState expectedState)
            throws ServerException {
        final Forum forum = new Forum(
                forumType, forumOwner,
                "ForumName",
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final String token = "token";
        final int messageId = 123;
        final EditMessageOrCommentDtoRequest request = new EditMessageOrCommentDtoRequest(
                "New Root Message Body"
        );
        final HistoryItem newHistory = new HistoryItem(
                request.getBody(), expectedState,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(messageOwner);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);
        when(mockMessageHistoryDao.saveNewVersion(any(MessageItem.class)))
                .thenReturn(newHistory);

        final EditMessageOrCommentDtoResponse response = messageService.editMessage(
                token, messageId, request
        );
        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockMessageDao).getMessageById(anyInt());
        verify(mockMessageHistoryDao).saveNewVersion(any(MessageItem.class));
        verify(mockMessageHistoryDao, never()).editLatestVersion(any(MessageItem.class));
        assertEquals(expectedState, response.getState());
    }

    @Test
    @DisplayName("Unublished message from regular user in moderated forum = replace message body, unpublished")
    void testEditMessage_unpublishedMessageFromRegularUserInModeratedForum_shouldAndUnpublishedHistory()
            throws ServerException {
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final Forum forum = new Forum(
                ForumType.MODERATED, forumOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.UNPUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final String token = "token";
        final int messageId = 123;
        final EditMessageOrCommentDtoRequest request = new EditMessageOrCommentDtoRequest(
                "New Root Message Body"
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(messageOwner);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);
        doNothing()
                .when(mockMessageHistoryDao)
                .editLatestVersion(any(MessageItem.class));

        final EditMessageOrCommentDtoResponse response = messageService.editMessage(
                token, messageId, request
        );
        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockMessageDao).getMessageById(anyInt());
        verify(mockMessageHistoryDao, never()).saveNewVersion(any(MessageItem.class));
        verify(mockMessageHistoryDao).editLatestVersion(any(MessageItem.class));
        assertEquals(MessageState.UNPUBLISHED, response.getState());
    }

    @Test
    void testEditMessage_userNotMessageOwner_shouldThrowException() throws ServerException {
        final User otherUser = new User(
                "OtherUser", "OtherUser@email.com", "passwordOfBannedUser"
        );
        final User victimUser = new User(
                "VictimUser", "VictimUser@email.com", "passwordOfBannedUser"
        );
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        final Forum forum = new Forum(
                ForumType.MODERATED, forumOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.UNPUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                victimUser, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final String token = "token";
        final int forumId = 1939;
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(otherUser);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);

        try {
            messageService.editMessage(token, forumId, null);
        } catch (ServerException se) {
            assertEquals(ErrorCode.FORBIDDEN_OPERATION, se.getErrorCode());
        }
        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockMessageDao).getMessageById(anyInt());
        verify(mockMessageHistoryDao, never()).saveNewVersion(any(MessageItem.class));
        verify(mockMessageHistoryDao, never()).editLatestVersion(any(MessageItem.class));
    }

    @Test
    void testEditMessage_userBanned_shouldThrowException() throws ServerException {
        final User bannedUser = new User(
                "BannedCreator", "BannedCreator@email.com", "passwordOfBannedUser"
        );
        bannedUser.setBannedUntil(LocalDateTime.now().plus(1, ChronoUnit.DAYS));

        final String token = "token";
        final int forumId = 1939;
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(bannedUser);

        try {
            messageService.editMessage(token, forumId, null);
        } catch (ServerException se) {
            assertEquals(ErrorCode.USER_BANNED, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockMessageDao, never())
                .getMessageById(anyInt());
        verify(mockMessageHistoryDao, never())
                .saveNewVersion(any(MessageItem.class));
        verify(mockMessageHistoryDao, never())
                .editLatestVersion(any(MessageItem.class));
    }

    @Test
    void testChangePriority() throws ServerException {
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final Forum forum = new Forum(
                ForumType.UNMODERATED, forumOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final String token = "token";
        final int messageId = 123;
        final ChangeMessagePriorityDtoRequest request = new ChangeMessagePriorityDtoRequest(
                MessagePriority.HIGH
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(messageOwner);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);
        doAnswer(invocationOnMock -> {
            MessageTree aTree = invocationOnMock.getArgument(0);
            aTree.setPriority(request.getPriority());
            return aTree;
        })
                .when(mockMessageTreeDao)
                .changeBranchPriority(any(MessageTree.class));

        messageService.changeMessagePriority(token, messageId, request);

        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockMessageDao).getMessageById(anyInt());
        verify(mockMessageTreeDao).changeBranchPriority(any(MessageTree.class));
    }

    @Test
    void testChangePriority_userNotMessageOwner_shouldThrowException() throws ServerException {
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final User strangeGuy = new User(
                "strangeGuy", "strangeGuy@email.com", "v3ryStr0ngPa55"
        );
        final Forum forum = new Forum(
                ForumType.UNMODERATED, forumOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final String token = "token";
        final int messageId = 123;
        final ChangeMessagePriorityDtoRequest request = new ChangeMessagePriorityDtoRequest(
                MessagePriority.HIGH
        );
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(strangeGuy);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);

        try {
            messageService.changeMessagePriority(token, messageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.FORBIDDEN_OPERATION, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockMessageDao)
                .getMessageById(anyInt());
        verify(mockMessageTreeDao, never())
                .changeBranchPriority(any(MessageTree.class));
    }

    @Test
    void testChangePriority_userBanned_shouldThrowException() throws ServerException {
        final User bannedUser = new User(
                "BannedCreator", "BannedCreator@email.com", "passwordOfBannedUser"
        );
        bannedUser.setBannedUntil(LocalDateTime.now().plus(1, ChronoUnit.DAYS));

        final String token = "token";
        final int messageId = 123;
        final ChangeMessagePriorityDtoRequest request = new ChangeMessagePriorityDtoRequest(
                MessagePriority.HIGH
        );
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(bannedUser);

        try {
            messageService.changeMessagePriority(token, messageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.USER_BANNED, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockMessageDao, never())
                .getMessageById(anyInt());
        verify(mockMessageTreeDao, never())
                .changeBranchPriority(any(MessageTree.class));
    }

    @Test
    void testNewBranchFromComment() throws ServerException {
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final User commentOwner = new User(
                "CommentOwner", "CommentOwner@email.com", "c0Mm3nTatrPa55"
        );
        final Forum forum = new Forum(
                ForumType.UNMODERATED, forumOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final HistoryItem commentHistory = new HistoryItem(
                "Comment Message Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem commentMessage = new MessageItem(
                commentOwner, tree, null,
                Collections.singletonList(parentHistory),
                commentHistory.getCreatedAt(), commentHistory.getCreatedAt()
        );

        final String token = "token";
        final int messageId = 123;
        final MadeBranchFromCommentDtoRequest request = new MadeBranchFromCommentDtoRequest(
                "NewTreeSubject", MessagePriority.NORMAL, null
        );
        final int newBranchId = 1984;

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(forumOwner);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(commentMessage);
        doAnswer(invocationOnMock -> {
            MessageTree aMessageTree = invocationOnMock.getArgument(0);
            aMessageTree.setId(newBranchId);
            return aMessageTree;
        })
                .when(mockMessageTreeDao)
                .newBranch(any(MessageTree.class));

        final MadeBranchFromCommentDtoResponse response =
                messageService.newBranchFromComment(token, messageId, request);

        assertEquals(messageId, response.getId());
        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockMessageDao).getMessageById(anyInt());
        verify(mockMessageTreeDao).newBranch(any(MessageTree.class));
    }

    @Test
    void testNewBranchFromComment_requestNotFromForumOwner_shouldThrowException() throws ServerException {
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final User commentOwner = new User(
                "CommentOwner", "CommentOwner@email.com", "c0Mm3nTatrPa55"
        );
        final Forum forum = new Forum(
                ForumType.UNMODERATED, forumOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final HistoryItem commentHistory = new HistoryItem(
                "Comment Message Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem commentMessage = new MessageItem(
                commentOwner, tree, null,
                Collections.singletonList(parentHistory),
                commentHistory.getCreatedAt(), commentHistory.getCreatedAt()
        );

        final String token = "token";
        final int messageId = 123;
        final MadeBranchFromCommentDtoRequest request = new MadeBranchFromCommentDtoRequest(
                "NewTreeSubject", MessagePriority.NORMAL, null
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(commentOwner);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(commentMessage);

        try {
            final MadeBranchFromCommentDtoResponse response =
                    messageService.newBranchFromComment(token, messageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.FORBIDDEN_OPERATION, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockMessageDao)
                .getMessageById(anyInt());
        verify(mockMessageTreeDao, never())
                .newBranch(any(MessageTree.class));
    }

    @Test
    void testNewBranchFromComment_forumOwnerBanned_shouldThrowException() throws ServerException {
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        forumOwner.setBannedUntil(LocalDateTime.now().plus(1, ChronoUnit.DAYS));

        final String token = "token";
        final int messageId = 123;
        final MadeBranchFromCommentDtoRequest request = new MadeBranchFromCommentDtoRequest(
                "NewTreeSubject", MessagePriority.NORMAL, null
        );
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(forumOwner);

        try {
            messageService.newBranchFromComment(token, messageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.USER_BANNED, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockMessageDao, never())
                .getMessageById(anyInt());
        verify(mockMessageTreeDao, never())
                .newBranch(any(MessageTree.class));
    }

    @Test
    void testNewBranchFromComment_forumReadOnly_shouldThrowException() throws ServerException {
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final User commentOwner = new User(
                "CommentOwner", "CommentOwner@email.com", "c0Mm3nTatrPa55"
        );
        final Forum readOnlyForum = new Forum(
                ForumType.MODERATED, forumOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        readOnlyForum.setReadonly(true);
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                readOnlyForum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final HistoryItem commentHistory = new HistoryItem(
                "Comment Message Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem commentMessage = new MessageItem(
                commentOwner, tree, null,
                Collections.singletonList(parentHistory),
                commentHistory.getCreatedAt(), commentHistory.getCreatedAt()
        );

        final String token = "token";
        final int messageId = 123;
        final MadeBranchFromCommentDtoRequest request = new MadeBranchFromCommentDtoRequest(
                "NewTreeSubject", MessagePriority.NORMAL, null
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(forumOwner);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(commentMessage);

        try {
            messageService.newBranchFromComment(token, messageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.FORUM_READ_ONLY, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockMessageDao)
                .getMessageById(anyInt());
        verify(mockMessageTreeDao, never())
                .newBranch(any(MessageTree.class));
    }

    @Test
    void testNewBranchFromComment_messageNotFound_shouldThrowException() throws ServerException {
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );

        final String token = "token";
        final int messageId = 123;
        final MadeBranchFromCommentDtoRequest request = new MadeBranchFromCommentDtoRequest(
                "NewTreeSubject", MessagePriority.NORMAL, null
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(forumOwner);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(null);

        try {
            messageService.newBranchFromComment(token, messageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.MESSAGE_NOT_FOUND, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockMessageDao)
                .getMessageById(anyInt());
        verify(mockMessageTreeDao, never())
                .newBranch(any(MessageTree.class));
    }

    @Test
    void testNewBranchFromComment_userNotFound_shouldThrowException() throws ServerException {
        final String token = "token";
        final int messageId = 123;
        final MadeBranchFromCommentDtoRequest request = new MadeBranchFromCommentDtoRequest(
                "NewTreeSubject", MessagePriority.NORMAL, null
        );
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(null);

        try {
            messageService.newBranchFromComment(token, messageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.WRONG_SESSION_TOKEN, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockMessageDao, never())
                .getMessageById(anyInt());
        verify(mockMessageTreeDao, never())
                .newBranch(any(MessageTree.class));
    }

    @Test
    void testPublishMessage() throws ServerException {
        final int maxBanCount = 5;
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final Forum forum = new Forum(
                ForumType.MODERATED, forumOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.UNPUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final String token = "token";
        final int messageId = 123;
        final PublicationDecisionDtoRequest request = new PublicationDecisionDtoRequest(
                PublicationDecision.YES
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(forumOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);
        doAnswer(invocationOnMock -> {
            MessageItem aItem = invocationOnMock.getArgument(0);
            aItem.getHistory().get(0).setState(MessageState.PUBLISHED);
            return aItem;
        })
                .when(mockMessageDao)
                .publish(any(MessageItem.class));

        messageService.publish(token, messageId, request);
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties)
                .getMaxBanCount();
        verify(mockMessageDao)
                .getMessageById(anyInt());
        verify(mockMessageDao)
                .publish(any(MessageItem.class));

        verify(mockMessageTreeDao, never())
                .deleteTreeById(anyInt());
        verify(mockMessageDao, never())
                .deleteMessageById(anyInt());
        verify(mockMessageHistoryDao, never())
                .unpublishNewVersionBy(anyInt());
    }

    @Test
    void testUnpublishMessage_itsARootMessage_successfullyUnpublished() throws ServerException {
        final int maxBanCount = 5;
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final Forum forum = new Forum(
                ForumType.MODERATED, forumOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.UNPUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final String token = "token";
        final int messageId = 123;
        final PublicationDecisionDtoRequest request = new PublicationDecisionDtoRequest(
                PublicationDecision.NO
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(forumOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);

        messageService.publish(token, messageId, request);

        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties)
                .getMaxBanCount();
        verify(mockMessageDao)
                .getMessageById(anyInt());
        verify(mockMessageTreeDao)
                .deleteTreeById(anyInt());

        verify(mockMessageDao, never())
                .publish(any(MessageItem.class));
        verify(mockMessageDao, never())
                .deleteMessageById(anyInt());
        verify(mockMessageHistoryDao, never())
                .unpublishNewVersionBy(anyInt());
    }

    @Test
    void testUnpublishMessage_itsAComment_successfullyUnpublished() throws ServerException {
        final int maxBanCount = 5;
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final Forum forum = new Forum(
                ForumType.MODERATED, forumOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.UNPUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final HistoryItem commentHistory = new HistoryItem(
                "Comment Message Body", MessageState.UNPUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem commentMessage = new MessageItem(
                messageOwner, tree, parentMessage,
                Collections.singletonList(commentHistory),
                commentHistory.getCreatedAt(), commentHistory.getCreatedAt()
        );

        final String token = "token";
        final int messageId = 123;
        final PublicationDecisionDtoRequest request = new PublicationDecisionDtoRequest(
                PublicationDecision.NO
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(forumOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(commentMessage);

        messageService.publish(token, messageId, request);
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties)
                .getMaxBanCount();
        verify(mockMessageDao)
                .getMessageById(anyInt());
        verify(mockMessageDao)
                .deleteMessageById(anyInt());

        verify(mockMessageDao, never())
                .publish(any(MessageItem.class));
        verify(mockMessageTreeDao, never())
                .deleteTreeById(anyInt());
        verify(mockMessageHistoryDao, never())
                .unpublishNewVersionBy(anyInt());
    }

    @Test
    void testUnpublishMessage_itsAnotherHistory_successfullyUnpublished() throws ServerException {
        final int maxBanCount = 5;
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final Forum forum = new Forum(
                ForumType.MODERATED, forumOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );

        final HistoryItem multiHistory1 = new HistoryItem(
                "History #1 Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem multiHistory2 = new HistoryItem(
                "History #2 Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem multiHistory3 = new HistoryItem(
                "History #3 Body", MessageState.UNPUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, null, null,
                Arrays.asList(multiHistory3, multiHistory2, multiHistory1),
                multiHistory3.getCreatedAt(), multiHistory3.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final String token = "token";
        final int messageId = 123;
        final PublicationDecisionDtoRequest request = new PublicationDecisionDtoRequest(
                PublicationDecision.NO
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(forumOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);

        messageService.publish(token, messageId, request);
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties)
                .getMaxBanCount();
        verify(mockMessageDao)
                .getMessageById(anyInt());
        verify(mockMessageHistoryDao)
                .unpublishNewVersionBy(anyInt());

        verify(mockMessageDao, never())
                .publish(any(MessageItem.class));
        verify(mockMessageDao, never())
                .deleteMessageById(anyInt());
        verify(mockMessageTreeDao, never())
                .deleteTreeById(anyInt());
    }

    @Test
    void testPublishMessage_requestNotFromForumOwner_shouldThrowException() throws ServerException {
        final int maxBanCount = 5;
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final Forum forum = new Forum(
                ForumType.MODERATED, forumOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.UNPUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final String token = "token";
        final int messageId = 123;
        final PublicationDecisionDtoRequest request = new PublicationDecisionDtoRequest(
                PublicationDecision.YES
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(messageOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);

        try {
            messageService.publish(token, messageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.FORBIDDEN_OPERATION, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties)
                .getMaxBanCount();
        verify(mockMessageDao)
                .getMessageById(anyInt());

        verify(mockMessageDao, never())
                .publish(any(MessageItem.class));
        verify(mockMessageTreeDao, never())
                .deleteTreeById(anyInt());
        verify(mockMessageDao, never())
                .deleteMessageById(anyInt());
        verify(mockMessageHistoryDao, never())
                .unpublishNewVersionBy(anyInt());
    }

    @Test
    void testPublishMessage_messageAlreadyPublished_shouldThrowException() throws ServerException {
        final int maxBanCount = 5;
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final Forum forum = new Forum(
                ForumType.MODERATED, forumOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.PUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final String token = "token";
        final int messageId = 123;
        final PublicationDecisionDtoRequest request = new PublicationDecisionDtoRequest(
                PublicationDecision.YES
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(forumOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);

        try {
            messageService.publish(token, messageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.MESSAGE_ALREADY_PUBLISHED, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties)
                .getMaxBanCount();
        verify(mockMessageDao)
                .getMessageById(anyInt());

        verify(mockMessageDao, never())
                .publish(any(MessageItem.class));
        verify(mockMessageTreeDao, never())
                .deleteTreeById(anyInt());
        verify(mockMessageDao, never())
                .deleteMessageById(anyInt());
        verify(mockMessageHistoryDao, never())
                .unpublishNewVersionBy(anyInt());
    }

    @Test
    void testPublishMessage_userNotFound_shouldThrowException() throws ServerException {
        final String token = "token";
        final int messageId = 123;
        final PublicationDecisionDtoRequest request = new PublicationDecisionDtoRequest(
                PublicationDecision.YES
        );
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(null);

        try {
            messageService.publish(token, messageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.WRONG_SESSION_TOKEN, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());

        verify(mockServerProperties, never())
                .getMaxBanCount();
        verify(mockMessageDao, never())
                .getMessageById(anyInt());
        verify(mockMessageDao, never())
                .publish(any(MessageItem.class));
        verify(mockMessageTreeDao, never())
                .deleteTreeById(anyInt());
        verify(mockMessageDao, never())
                .deleteMessageById(anyInt());
        verify(mockMessageHistoryDao, never())
                .unpublishNewVersionBy(anyInt());
    }

    @Test
    void testPublishMessage_messageNotFound_shouldThrowException() throws ServerException {
        final int maxBanCount = 5;
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );

        final String token = "token";
        final int messageId = 123;
        final PublicationDecisionDtoRequest request = new PublicationDecisionDtoRequest(
                PublicationDecision.YES
        );
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(forumOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(null);

        try {
            messageService.publish(token, messageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.MESSAGE_NOT_FOUND, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties)
                .getMaxBanCount();
        verify(mockMessageDao)
                .getMessageById(anyInt());

        verify(mockMessageDao, never())
                .publish(any(MessageItem.class));
        verify(mockMessageTreeDao, never())
                .deleteTreeById(anyInt());
        verify(mockMessageDao, never())
                .deleteMessageById(anyInt());
        verify(mockMessageHistoryDao, never())
                .unpublishNewVersionBy(anyInt());
    }

    @Test
    void testPublishMessage_userPermanentlyBanned_shouldThrowException() throws ServerException {
        final int maxBanCount = 5;
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        forumOwner.setBanCount(maxBanCount);

        final String token = "token";
        final int messageId = 123;
        final PublicationDecisionDtoRequest request = new PublicationDecisionDtoRequest(
                PublicationDecision.YES
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(forumOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);

        try {
            messageService.publish(token, messageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.USER_PERMANENTLY_BANNED, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties)
                .getMaxBanCount();

        verify(mockMessageDao, never())
                .getMessageById(anyInt());
        verify(mockMessageDao, never())
                .publish(any(MessageItem.class));
        verify(mockMessageTreeDao, never())
                .deleteTreeById(anyInt());
        verify(mockMessageDao, never())
                .deleteMessageById(anyInt());
        verify(mockMessageHistoryDao, never())
                .unpublishNewVersionBy(anyInt());
    }

    @Test
    void testRateMessage() throws ServerException {
        final int maxBanCount = 5;
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final Forum forum = new Forum(
                ForumType.MODERATED, forumOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.UNPUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final String token = "token";
        final int messageId = 123;
        final RateMessageDtoRequest request = new RateMessageDtoRequest(5);

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(forumOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);
        doAnswer(invocationOnMock -> {
            MessageItem aMessage = invocationOnMock.getArgument(0);
            aMessage.setAverageRating(request.getValue());
            return invocationOnMock;
        })
                .when(mockRatingDao)
                .upsertRating(any(MessageItem.class), any(User.class), anyInt());

        messageService.rate(token, messageId, request);

        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties)
                .getMaxBanCount();
        verify(mockRatingDao)
                .upsertRating(any(MessageItem.class), any(User.class), anyInt());
        verify(mockRatingDao, never())
                .deleteRate(any(MessageItem.class), any(User.class));
    }

    @Test
    void testUpdateRating() throws ServerException {
        final int maxBanCount = 5;
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final Forum forum = new Forum(
                ForumType.MODERATED, forumOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.UNPUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final int oldRate = 5;
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        parentMessage.setAverageRating(oldRate);
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final String token = "token";
        final int messageId = 123;
        final RateMessageDtoRequest request = new RateMessageDtoRequest(3);

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(forumOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);
        doAnswer(invocationOnMock -> {
            MessageItem aMessage = invocationOnMock.getArgument(0);
            aMessage.setAverageRating(request.getValue());
            return invocationOnMock;
        })
                .when(mockRatingDao)
                .upsertRating(any(MessageItem.class), any(User.class), anyInt());

        messageService.rate(token, messageId, request);

        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties)
                .getMaxBanCount();
        verify(mockRatingDao)
                .upsertRating(any(MessageItem.class), any(User.class), anyInt());
        verify(mockRatingDao, never())
                .deleteRate(any(MessageItem.class), any(User.class));
    }

    @Test
    void testRemoveRating() throws ServerException {
        final int maxBanCount = 5;
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        final User messageOwner = new User(
                "MessageOwner", "MessageOwner@email.com", "v3ryStr0ngPa55"
        );
        final Forum forum = new Forum(
                ForumType.MODERATED, forumOwner,
                "ForumName", LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final HistoryItem parentHistory = new HistoryItem(
                "Root Message Body", MessageState.UNPUBLISHED,
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        final int oldRate = 5;
        final MessageItem parentMessage = new MessageItem(
                messageOwner, Collections.singletonList(parentHistory),
                parentHistory.getCreatedAt(), parentHistory.getCreatedAt()
        );
        parentMessage.setAverageRating(oldRate);
        final MessageTree tree = new MessageTree(
                forum, "TreeSubject", parentMessage, MessagePriority.NORMAL
        );
        parentMessage.setMessageTree(tree);

        final String token = "token";
        final int messageId = 123;
        final RateMessageDtoRequest request = new RateMessageDtoRequest(null);

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(forumOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(parentMessage);
        doAnswer(invocationOnMock -> {
            MessageItem aMessage = invocationOnMock.getArgument(0);
            aMessage.setAverageRating(0);
            return invocationOnMock;
        })
                .when(mockRatingDao)
                .deleteRate(any(MessageItem.class), any(User.class));

        messageService.rate(token, messageId, request);

        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties)
                .getMaxBanCount();
        verify(mockRatingDao)
                .deleteRate(any(MessageItem.class), any(User.class));
        verify(mockRatingDao, never())
                .upsertRating(any(MessageItem.class), any(User.class), anyInt());
    }

    @Test
    void testRateMessage_userNotFound_shouldThrowException() throws ServerException {
        final String token = "token";
        final int messageId = 123;
        final RateMessageDtoRequest request = new RateMessageDtoRequest(5);
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(null);

        try {
            messageService.rate(token, messageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.WRONG_SESSION_TOKEN, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties, never())
                .getMaxBanCount();
        verify(mockMessageDao, never())
                .getMessageById(anyInt());
        verify(mockRatingDao, never())
                .upsertRating(any(MessageItem.class), any(User.class), anyInt());
        verify(mockRatingDao, never())
                .deleteRate(any(MessageItem.class), any(User.class));
    }

    @Test
    void testRateMessage_userPermanentlyBanned_shouldThrowException() throws ServerException {
        final int maxBanCount = 5;
        final User bannedForumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );
        bannedForumOwner.setBanCount(maxBanCount);

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(bannedForumOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);

        final String token = "token";
        final int messageId = 123;
        final RateMessageDtoRequest request = new RateMessageDtoRequest(5);

        try {
            messageService.rate(token, messageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.USER_PERMANENTLY_BANNED, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties)
                .getMaxBanCount();
        verify(mockRatingDao, never())
                .upsertRating(any(MessageItem.class), any(User.class), anyInt());
        verify(mockRatingDao, never())
                .deleteRate(any(MessageItem.class), any(User.class));
    }

    @Test
    void testRateMessage_messageNotFound_shouldThrowException() throws ServerException {
        final int maxBanCount = 5;
        final User forumOwner = new User(
                "ForumOwner", "ForumOwner@email.com", "f0rUmS|r0nGPa55"
        );

        final String token = "token";
        final int messageId = 123;
        final RateMessageDtoRequest request = new RateMessageDtoRequest(5);
        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(forumOwner);
        when(mockServerProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockMessageDao.getMessageById(anyInt()))
                .thenReturn(null);

        try {
            messageService.rate(token, messageId, request);
        } catch (ServerException se) {
            assertEquals(ErrorCode.MESSAGE_NOT_FOUND, se.getErrorCode());
        }
        verify(mockSessionDao)
                .getUserByToken(anyString());
        verify(mockServerProperties)
                .getMaxBanCount();
        verify(mockMessageDao)
                .getMessageById(anyInt());
        verify(mockRatingDao, never())
                .upsertRating(any(MessageItem.class), any(User.class), anyInt());
        verify(mockRatingDao, never())
                .deleteRate(any(MessageItem.class), any(User.class));
    }
}