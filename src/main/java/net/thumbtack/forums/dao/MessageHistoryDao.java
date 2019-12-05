package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.HistoryItem;

public interface MessageHistoryDao {
    HistoryItem saveNewVersion(int messageId, HistoryItem history);

    void editLatestVersion(int messageId, HistoryItem history);

    void unpublishNewVersionBy(int messageId);
}
