package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.MessageTree;

public interface MessageTreeDao {
    MessageTree newMessageTree(MessageTree tree);

    MessageTree newBranch(MessageTree tree);

    void changeBranchPriority(MessageTree tree);
}
