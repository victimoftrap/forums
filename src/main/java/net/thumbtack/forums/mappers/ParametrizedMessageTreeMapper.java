package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.MessageTree;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.time.LocalDateTime;
import java.util.List;

public interface ParametrizedMessageTreeMapper {
    String PARAMS_INITIALIZING = "(SELECT #{order}) AS comment_order," +
            "(SELECT #{allVersions}) AS all_versions," +
            "(SELECT #{unpublished}) AS unpublished";

    @Select({"SELECT id, forum_id, subject, priority, created_at,",
            PARAMS_INITIALIZING,
            "FROM messages_tree",
            "WHERE forum_id = #{forumId}",
            "ORDER BY priority DESC, created_at ${order}",
            "LIMIT #{limit} OFFSET #{offset}"
    })
    @Results(id = "treeListResult",
            value = {
                    @Result(property = "id", column = "id", javaType = int.class),
                    @Result(property = "forum", column = "forum_id", javaType = Forum.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.ForumMapper.getById",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "rootMessage",
                            column = "{treeId = id, order = comment_order, " +
                                    "allVersions = all_versions, unpublished = unpublished}",
                            javaType = MessageItem.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.ParametrizedMessageMapper.getRootByTreeId",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "tags", column = "id", javaType = List.class,
                            many = @Many(
                                    select = "net.thumbtack.forums.mappers.TagMapper.getMessageTreeTags",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class)
            }
    )
    List<MessageTree> getTrees(
            @Param("forumId") int forumId,
            @Param("offset") int offset,
            @Param("limit") int limit,
            @Param("order") String order,
            @Param("allVersions") boolean allVersions,
            @Param("unpublished") boolean unpublished
    );
}
