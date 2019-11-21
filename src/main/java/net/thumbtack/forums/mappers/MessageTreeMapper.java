package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.MessageTree;
import org.apache.ibatis.annotations.*;

public interface MessageTreeMapper {
    @Insert("INSERT INTO messages_tree (forum_id, root_message, subject, priority) " +
            "VALUES(#{forum}, #{rootMessage}, #{subject}, #{priority})"
    )
    @Options(useGeneratedKeys = true)
    Integer save(MessageTree messageTree);

    @Update("UPDATE messages_tree SET " +
            "priority = COALESCE(#{priority}, priority) " +
            "WHERE id = #{id}"
    )
    void update(MessageTree messageTree);

    @Delete("DELETE FROM messages_tree WHERE id = #{id}")
    void deleteById(@Param("id") int id);

    @Delete("DELETE FROM messages_tree")
    void deleteAll();
}
