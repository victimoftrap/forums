package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.model.MessageTree;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.enums.MessagePriority;
import net.thumbtack.forums.daoimpl.provider.MessageTreeDaoProvider;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.util.List;

public interface MessageTreeMapper {
    @Insert({"INSERT INTO messages_tree",
            "(forum_id, root_message, subject, priority)",
            "VALUES(#{forum.id}, #{rootMessage.id}, #{subject}, #{priority.name})"
    })
    @Options(useGeneratedKeys = true)
    Integer saveMessageTree(MessageTree tree);

    @SelectProvider(method = "getMessageTreeByMessage", type = MessageTreeDaoProvider.class)
    @Results({
            @Result(property = "id", column = "id", javaType = int.class),
            @Result(property = "forum", column = "forum_id", javaType = Forum.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.ForumMapper.getById",
                            fetchType = FetchType.LAZY
                    )
            ),
            @Result(property = "rootMessage", column = "root_message", javaType = MessageItem.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.MessageMapper.getMessageById",
                            fetchType = FetchType.LAZY
                    )
            ),
            @Result(property = "subject", column = "subject", javaType = String.class),
            @Result(property = "priority", column = "priority", javaType = MessagePriority.class),
            @Result(property = "tags", column = "root_message", javaType = List.class,
                    many = @Many(
                            select = "net.thumbtack.forums.mappers.TagMapper.getMessageTags",
                            fetchType = FetchType.LAZY
                    )
            )
    })
    MessageTree getMessageTreeByMessage(int id);

    @Update({"UPDATE message_tree",
            "SET priority = #{priority.name}",
            "WHERE id = #{id}"
    })
    void updateMessagePriority(MessageTree tree);

    @Delete("DELETE FROM message_tree WHERE id = #{id}")
    void deleteTreeById(int id);

    @Delete("DELETE FROM message_tree")
    void deleteAll();
}
