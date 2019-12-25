package net.thumbtack.forums.service;

import net.thumbtack.forums.dao.*;
import net.thumbtack.forums.dto.responses.message.*;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.converter.TagConverter;
import net.thumbtack.forums.dto.requests.message.*;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class MessageService {
    private final SessionDao sessionDao;
    private final ForumDao forumDao;
    private final MessageTreeDao messageTreeDao;
    private final MessageDao messageDao;
    private final MessageHistoryDao messageHistoryDao;
    private final RatingDao ratingDao;

    @Autowired
    public MessageService(final SessionDao sessionDao,
                          final ForumDao forumDao,
                          final MessageTreeDao messageTreeDao,
                          final MessageDao messageDao,
                          final MessageHistoryDao messageHistoryDao,
                          final RatingDao ratingDao) {
        this.sessionDao = sessionDao;
        this.forumDao = forumDao;
        this.messageTreeDao = messageTreeDao;
        this.messageDao = messageDao;
        this.messageHistoryDao = messageHistoryDao;
        this.ratingDao = ratingDao;
    }

    private User getUserBySession(final String token) throws ServerException {
        final User user = sessionDao.getUserByToken(token);
        if (user == null) {
            throw new ServerException(ErrorCode.WRONG_SESSION_TOKEN);
        }
        return user;
    }

    private Forum getForumById(final int id) throws ServerException {
        final Forum forum = forumDao.getById(id);
        if (forum == null) {
            throw new ServerException(ErrorCode.FORUM_NOT_FOUND);
        }
        return forum;
    }

    private MessagePriority getMessagePriority(final MessagePriority priority) {
        return priority == null ? MessagePriority.NORMAL : priority;
    }

    private MessageState getMessageStateByForumType(final ForumType type) {
        return type == ForumType.UNMODERATED ? MessageState.PUBLISHED : MessageState.UNPUBLISHED;
    }

    private MessageItem getMessageById(final int messageId) throws ServerException {
        final MessageItem item = messageDao.getMessageById(messageId);
        if (item == null) {
            throw new ServerException(ErrorCode.MESSAGE_NOT_FOUND);
        }
        return item;
    }

    private void checkIsUserMessageCreator(final MessageItem item, final User user) throws ServerException {
        if (!item.getOwner().equals(user)) {
            throw new ServerException(ErrorCode.FORBIDDEN_OPERATION);
        }
    }

    private void checkUserBanned(final User user) throws ServerException {
        if (user.getBannedUntil() != null) {
            throw new ServerException(ErrorCode.USER_BANNED);
        }
    }

    public MessageDtoResponse addMessage(
            final String token,
            final int forumId,
            final CreateMessageDtoRequest request
    ) throws ServerException {
        final User creator = getUserBySession(token);
        checkUserBanned(creator);

        final Forum forum = getForumById(forumId);
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

        messageTreeDao.saveMessageTree(tree);
        return new MessageDtoResponse(messageItem.getId(), state);
    }

    public MessageDtoResponse addComment(
            final String token,
            final int parentId,
            final CreateCommentDtoRequest request
    ) throws ServerException {
        final User creator = getUserBySession(token);
        checkUserBanned(creator);

        final MessageItem parentMessage = getMessageById(parentId);
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

    public EmptyDtoResponse deleteMessage(
            final String token,
            final int messageId
    ) throws ServerException {
        final User requesterUser = getUserBySession(token);
        final MessageItem deletingMessage = getMessageById(messageId);
        checkIsUserMessageCreator(deletingMessage, requesterUser);
        // TODO check are user banned permanently

        if (!deletingMessage.getChildrenComments().isEmpty()) {
            throw new ServerException(ErrorCode.MESSAGE_NOT_DELETED);
        }

        if (deletingMessage.getParentMessage() == null) {
            messageTreeDao.deleteTreeById(deletingMessage.getMessageTree().getId());
        } else {
            messageDao.deleteMessageById(messageId);
        }
        return new EmptyDtoResponse();
    }

    public EditMessageOrCommentDtoResponse editMessage(
            final String token,
            final int messageId,
            final EditMessageOrCommentDtoRequest request
    ) throws ServerException {
        final User requesterUser = getUserBySession(token);
        final MessageItem editingMessage = getMessageById(messageId);
        checkIsUserMessageCreator(editingMessage, requesterUser);
        checkUserBanned(requesterUser);

        final Forum forum = editingMessage.getMessageTree().getForum();
        final ForumType type = forum.getType();
        final User forumOwner = forum.getOwner();
        final HistoryItem latestHistory = editingMessage.getHistory().get(0);

        MessageState messageState = MessageState.UNPUBLISHED;
        if (latestHistory.getState() == MessageState.PUBLISHED) {
            final List<HistoryItem> newHistory = new ArrayList<>(editingMessage.getHistory());
            if (type == ForumType.UNMODERATED || requesterUser.equals(forumOwner)) {
                messageState = MessageState.PUBLISHED;
            }
            if (type == ForumType.MODERATED && !requesterUser.equals(forumOwner)) {
                messageState = MessageState.UNPUBLISHED;
            }
            final HistoryItem newVersion = new HistoryItem(
                    request.getBody(), messageState, LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
            );
            newHistory.add(0, newVersion);
            editingMessage.setHistory(newHistory);
            messageHistoryDao.saveNewVersion(editingMessage);
        } else {
            messageState = MessageState.UNPUBLISHED;
            messageHistoryDao.editLatestVersion(editingMessage);
        }
        return new EditMessageOrCommentDtoResponse(messageState);
    }

    public EmptyDtoResponse changeMessagePriority(
            final String token,
            final int messageId,
            final ChangeMessagePriorityDtoRequest request
    ) throws ServerException {
        final User requesterUser = getUserBySession(token);
        final MessageItem editingMessage = getMessageById(messageId);
        checkIsUserMessageCreator(editingMessage, requesterUser);
        checkUserBanned(requesterUser);

        final MessageTree tree = editingMessage.getMessageTree();
        tree.setPriority(request.getPriority());
        messageTreeDao.changeBranchPriority(tree);
        return new EmptyDtoResponse();
    }

    public MadeBranchFromCommentDtoResponse newBranchFromComment(
            final String token,
            final int messageId,
            final MadeBranchFromCommentDtoRequest request
    ) throws ServerException {
        final User requesterUser = getUserBySession(token);
        final MessageItem newRootMessage = getMessageById(messageId);
        checkUserBanned(requesterUser);

        final MessageTree oldTree = newRootMessage.getMessageTree();
        final Forum forum = oldTree.getForum();
        if (!forum.getOwner().equals(requesterUser)) {
            throw new ServerException(ErrorCode.FORBIDDEN_OPERATION);
        }

        final MessageTree newTree = new MessageTree(
                oldTree.getForum(), request.getSubject(), newRootMessage,
                request.getPriority(), TagConverter.tagNamesToTagList(request.getTags())
        );

        messageTreeDao.newBranch(newTree);
        return new MadeBranchFromCommentDtoResponse(newTree.getId());
    }

    public EmptyDtoResponse publish(
            final String token,
            final int messageId,
            final PublicationDecisionDtoRequest request
    ) throws ServerException {
        final User requesterUser = getUserBySession(token);
        final MessageItem publishingMessage = getMessageById(messageId);

        final MessageTree tree = publishingMessage.getMessageTree();
        final User forumOwner = tree.getForum().getOwner();
        if (!requesterUser.equals(forumOwner)) {
            throw new ServerException(ErrorCode.FORBIDDEN_OPERATION);
        }
        // TODO check are user banned permanently

        final List<HistoryItem> messageHistory = publishingMessage.getHistory();
        final HistoryItem latestHistoryToPublish = messageHistory.get(0);
        if (latestHistoryToPublish.getState() == MessageState.PUBLISHED) {
            throw new ServerException(ErrorCode.MESSAGE_ALREADY_PUBLISHED);
        }

        if (request.getDecision() == PublicationDecision.YES) {
            latestHistoryToPublish.setState(MessageState.PUBLISHED);
            messageDao.publish(publishingMessage);
        }
        if (request.getDecision() == PublicationDecision.NO) {
            if (messageHistory.size() > 1) {
                messageHistoryDao.unpublishNewVersionBy(publishingMessage.getId());
            } else if (publishingMessage.getParentMessage() == null) {
                messageTreeDao.deleteTreeById(tree.getId());
            } else {
                messageDao.deleteMessageById(publishingMessage.getId());
            }
        }
        return new EmptyDtoResponse();
    }

    public EmptyDtoResponse rate(
            final String token,
            final int messageId,
            final RateMessageDtoRequest request
    ) throws ServerException {
        final User requesterUser = getUserBySession(token);
        final MessageItem ratedMessage = getMessageById(messageId);
        // TODO check are user banned permanently

        if (request.getValue() == null) {
            ratingDao.deleteRate(ratedMessage, requesterUser);
        } else {
            ratingDao.upsertRating(ratedMessage, requesterUser, request.getValue());
        }
        return new EmptyDtoResponse();
    }
}
