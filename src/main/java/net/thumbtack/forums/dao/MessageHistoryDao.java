package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.HistoryItem;

import java.util.List;

public interface MessageHistoryDao {
    HistoryItem save(HistoryItem history);

    List<HistoryItem> getMessageHistory(int messageId, boolean allVersions, boolean unpublished);

    void update(HistoryItem history);

    void delete(HistoryItem history);

    void deleteAll();
}
