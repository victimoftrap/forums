package net.thumbtack.forums.dao;

import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.model.MessageTree;
import net.thumbtack.forums.model.enums.MessageOrder;

import java.util.List;

public interface MessageTreeDao {
    MessageTree saveMessageTree(MessageTree tree) throws ServerException;

    MessageTree newBranch(MessageTree tree) throws ServerException;

    MessageTree getMessageTreeById(int id) throws ServerException;

    List<MessageTree> getTreesByForum(int forumId,
                                      MessageOrder order,
                                      int offset,
                                      int limit) throws ServerException;

    void changeBranchPriority(MessageTree tree) throws ServerException;

    void deleteTreeById(int id) throws ServerException;

    void deleteTreeByRootMessageId(int messageId) throws ServerException;
}
