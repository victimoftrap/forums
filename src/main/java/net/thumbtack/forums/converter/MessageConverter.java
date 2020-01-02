package net.thumbtack.forums.converter;

import net.thumbtack.forums.model.MessageTree;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.dto.responses.message.MessageInfoDtoResponse;
import net.thumbtack.forums.dto.responses.message.CommentInfoDtoResponse;

import java.util.List;

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
                message.getRatings().size(),
                commentsResponse
        );
    }
}
