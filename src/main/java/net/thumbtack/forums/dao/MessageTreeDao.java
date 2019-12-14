package net.thumbtack.forums.dao;

import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.model.MessageTree;

public interface MessageTreeDao {
    MessageTree saveMessageTree(MessageTree tree) throws ServerException;

    MessageTree newBranch(MessageTree tree) throws ServerException;

    void changeBranchPriority(MessageTree tree) throws ServerException;

    void deleteTreeById(int id) throws ServerException;

    void deleteTreeByRootMessageId(int messageId) throws ServerException;
}
