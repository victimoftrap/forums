package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.HistoryItem;

public interface MessageHistoryDao {
    HistoryItem saveNewVersion(HistoryItem history);

    void editLatestVersion(HistoryItem history);

    void deleteHistoryById(int id);
}
