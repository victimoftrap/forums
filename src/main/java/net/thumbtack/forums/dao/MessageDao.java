package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.MessageTree;

public interface MessageDao {
    MessageTree saveMessage(MessageTree tree);
}
