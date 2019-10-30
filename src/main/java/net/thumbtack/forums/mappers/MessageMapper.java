package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.*;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.sql.Timestamp;

public interface MessageMapper {
    @Insert(
            "INSERT INTO forum_messages (forum_id, owner_id, refer_to, " +
                    "state, priority, subject, body, rating, created_at, updated_at) " +
                    "VALUES(#{msg.forum.id}, #{msg.owner.id}, NULL, #{msg.state.name}, #{msg.priority.name}, " +
                    "#{msg.subject}, #{msg.body}, #{msg.rating}, " +
                    "#{msg.createdAt}, #{msg.updatedAt}" +
                    ")"
    )
    @Options(useGeneratedKeys = true, keyProperty = "msg.id")
    Integer save(@Param("msg") Message message);

    @Select(
            "SELECT id, forum_id, owner_id, state, priority, subject, body, rating, created_at, updated_at " +
                    "FROM forum_messages WHERE id = #{id}"
    )
    @Results({
            @Result(property = "id", column = "id", javaType = Integer.class),
            @Result(property = "forum", column = "forum_id", javaType = User.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.ForumMapper.findById",
                            fetchType = FetchType.LAZY
                    )
            ),
            @Result(property = "owner", column = "owner_id", javaType = Forum.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.UserMapper.findById",
                            fetchType = FetchType.LAZY
                    )
            ),
            @Result(property = "state", column = "state", javaType = MessageStates.class),
            @Result(property = "priority", column = "priority", javaType = MessagePriorities.class),
            @Result(property = "subject", column = "subject", javaType = String.class),
            @Result(property = "body", column = "body", javaType = String.class),
            @Result(property = "rating", column = "rating", javaType = Integer.class),
            @Result(property = "createdAt", column = "created_at", javaType = Timestamp.class),
            @Result(property = "updatedAt", column = "updated_at", javaType = Timestamp.class),
    })
    Message findById(@Param("id") Integer id);

    @Update(
            "UPDATE forum_messages SET state = #{upd.state.name}, priority = #{upd.priority.name}, " +
                    "subject = #{upd.subject}, body = #{upd.body}, " +
                    "rating = #{upd.rating}, updated_at = #{upd.updatedAt}"
    )
    void update(@Param("upd") Message message);

    @Delete("DELETE FROM forum_messages WHERE id = #{id}")
    void deleteById(@Param("id") Integer id);

    @Delete("DELETE FROM forum_messages")
    void deleteAll();
}
