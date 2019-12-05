package net.thumbtack.forums.service;

import net.thumbtack.forums.converter.TagConverter;
import net.thumbtack.forums.dao.*;
import net.thumbtack.forums.dto.message.CreateCommentDtoRequest;
import net.thumbtack.forums.dto.message.CreateMessageDtoRequest;
import net.thumbtack.forums.dto.message.MessageDtoResponse;
import net.thumbtack.forums.model.*;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.model.enums.MessagePriority;
import net.thumbtack.forums.model.enums.MessageState;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;

@Service
public class MessageService {
    private final SessionDao sessionDao;
    private final ForumDao forumDao;
    private final MessageTreeDao messageTreeDao;
    private final MessageDao messageDao;
    private final MessageHistoryDao messageHistoryDao;

    @Autowired
    public MessageService(final SessionDao sessionDao,
                          final ForumDao forumDao,
                          final MessageTreeDao messageTreeDao,
                          final MessageDao messageDao,
                          final MessageHistoryDao messageHistoryDao) {
        this.sessionDao = sessionDao;
        this.forumDao = forumDao;
        this.messageTreeDao = messageTreeDao;
        this.messageDao = messageDao;
        this.messageHistoryDao = messageHistoryDao;
    }

    private User getUserBySessionOrThrowException(final String token) {
        final User user = sessionDao.getUserByToken(token);
        if (user == null) {
            throw new ServerException(ErrorCode.WRONG_SESSION_TOKEN);
        }
        return user;
    }

    private Forum getForumByIdOrThrowException(final int id) {
        final Forum forum = forumDao.getById(id);
        if (forum == null) {
            throw new ServerException(ErrorCode.FORUM_NOT_FOUND);
        }
        return forum;
    }

    private MessagePriority getMessagePriority(final MessagePriority priority) {
        if (priority == null) {
            return MessagePriority.NORMAL;
        }
        return priority;
    }

    private MessageState getMessageStateByForumType(final ForumType type) {
        if (type == ForumType.UNMODERATED) {
            return MessageState.PUBLISHED;
        }
        return MessageState.UNPUBLISHED;
    }

    private MessageItem getMessageOrThrowException(final int messageId) {
        final MessageItem item = messageDao.getMessageById(messageId);
        if (item == null) {
            throw new ServerException(ErrorCode.MESSAGE_NOT_FOUND);
        }
        return item;
    }

    public MessageDtoResponse addMessage(final String token,
                                         final int forumId,
                                         final CreateMessageDtoRequest request) {
        final User creator = getUserBySessionOrThrowException(token);
        final Forum forum = getForumByIdOrThrowException(forumId);

        final MessagePriority priority = getMessagePriority(request.getPriority());
        final MessageState state = getMessageStateByForumType(forum.getType());
        final LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        final HistoryItem historyItem = new HistoryItem(
                request.getBody(), state, createdAt
        );
        final MessageItem messageItem = new MessageItem(
                creator, Collections.singletonList(historyItem), createdAt, createdAt
        );
        final MessageTree tree = new MessageTree(
                forum, request.getSubject(), messageItem, priority,
                TagConverter.tagNamesToTagList(request.getTags())
        );
        messageItem.setMessageTree(tree);

        messageTreeDao.newMessageTree(tree);
        return new MessageDtoResponse(messageItem.getId(), state);
    }

    public MessageDtoResponse addComment(final String token,
                                         final int parentId,
                                         final CreateCommentDtoRequest request) {
        final User creator = getUserBySessionOrThrowException(token);
        final MessageItem parentMessage = getMessageOrThrowException(parentId);
        if (parentMessage.getHistory().get(0).getState() == MessageState.UNPUBLISHED) {
            throw new ServerException(ErrorCode.MESSAGE_NOT_PUBLISHED);
        }

        final Forum forum = parentMessage.getMessageTree().getForum();
        final MessageState state = getMessageStateByForumType(forum.getType());
        final LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        final HistoryItem historyItem = new HistoryItem(
                request.getBody(), state, createdAt
        );
        final MessageItem messageItem = new MessageItem(
                creator, Collections.singletonList(historyItem), createdAt, createdAt
        );

        messageDao.saveMessageItem(messageItem);
        return new MessageDtoResponse(messageItem.getId(), state);
    }
}
