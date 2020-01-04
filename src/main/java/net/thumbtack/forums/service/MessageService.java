package net.thumbtack.forums.service;

import net.thumbtack.forums.model.*;
import net.thumbtack.forums.model.enums.*;
import net.thumbtack.forums.dao.*;
import net.thumbtack.forums.dto.requests.message.*;
import net.thumbtack.forums.dto.responses.message.*;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.converter.TagConverter;
import net.thumbtack.forums.converter.MessageConverter;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service("messageService")
public class MessageService extends ServiceBase {
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
                          final RatingDao ratingDao,
                          final ServerConfigurationProperties serverProperties) {
        super(sessionDao, forumDao, serverProperties);
        this.messageTreeDao = messageTreeDao;
        this.messageDao = messageDao;
        this.messageHistoryDao = messageHistoryDao;
        this.ratingDao = ratingDao;
    }

    private MessagePriority getMessagePriority(final String priority) {
        return priority == null ? MessagePriority.NORMAL : MessagePriority.valueOf(priority);
    }

    private MessageState getMessageState(final Forum forum, final User messageCreator) {
        if (forum.getType() == ForumType.UNMODERATED) {
            return MessageState.PUBLISHED;
        }
        if (forum.getOwner().equals(messageCreator)) {
            return MessageState.PUBLISHED;
        }
        return MessageState.UNPUBLISHED;
    }

    private MessageItem getMessageById(final int messageId) throws ServerException {
        final MessageItem item = messageDao.getMessageById(messageId);
        if (item == null) {
            throw new ServerException(ErrorCode.MESSAGE_NOT_FOUND);
        }
        return item;
    }

    private void checkIsForumReadOnly(final Forum forum) throws ServerException {
        if (forum.isReadonly()) {
            throw new ServerException(ErrorCode.FORUM_READ_ONLY);
        }
    }

    private void checkPermission(final User owner, final User requesterUser) throws ServerException {
        if (!owner.equals(requesterUser)) {
            throw new ServerException(ErrorCode.FORBIDDEN_OPERATION);
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
        checkIsForumReadOnly(forum);

        final MessagePriority priority = getMessagePriority(request.getPriority());
        final MessageState state = getMessageState(forum, creator);
        final LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        final HistoryItem historyItem = new HistoryItem(
                request.getBody(), state, createdAt
        );
        final MessageItem messageItem = new MessageItem(
                creator, Collections.singletonList(historyItem), createdAt
        );
        final MessageTree tree = new MessageTree(
                forum, request.getSubject(), messageItem, priority, createdAt,
                TagConverter.tagNamesToTagList(request.getTags())
        );
        messageItem.setMessageTree(tree);

        messageTreeDao.saveMessageTree(tree);
        return new MessageDtoResponse(messageItem.getId(), state.name());
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
        checkIsForumReadOnly(forum);

        final MessageState state = getMessageState(forum, creator);
        final LocalDateTime createdAt = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);

        final HistoryItem historyItem = new HistoryItem(
                request.getBody(), state, createdAt
        );
        final MessageItem messageItem = new MessageItem(
                creator, Collections.singletonList(historyItem), createdAt
        );

        messageDao.saveMessageItem(messageItem);
        return new MessageDtoResponse(messageItem.getId(), state.name());
    }

    public EmptyDtoResponse deleteMessage(
            final String token,
            final int messageId
    ) throws ServerException {
        final User requesterUser = getUserBySession(token);
        checkUserBannedPermanently(requesterUser);

        final MessageItem deletingMessage = getMessageById(messageId);
        checkPermission(deletingMessage.getOwner(), requesterUser);

        final Forum forum = deletingMessage.getMessageTree().getForum();
        checkIsForumReadOnly(forum);

        if (!deletingMessage.getChildrenComments().isEmpty()) {
            throw new ServerException(ErrorCode.MESSAGE_HAS_COMMENTS);
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
        checkUserBanned(requesterUser);

        final MessageItem editingMessage = getMessageById(messageId);
        checkPermission(editingMessage.getOwner(), requesterUser);

        final Forum forum = editingMessage.getMessageTree().getForum();
        checkIsForumReadOnly(forum);

        final HistoryItem latestHistory = editingMessage.getHistory().get(0);
        MessageState messageState;
        if (latestHistory.getState() == MessageState.PUBLISHED) {
            final List<HistoryItem> newHistory = new ArrayList<>(editingMessage.getHistory());
            messageState = getMessageState(forum, requesterUser);

            final HistoryItem newVersion = new HistoryItem(
                    request.getBody(), messageState,
                    LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
            );
            newHistory.add(0, newVersion);
            editingMessage.setHistory(newHistory);
            messageHistoryDao.saveNewVersion(editingMessage);
        } else {
            messageState = MessageState.UNPUBLISHED;
            messageHistoryDao.editLatestVersion(editingMessage);
        }
        return new EditMessageOrCommentDtoResponse(messageState.name());
    }

    public EmptyDtoResponse changeMessagePriority(
            final String token,
            final int messageId,
            final ChangeMessagePriorityDtoRequest request
    ) throws ServerException {
        final User requesterUser = getUserBySession(token);
        checkUserBanned(requesterUser);

        final MessageItem editingMessage = getMessageById(messageId);
        checkPermission(editingMessage.getOwner(), requesterUser);

        final Forum forum = editingMessage.getMessageTree().getForum();
        checkIsForumReadOnly(forum);

        final MessageTree tree = editingMessage.getMessageTree();
        tree.setPriority(getMessagePriority(request.getPriority()));
        messageTreeDao.changeBranchPriority(tree);
        return new EmptyDtoResponse();
    }

    public MadeBranchFromCommentDtoResponse newBranchFromComment(
            final String token,
            final int messageId,
            final MadeBranchFromCommentDtoRequest request
    ) throws ServerException {
        final User requesterUser = getUserBySession(token);
        checkUserBanned(requesterUser);

        final MessageItem newRootMessage = getMessageById(messageId);
        if (newRootMessage.getParentMessage() == null) {
            throw new ServerException(ErrorCode.MESSAGE_ALREADY_BRANCH);
        }

        final MessageTree oldTree = newRootMessage.getMessageTree();
        final Forum forum = oldTree.getForum();
        checkIsForumReadOnly(forum);
        checkPermission(forum.getOwner(), requesterUser);

        final MessageTree newTree = new MessageTree(
                oldTree.getForum(), request.getSubject(), newRootMessage, request.getPriority(),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                TagConverter.tagNamesToTagList(request.getTags())
        );
        messageTreeDao.newBranch(newTree);
        return new MadeBranchFromCommentDtoResponse(messageId);
    }

    public EmptyDtoResponse publish(
            final String token,
            final int messageId,
            final PublicationDecisionDtoRequest request
    ) throws ServerException {
        final User requesterUser = getUserBySession(token);
        checkUserBannedPermanently(requesterUser);

        final MessageItem publishingMessage = getMessageById(messageId);
        final MessageTree tree = publishingMessage.getMessageTree();
        final Forum forum = tree.getForum();
        checkIsForumReadOnly(forum);
        checkPermission(forum.getOwner(), requesterUser);

        final List<HistoryItem> messageHistory = publishingMessage.getHistory();
        final HistoryItem latestHistoryToPublish = messageHistory.get(0);
        if (latestHistoryToPublish.getState() == MessageState.PUBLISHED) {
            throw new ServerException(ErrorCode.MESSAGE_ALREADY_PUBLISHED);
        }

        final PublicationDecision decision = PublicationDecision.valueOf(request.getDecision());
        if (decision == PublicationDecision.YES) {
            latestHistoryToPublish.setState(MessageState.PUBLISHED);
            messageDao.publish(publishingMessage);
        }
        if (decision == PublicationDecision.NO) {
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
        checkUserBannedPermanently(requesterUser);

        final MessageItem ratedMessage = getMessageById(messageId);
        if (request.getValue() == null) {
            ratingDao.deleteRate(ratedMessage, requesterUser);
        } else {
            ratingDao.upsertRating(ratedMessage, requesterUser, request.getValue());
        }
        return new EmptyDtoResponse();
    }

    public MessageInfoDtoResponse getMessage(
            final String token,
            final int messageId,
            final Boolean allVersions,
            final Boolean noComments,
            final Boolean unpublished,
            final String order
    ) throws ServerException {
        getUserBySession(token);

        final boolean realAllVersions = allVersions == null ? false : allVersions;
        final boolean realNoComments = noComments == null ? false : noComments;
        final boolean realUnpublished = unpublished == null ? false : unpublished;
        final MessageOrder realOrder = order == null ? MessageOrder.DESC : MessageOrder.valueOf(order);

        final MessageItem rootMessage = messageTreeDao.getTreeRootMessage(
                messageId, realOrder,
                realNoComments, realAllVersions, realUnpublished
        );
        if (rootMessage == null) {
            throw new ServerException(ErrorCode.MESSAGE_NOT_FOUND);
        }
        return MessageConverter.messageToResponse(rootMessage);
    }
}
