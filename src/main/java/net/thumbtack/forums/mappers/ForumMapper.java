package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.model.enums.ForumType;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.time.LocalDateTime;
import java.util.List;

public interface ForumMapper {
    @Insert("INSERT INTO forums (forum_type, owner_id, name, readonly, created_at) " +
            "VALUES(" +
            "#{type.name}, #{owner.id}, " +
            "#{name}, #{readonly}, #{createdAt}" +
            ")"
    )
    @Options(useGeneratedKeys = true)
    Integer save(Forum forum);

    @Select("SELECT id, forum_type, owner_id, name, readonly, created_at FROM forums WHERE id = #{id}")
    @Results({
            @Result(property = "id", column = "id", javaType = Integer.class),
            @Result(property = "type", column = "forum_type", javaType = ForumType.class),
            @Result(property = "owner", column = "owner_id", javaType = User.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.UserMapper.getById",
                            fetchType = FetchType.LAZY
                    )
            ),
            @Result(property = "name", column = "name", javaType = String.class),
            @Result(property = "readonly", column = "readonly", javaType = Boolean.class),
            @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class),
            @Result(property = "messageCount", column = "id", javaType = Integer.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.ForumMapper.getPublishedMessagesCountInForum"
                    )
            ),
            /*@Result(property = "commentCount", column = "id", javaType = Integer.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.MessageMapper.getCommentsCount"
                    )
            )*/
    })
    Forum getById(@Param("id") int id);

    @Select({"SELECT COUNT(*) FROM message_history WHERE state = 'PUBLISHED' AND message_id IN (",
            "SELECT root_message FROM messages_tree WHERE forum_id = #{id}",
            ")"
    })
    int getPublishedMessagesCountInForum(@Param("id") int forumId);

    @Select("SELECT id, forum_type, owner_id, name, readonly, created_at FROM forums")
    @Results({
            @Result(property = "id", column = "id", javaType = Integer.class),
            @Result(property = "type", column = "forum_type", javaType = ForumType.class),
            @Result(property = "owner", column = "owner_id", javaType = User.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.UserMapper.getById",
                            fetchType = FetchType.LAZY
                    )
            ),
            @Result(property = "name", column = "name", javaType = String.class),
            @Result(property = "readonly", column = "readonly", javaType = Boolean.class),
            @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class),
            @Result(property = "messageCount", column = "id", javaType = Integer.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.MessageTreeMapper.getMessagesCount",
                            fetchType = FetchType.LAZY
                    )
            ),
            @Result(property = "commentCount", column = "id", javaType = Integer.class,
                    one = @One(
                            select = "net.thumbtack.forums.mappers.MessageMapper.getCommentsCount",
                            fetchType = FetchType.LAZY
                    )
            )
    })
    List<Forum> getAll();

    @Update("UPDATE forums SET readonly = COALESCE(#{forum.readonly}, readonly) WHERE id = #{forum.id}")
    void update(@Param("forum") Forum forum);

    @Update("UPDATE forums SET readonly = TRUE WHERE owner_id = #{user.id}")
    void madeReadonlyModeratedForumsOf(@Param("user") User user);

    @Delete("DELETE FROM forums WHERE id = #{id}")
    void deleteById(@Param("id") int id);

    @Delete("DELETE FROM forums")
    void deleteAll();
}
