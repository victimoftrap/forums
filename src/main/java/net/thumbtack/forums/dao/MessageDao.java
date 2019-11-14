package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.MessageItem;

public interface MessageDao {
    MessageItem saveMessage(MessageItem message);

    MessageItem madeNewBranch(MessageItem message);

    void update(MessageItem message);

    void deleteById(int id);

    void deleteAll();
}
