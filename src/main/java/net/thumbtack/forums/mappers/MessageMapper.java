package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.MessageTree;
import net.thumbtack.forums.model.MessageItem;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Update;

public interface MessageMapper {
    @Insert({"INSERT INTO messages_tree",
            "(forum_id, root_message, subject, priority)",
            "VALUES(#{forum.id}, #{rootMessage.id}, #{subject}, #{priority})"
    })
    @Options(useGeneratedKeys = true)
    Integer saveMessageTree(MessageTree tree);

    @Insert({"INSERT INTO messages",
            "(owner_id, parent_message, created_at, updated_at)",
            "VALUES(#{owner.id}, NULL, #{createdAt}, #{updatedAt})"
    })
    @Options(useGeneratedKeys = true)
    Integer saveMessage(MessageItem item);

    @Insert({"INSERT INTO messages",
            "(owner_id, parent_message, created_at, updated_at)",
            "VALUES(#{owner.id}, #{parentMessage.id}, #{createdAt}, #{updatedAt})"
    })
    @Options(useGeneratedKeys = true)
    Integer saveComment(MessageItem item);

    @Update({"UPDATE message",
            "SET parent_message = NULL",
            "WHERE id = #{id}"
    })
    void madeNewBranch(int id);
}
