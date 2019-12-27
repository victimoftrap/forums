package net.thumbtack.forums.converter;

import net.thumbtack.forums.model.MessageTree;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.dto.responses.message.MessageInfoDtoResponse;

import java.util.List;

public class MessageConverter {
    public static MessageInfoDtoResponse messageToMessageInfoDto(final MessageTree messageTree) {
        final MessageItem messageItem = messageTree.getRootMessage();
        final List<String> history = HistoryConverter.historyToBodyHistoryList(messageItem.getHistory());
        final List<String> tags = TagConverter.tagListToTagNamesList(messageTree.getTags());

        return new MessageInfoDtoResponse(
                messageItem.getId(),
                messageItem.getOwner().getUsername(),
                messageTree.getSubject(),
                history,
                messageTree.getPriority().name(),
                tags,
                messageTree.getCreatedAt(),
                messageItem.getAverageRating(),
                messageItem.getRatings().size(),
                null
        );
    }
}
