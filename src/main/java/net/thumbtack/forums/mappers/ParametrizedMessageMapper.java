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

    String CREATOR_UNPUBLISHED = ", (SELECT #{requesterId}) AS requester_id," +
            "(SELECT owner_id = #{requesterId}) AS owner_unpublished";

    @Select({"SELECT id, owner_id, tree_id, parent_message, created_at, updated_at,",
            PARAMS_INITIALIZING,
            CREATOR_UNPUBLISHED,
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
                            column = "{id = parent_message, order = comment_order, allVersions = all_versions," +
                                    " unpublished = unpublished, requesterId = requester_id}",
                            javaType = MessageItem.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.ParametrizedMessageMapper.getMessage",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "childrenComments",
                            column = "{parentId = id, order = comment_order, allVersions = all_versions, " +
                                    "unpublished = unpublished, requesterId = requester_id}",
                            javaType = List.class,
                            many = @Many(
                                    select = "net.thumbtack.forums.mappers.ParametrizedMessageMapper.getComments",
                                    fetchType = FetchType.EAGER
                            )
                    ),
                    @Result(property = "history",
                            column = "{messageId = id, allVersions = all_versions, " +
                                    "unpublished = unpublished, ownerUnpublished = owner_unpublished}",
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
                    @Result(property = "rated", column = "id", javaType = int.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.RatingMapper.getMessageRatedCount",
                                    fetchType = FetchType.LAZY
                            )
                    )
            }
    )
    MessageItem getRootMessage(
            @Param("id") int id,
            @Param("order") String order,
            @Param("allVersions") boolean allVersions,
            @Param("unpublished") boolean unpublished,
            @Param("requesterId") int requesterId
    );

    @Select({
            "SELECT id, owner_id, tree_id, parent_message, created_at, updated_at,",
            PARAMS_INITIALIZING,
            CREATOR_UNPUBLISHED,
            "FROM messages",
            "WHERE parent_message = #{parentId}",
            "ORDER BY created_at ${order}"
    })
    @ResultMap("rootMessageResult")
    List<MessageItem> getComments(
            @Param("parentId") int parentId,
            @Param("order") String order,
            @Param("allVersions") boolean allVersions,
            @Param("unpublished") boolean unpublished,
            @Param("requesterId") int requesterId
    );

    @Select({
            "SELECT id, owner_id, tree_id, parent_message, created_at, updated_at,",
            PARAMS_INITIALIZING,
            CREATOR_UNPUBLISHED,
            "FROM messages",
            "WHERE id = #{id}"
    })
    @ResultMap("rootMessageResult")
    MessageItem getMessage(
            @Param("id") int id,
            @Param("order") String order,
            @Param("allVersions") boolean allVersions,
            @Param("unpublished") boolean unpublished,
            @Param("requesterId") int requesterId
    );

    @Select({"<script>",
            "SELECT",
            "IF(state = 'UNPUBLISHED', CONCAT('[UNPUBLISHED]', body), body) AS body, state, created_at",
            "FROM message_history WHERE message_id = #{messageId}",
            "<if test='unpublished == false and ownerUnpublished == false'>",
            "AND state = 'PUBLISHED'",
            "</if>",
            "ORDER BY id DESC",
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
            @Param("unpublished") boolean unpublished,
            @Param("ownerUnpublished") boolean ownerUnpublished
    );

    @Select({
            "SELECT id, owner_id, tree_id, parent_message, created_at, updated_at,",
            PARAMS_INITIALIZING,
            CREATOR_UNPUBLISHED,
            "FROM messages",
            "WHERE tree_id = #{treeId} AND parent_message IS NULL"
    })
    @ResultMap("rootMessageResult")
    MessageItem getRootByTreeId(
            @Param("treeId") int treeId,
            @Param("order") String order,
            @Param("allVersions") boolean allVersions,
            @Param("unpublished") boolean unpublished
    );
}
