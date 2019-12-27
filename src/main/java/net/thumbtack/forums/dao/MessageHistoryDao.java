package net.thumbtack.forums.dao;

import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.model.HistoryItem;
import net.thumbtack.forums.model.MessageItem;

import java.util.List;

public interface MessageHistoryDao {
    HistoryItem saveNewVersion(MessageItem item) throws ServerException;

    List<HistoryItem> getMessageHistory(int messageId,
                                        boolean allVersions,
                                        boolean unpublished) throws ServerException;

    void editLatestVersion(MessageItem item) throws ServerException;

    void unpublishNewVersionBy(int messageId) throws ServerException;
}
