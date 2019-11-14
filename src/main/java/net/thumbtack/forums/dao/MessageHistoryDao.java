package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.HistoryItem;

import java.util.List;

public interface MessageHistoryDao {
    Integer save(HistoryItem history);

    List<HistoryItem> getHistoryOfMessage(int messageId);

    void update(HistoryItem history);

    void delete(HistoryItem history);

    void deleteAll();
}
