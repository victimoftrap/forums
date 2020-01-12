package net.thumbtack.forums.converter;

import net.thumbtack.forums.model.MessageTree;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.dto.responses.message.MessageInfoDtoResponse;
import net.thumbtack.forums.dto.responses.message.CommentInfoDtoResponse;
import net.thumbtack.forums.model.Rating;

import java.util.List;
import java.util.ArrayList;

public class MessageConverter {
    public static MessageInfoDtoResponse messageToResponse(final MessageItem message) {
        final MessageTree tree = message.getMessageTree();
        final String creatorName = message.getOwner().getUsername();
        final List<MessageItem> comments = message.getChildrenComments();

        final List<String> bodyResponse = HistoryConverter
                .historyToBodyHistoryList(message.getHistory());
        final List<String> tagsResponse = TagConverter
                .tagListToTagNamesList(tree.getTags());
        final List<CommentInfoDtoResponse> commentsResponse = CommentConverter
                .commentListToResponse(comments);

        return new MessageInfoDtoResponse(
                message.getId(),
                creatorName,
                tree.getSubject(),
                bodyResponse,
                tree.getPriority().name(),
                tagsResponse,
                message.getCreatedAt(),
                message.getAverageRating(),
                message.getRated(),
                commentsResponse
        );
    }

    public static List<MessageInfoDtoResponse> messageListToResponse(final List<MessageTree> messages) {
        final List<MessageInfoDtoResponse> messagesResponse = new ArrayList<>();

        for (final MessageTree tree : messages) {
            final MessageItem root = tree.getRootMessage();
            final String creator = root.getOwner().getUsername();
            final List<MessageItem> comments = root.getChildrenComments();

            final List<String> bodyResponse = HistoryConverter
                    .historyToBodyHistoryList(root.getHistory());
            final List<String> tagsResponse = TagConverter
                    .tagListToTagNamesList(tree.getTags());
            final List<CommentInfoDtoResponse> commentsResponse = CommentConverter
                    .commentListToResponse(comments);

            messagesResponse.add(
                    new MessageInfoDtoResponse(
                            root.getId(),
                            creator,
                            tree.getSubject(),
                            bodyResponse,
                            tree.getPriority().name(),
                            tagsResponse,
                            root.getCreatedAt(),
                            root.getAverageRating(),
                            root.getRated(),
                            commentsResponse
                    )
            );
        }
        return messagesResponse;
    }
}
