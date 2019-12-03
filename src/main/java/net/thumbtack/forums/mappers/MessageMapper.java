package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.MessageItem;

import net.thumbtack.forums.model.MessageTree;
import org.apache.ibatis.annotations.*;

public interface MessageMapper {
    @Insert({"INSERT INTO messages",
            "(owner_id, parent_message, created_at, updated_at)",
            "VALUES(#{owner.id}, #{parentMessage.id}, #{createdAt}, #{updatedAt})"
    })
    @Options(useGeneratedKeys = true)
    Integer saveMessageItem(MessageItem item);

    @Update({"UPDATE message",
            "SET parent_message = NULL",
            "WHERE id = #{id}"
    })
    void deleteParentMessage(int id);

    @Delete("DELETE FROM message WHERE id = #{id}")
    void deleteById(int id);

    MessageTree getMessageTreeByMessage(int id);
}
