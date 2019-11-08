package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.*;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.sql.Timestamp;
import java.util.List;

public interface CommentMapper {
    @Insert(
            "INSERT INTO forum_messages (forum_id, owner_id, refer_to, " +
                    "state, priority, subject, body, rating, created_at, updated_at) " +
                    "VALUES(#{com.forum.id}, #{com.owner.id}, #{com.referredMessage.id}, " +
                    "#{com.state.name}, NULL, NULL, #{com.body}, #{com.rating}, " +
                    "#{com.createdAt}, #{com.updatedAt}" +
                    ")"
    )
    @Options(useGeneratedKeys = true, keyProperty = "com.id")
    Integer save(@Param("com") Comment comment);

    @Select(
            "SELECT id, forum_id, owner_id, refer_to, state, body, rating, created_at, updated_at " +
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
            @Result(property = "referredMessage", column = "refer_to", javaType = Message.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.MessageMapper.findById",
                            fetchType = FetchType.LAZY
                    )
            ),
            @Result(property = "state", column = "state", javaType = String.class),
            @Result(property = "body", column = "body", javaType = String.class),
            @Result(property = "rating", column = "rating", javaType = Integer.class),
            @Result(property = "createdAt", column = "created_at", javaType = Timestamp.class),
            @Result(property = "updatedAt", column = "updated_at", javaType = Timestamp.class),
    })
    Comment findById(@Param("id") Integer id);

    @Select(
            "SELECT id, forum_id, owner_id, refer_to, state, body, rating, created_at, updated_at " +
                    "FROM forum_messages WHERE refer_to = #{id}"
    )
    @Results({
            @Result(property = "id", column = "id", javaType = Integer.class),
            @Result(property = "owner", column = "owner_id", javaType = Forum.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.UserMapper.findById",
                            fetchType = FetchType.LAZY
                    )
            ),
            @Result(property = "state", column = "state", javaType = String.class),
            @Result(property = "body", column = "body", javaType = String.class),
            @Result(property = "rating", column = "rating", javaType = Integer.class),
            @Result(property = "createdAt", column = "created_at", javaType = Timestamp.class),
            @Result(property = "updatedAt", column = "updated_at", javaType = Timestamp.class),
    })
    List<Comment> findAllForMessage(@Param("id") Integer messageId);

    @Update(
            "UPDATE forum_messages SET state = #{upd.state}, body = #{upd.body}, " +
                    "rating = #{upd.rating}, updated_at = #{upd.updatedAt} WHERE id = #{upd.id}"
    )
    void update(@Param("upd") Comment comment);

    @Delete("DELETE FROM forum_messages WHERE id = #{id}")
    void delete(@Param("id") Integer id);
}
