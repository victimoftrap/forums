package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.HistoryItem;
import net.thumbtack.forums.model.MessageItem;

public interface MessageHistoryDao {
    HistoryItem saveNewVersion(MessageItem item);

    void editLatestVersion(MessageItem item);

    void unpublishNewVersionBy(int messageId);
}
