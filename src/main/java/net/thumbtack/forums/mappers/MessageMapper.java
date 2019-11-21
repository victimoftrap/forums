package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.User;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageMapper {
    @Insert("INSERT INTO messages (owner_id, parent_message, created_at, updated_at) " +
            "VALUES(#{owner}, #{parentMessage}, #{createdAt}, #{updatedAt})"
    )
    @Options(useGeneratedKeys = true)
    Integer save(MessageItem message);

    @Select("SELECT id, owner_id, parent_message, created_at, updated_at " +
            "FROM messages WHERE id = #{id}"
    )
    @Results({
            @Result(property = "owner", column = "owner_id", javaType = User.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.UserMapper.getById",
                            fetchType = FetchType.LAZY
                    )
            ),
            @Result(property = "parentMessage", column = "parent_message", javaType = MessageItem.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.MessageMapper.getById",
                            fetchType = FetchType.LAZY
                    )
            ),
            @Result(property = "history", column = "id", javaType = List.class,
                    many = @Many(
                            select = "net.thumbtack.forums.mappers.MessageHistoryMapper.getByMessageId"
                    )
            ),
            @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class),
            @Result(property = "updatedAt", column = "updated_at", javaType = LocalDateTime.class)
    })
    MessageItem getById(@Param("id") int id);

    @Update("UPDATE messages SET " +
            "parent_message = #{parentMessage}, " +
            "updated_at = #{updatedAt} " +
            "WHERE id = #{id}"
    )
    void update(MessageItem message);

    @Delete("DELETE FROM messages WHERE id = #{id}")
    void deleteById(@Param("id") int id);

    @Delete("DELETE FROM messages")
    void deleteAll();
}
