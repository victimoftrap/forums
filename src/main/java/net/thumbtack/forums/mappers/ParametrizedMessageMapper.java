package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.MessageTree;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.HistoryItem;
import net.thumbtack.forums.model.enums.MessageState;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.time.LocalDateTime;
import java.util.List;

public interface ParametrizedMessageMapper {
    String PARAMS_INITIALIZING = "(SELECT #{order}) AS comment_order," +
            "(SELECT #{allVersions}) AS all_versions," +
            "(SELECT #{unpublished}) AS unpublished";

    @Select({"SELECT id, owner_id, tree_id, parent_message, created_at, updated_at,",
            PARAMS_INITIALIZING,
            "FROM messages",
            "WHERE id = #{id} AND parent_message IS NULL"
    })
    @Results(
            id = "rootMessageResult",
            value = {
                    @Result(property = "id", column = "id", javaType = int.class),
                    @Result(property = "owner",
                            column = "owner_id",
                            javaType = User.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.UserMapper.getById",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "messageTree",
                            column = "tree_id",
                            javaType = MessageTree.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.MessageTreeMapper.getTreeById",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "parentMessage",
                            column = "{id = parent_message, order = comment_order, " +
                                    "allVersions = all_versions, unpublished = unpublished}",
                            javaType = MessageItem.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.ParametrizedMessageMapper.getMessage",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "childrenComments",
                            column = "{parentId = id, order = comment_order, " +
                                    "allVersions = all_versions, unpublished = unpublished}",
                            javaType = List.class,
                            many = @Many(
                                    select = "net.thumbtack.forums.mappers.ParametrizedMessageMapper.getComments",
                                    fetchType = FetchType.EAGER
                            )
                    ),
                    @Result(property = "history",
                            column = "{messageId = id, allVersions = all_versions, unpublished = unpublished}",
                            javaType = List.class,
                            many = @Many(
                                    select = "net.thumbtack.forums.mappers.ParametrizedMessageMapper.getHistory",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class),
                    @Result(property = "updatedAt", column = "updated_at", javaType = LocalDateTime.class),
                    @Result(property = "averageRating",
                            column = "id",
                            javaType = double.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.RatingMapper.getMessageRating",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "ratings",
                            column = "id",
                            javaType = List.class,
                            many = @Many(
                                    select = "net.thumbtack.forums.mappers.RatingMapper.getMessageRatingsList",
                                    fetchType = FetchType.LAZY
                            )
                    )
            }
    )
    MessageItem getRootMessage(
            @Param("id") int id,
            @Param("order") String order,
            @Param("allVersions") boolean allVersions,
            @Param("unpublished") boolean unpublished
    );

    @Select({
            "SELECT id, owner_id, tree_id, parent_message, created_at, updated_at,",
            PARAMS_INITIALIZING,
            "FROM messages",
            "WHERE parent_message = #{parentId}",
            "ORDER BY created_at ${order}"
    })
    @ResultMap("rootMessageResult")
    List<MessageItem> getComments(
            @Param("parentId") int parentId,
            @Param("order") String order,
            @Param("allVersions") boolean allVersions,
            @Param("unpublished") boolean unpublished
    );

    @Select({
            "SELECT id, owner_id, tree_id, parent_message, created_at, updated_at,",
            PARAMS_INITIALIZING,
            "FROM messages",
            "WHERE id = #{id}"
    })
    @ResultMap("rootMessageResult")
    MessageItem getMessage(
            @Param("id") int id,
            @Param("order") String order,
            @Param("allVersions") boolean allVersions,
            @Param("unpublished") boolean unpublished
    );

    @Select({"<script>",
            "SELECT",
            "IF(state = 'UNPUBLISHED', CONCAT('[UNPUBLISHED]', body), body) AS body, state, created_at",
            "FROM message_history WHERE message_id = #{messageId}",
            "<if test='unpublished == false'>",
            "AND state = 'PUBLISHED'",
            "</if>",
            "ORDER BY created_at DESC",
            "<if test='allVersions == false'>",
            "LIMIT 1",
            "</if>",
            "</script>"
    })
    @Results(id = "historyResult",
            value = {
                    @Result(property = "body", column = "body", javaType = String.class),
                    @Result(property = "state", column = "state", javaType = MessageState.class),
                    @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class)
            }
    )
    List<HistoryItem> getHistory(
            @Param("messageId") int messageId,
            @Param("allVersions") boolean allVersions,
            @Param("unpublished") boolean unpublished
    );
}
