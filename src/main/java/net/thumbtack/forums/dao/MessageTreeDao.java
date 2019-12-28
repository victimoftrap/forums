package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.MessageTree;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.enums.MessageOrder;
import net.thumbtack.forums.exception.ServerException;

public interface MessageTreeDao {
    MessageTree saveMessageTree(MessageTree tree) throws ServerException;

    MessageTree newBranch(MessageTree tree) throws ServerException;

    MessageItem getTreeRootMessage(
            int messageId, MessageOrder order,
            boolean noComments, boolean allVersions, boolean unpublished) throws ServerException;

    void changeBranchPriority(MessageTree tree) throws ServerException;

    void deleteTreeById(int id) throws ServerException;

    void deleteTreeByRootMessageId(int messageId) throws ServerException;
}
