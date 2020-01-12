package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.MessageTree;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.MessageOrder;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageMapper {
    @Insert({"INSERT INTO messages",
            "(owner_id, tree_id, parent_message, created_at, updated_at)",
            "VALUES(#{owner.id}, #{messageTree.id}, #{parentMessage.id},",
            "#{createdAt}, #{updatedAt})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer saveMessageItem(MessageItem item);

    @Select({"SELECT id, owner_id, tree_id, parent_message, created_at, updated_at",
            "FROM messages WHERE id = #{id}"
    })
    @Results(id = "messageResult",
            value = {
                    @Result(property = "id", column = "id", javaType = int.class),
                    @Result(property = "owner", column = "owner_id", javaType = User.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.UserMapper.getById",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "messageTree", column = "tree_id", javaType = MessageTree.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.MessageTreeMapper.getTreeById",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "parentMessage", column = "parent_message", javaType = MessageItem.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.MessageMapper.getMessageById",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "childrenComments", column = "id", javaType = List.class,
                            many = @Many(
                                    select = "net.thumbtack.forums.mappers.MessageMapper.getChildrenMessages",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "history", column = "id", javaType = List.class,
                            many = @Many(
                                    select = "net.thumbtack.forums.mappers.MessageHistoryMapper.getMessageHistory",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class),
                    @Result(property = "updatedAt", column = "updated_at", javaType = LocalDateTime.class),
                    @Result(property = "averageRating", column = "id", javaType = double.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.RatingMapper.getMessageRating",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "rated", column = "id", javaType = int.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.RatingMapper.getMessageRatedCount",
                                    fetchType = FetchType.LAZY
                            )
                    )
            }
    )
    MessageItem getMessageById(int id);

    @Select({"SELECT id, owner_id, tree_id, parent_message, created_at, updated_at",
            "FROM messages WHERE tree_id = #{treeId} AND parent_message IS NULL"
    })
    @ResultMap("messageResult")
    MessageItem getRootMessageById(int treeId);

    @Select({"SELECT id, owner_id, tree_id, parent_message, created_at, updated_at",
            "FROM messages WHERE parent_message = #{id}",
            "ORDER BY created_at DESC"
    })
    @ResultMap("messageResult")
    List<MessageItem> getChildrenMessages(int id);

    @Update({"UPDATE messages",
            "SET tree_id = COALESCE(#{messageTree.id}, tree_id),",
            "SET parent_message = #{parentMessage.id}",
            "SET updated_at = COALESCE(#{updatedAt}, updated_at),",
            "WHERE id = #{id}"
    })
    void update(MessageItem item);

    @Update({"UPDATE messages",
            "SET tree_id = COALESCE(#{messageTree.id}, tree_id),",
            "parent_message = NULL",
            "WHERE id = #{id}"
    })
    void madeTreeRootMessage(MessageItem item);

    @Update({"UPDATE messages",
            "SET tree_id = COALESCE(#{messageTree.id}, tree_id),",
            "parent_message = #{messageTree.rootMessage.id}",
            "WHERE id = #{id}"
    })
    void moveMessageToBranch(MessageItem item);

    @Delete("DELETE FROM messages WHERE id = #{id}")
    void deleteById(int id);

    @Delete("DELETE FROM messages")
    void deleteAll();
}
