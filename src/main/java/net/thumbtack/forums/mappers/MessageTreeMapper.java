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
            "(forum_id, subject, priority)",
            "VALUES(#{forum.id}, #{subject}, #{priority.name})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer saveMessageTree(MessageTree tree);

    @Select("SELECT id, forum_id, subject, priority FROM messages_tree WHERE id = #{id}")
    @Results(id = "treeResult",
            value = {
                    @Result(property = "id", column = "id", javaType = int.class),
                    @Result(property = "forum", column = "forum_id", javaType = Forum.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.ForumMapper.getById",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "rootMessage", column = "id", javaType = MessageItem.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.MessageMapper.getRootMessageById",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "tags", column = "id", javaType = List.class,
                            many = @Many(
                                    select = "net.thumbtack.forums.mappers.TagMapper.getMessageTreeTags",
                                    fetchType = FetchType.LAZY
                            )
                    )
            }
    )
    MessageTree getTreeById(int id);

    @Update({"UPDATE messages_tree",
            "SET priority = #{priority.name}",
            "WHERE id = #{id}"
    })
    void updateMessagePriority(MessageTree tree);

    @Delete("DELETE FROM messages_tree WHERE id = #{id}")
    void deleteTreeById(int id);

    @Delete({"DELETE FROM messages_tree WHERE id = (",
            "SELECT tree_id FROM messages WHERE id = #{messageId} AND parent_message IS NULL",
            ")"
    })
    void deleteTreeByRootMessageId(int messageId);

    @Delete("DELETE FROM messages_tree")
    void deleteAll();
}
