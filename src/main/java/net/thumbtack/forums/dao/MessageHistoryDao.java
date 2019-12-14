package net.thumbtack.forums.dao;

import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.model.HistoryItem;
import net.thumbtack.forums.model.MessageItem;

public interface MessageHistoryDao {
    HistoryItem saveNewVersion(MessageItem item) throws ServerException;

    void editLatestVersion(MessageItem item) throws ServerException;

    void unpublishNewVersionBy(int messageId) throws ServerException;
}
