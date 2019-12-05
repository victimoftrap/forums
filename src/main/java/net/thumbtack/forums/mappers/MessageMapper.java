package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.MessageTree;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.User;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageMapper {
    @Insert({"INSERT INTO messages",
            "(owner_id, parent_message, created_at, updated_at)",
            "VALUES(#{item.owner.id}, #{item.parentMessage.id}, #{item.createdAt}, #{item.updatedAt})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "item.id")
    Integer saveMessageItem(@Param("item") MessageItem item);

    @Select({"SELECT id, owner_id, parent_message, created_at, updated_at",
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
                    @Result(property = "messageTree", column = "id", javaType = MessageTree.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.MessageTreeMapper.getMessageTreeByMessage",
                                    fetchType = FetchType.LAZY
                            )
                    ),//
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
                    @Result(property = "rating", column = "id", javaType = int.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.RatingMapper.getMessageRating",
                                    fetchType = FetchType.LAZY
                            )
                    )
            }
    )
    MessageItem getMessageById(int id);

    @Select({"SELECT id, owner_id, parent_message, created_at, updated_at",
            "FROM messages WHERE id = #{id}"
    })
    @Results(id = "simpleMessageResult",
            value = {
                    @Result(property = "id", column = "id", javaType = int.class),
                    @Result(property = "owner", column = "owner_id", javaType = User.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.UserMapper.getById",
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
                    @Result(property = "rating", column = "id", javaType = int.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.RatingMapper.getMessageRating",
                                    fetchType = FetchType.LAZY
                            )
                    )
            }
    )
    MessageItem getSimpleMessageById(int id);

    @Select({"SELECT id, owner_id, parent_message, created_at, updated_at",
            "FROM messages WHERE parent_message = #{id}"
    })
    @ResultMap("messageResult")
//    @ResultMap("simpleMessageResult")
    List<MessageItem> getChildrenMessages(int id);

    @Update({"UPDATE messages",
            "SET parent_message = NULL",
            "WHERE id = #{id}"
    })
    void deleteParentMessage(int id);

    @Delete("DELETE FROM messages WHERE id = #{id}")
    void deleteById(int id);

    @Delete("DELETE FROM messages")
    void deleteAll();

    @Update({"UPDATE messages",
            "SET updated_at = COALESCE(#{updatedAt}, updated_at)",
            "WHERE id = #{id}"
    })
    void update(MessageItem item);
}
