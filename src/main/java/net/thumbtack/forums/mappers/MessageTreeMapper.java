package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.MessageTree;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;

public interface MessageTreeMapper {
    @Insert({"INSERT INTO messages_tree",
            "(forum_id, root_message, subject, priority)",
            "VALUES(#{forum.id}, #{rootMessage.id}, #{subject}, #{priority.name})"
    })
    @Options(useGeneratedKeys = true)
    Integer saveMessageTree(MessageTree tree);

    @Delete("DELETE FROM message_tree WHERE id = #{id}")
    void deleteMessageTreeById(int id);

    @Update({"UPDATE message_tree",
            "SET priority = #{priority.name}",
            "WHERE id = #{id}"
    })
    void updateMessagePriority(MessageTree tree);

    @Delete("DELETE FROM message_tree WHERE id = #{id}")
    void deleteTreeById(int id);
}
