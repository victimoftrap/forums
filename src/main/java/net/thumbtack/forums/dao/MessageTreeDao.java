package net.thumbtack.forums.dao;

import net.thumbtack.forums.model.MessageTree;
import net.thumbtack.forums.model.Tag;

import java.util.List;

public interface MessageTreeDao {
    MessageTree saveMessageTree(MessageTree message);

    void changePriority(MessageTree message);

    MessageTree getMessage(int id, boolean allVersions, boolean noComments, boolean unpublished);

    List<MessageTree> getMessageList(
            int forumId, boolean allVersions, boolean noComments, boolean unpublished,
            List<Tag> tags, String order, int offset, int limit
    );

    void delete();

    void deleteAll();
}
