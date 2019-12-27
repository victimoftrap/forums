package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.model.MessageTree;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.MessageOrder;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageTreeMapper {
    @Insert({"INSERT INTO messages_tree",
            "(forum_id, subject, priority, created_at)",
            "VALUES(#{forum.id}, #{subject}, #{priority.name}, #{createdAt})"
    })
    @Options(useGeneratedKeys = true, keyProperty = "id")
    Integer saveMessageTree(MessageTree tree);

    @Select("SELECT id, forum_id, subject, priority, created_at FROM messages_tree WHERE id = #{id}")
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
                    ),
                    @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class),
            }
    )
    MessageTree getTreeById(int id);

    @Select({"SELECT id, owner_id, tree_id, parent_message, created_at, updated_at,",
            "(SELECT #{order}) AS comment_order,",
            "(SELECT #{allVersions}) AS all_ver, (SELECT #{unpublished}) AS unpublished",
            "FROM messages WHERE id = #{id}"
    })
    @Results(id = "optionsMessage",
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
                    @Result(property = "messageTree", column = "tree_id", javaType = MessageTree.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.MessageTreeMapper.getTreeById",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "parentMessage",
                            column = "{id = parent_message, order = comment_order, allVersions = all_ver, unpublished = unpublished}",
                            javaType = MessageItem.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.MessageTreeMapper.getMessageWithOptions",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "childrenComments",
                            column = "{messageId = id, order = comment_order, allVersions = all_ver, unpublished = unpublished}",
                            javaType = List.class,
                            many = @Many(
                                    select = "net.thumbtack.forums.mappers.MessageTreeMapper.getCommentsWithOptions",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "history",
                            column = "{messageId = id, allVersions = all_ver, unpublished = unpublished}",
                            javaType = List.class,
                            many = @Many(
                                    select = "net.thumbtack.forums.mappers.MessageHistoryMapper.getMessageHistoryByOptions",
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
    MessageItem getMessageWithOptions(
            @Param("id") int id,
            @Param("order") String order,
            @Param("allVersions") boolean allVersions,
            @Param("unpublished") boolean unpublished
    );

    @Select({"SELECT id, owner_id, tree_id, parent_message, created_at, updated_at,",
            "(SELECT #{order}) AS comment_order,",
            "(SELECT #{allVersions}) AS all_ver, (SELECT #{unpublished}) AS unpublished",
            "FROM messages WHERE tree_id = #{treeId} AND parent_message IS NULL"
    })
    @ResultMap("optionsMessage")
    MessageItem getRootWithOptions(
            @Param("treeId") int treeId,
            @Param("order") String order,
            @Param("allVersions") boolean allVersions,
            @Param("unpublished") boolean unpublished
    );

    @Select({"SELECT id, owner_id, tree_id, parent_message, created_at, updated_at,",
            "(SELECT #{order}) AS comment_order,",
            "(SELECT #{allVersions}) AS all_ver, (SELECT #{unpublished}) AS unpublished",
            "FROM messages WHERE parent_message = #{id}",
            "ORDER BY created_at ${order}"
    })
    @ResultMap("optionsMessage")
    List<MessageItem> getCommentsWithOptions(
            @Param("id") int messageId,
            @Param("order") String order,
            @Param("allVersions") boolean allVersions,
            @Param("unpublished") boolean unpublished
    );

    @Select({"SELECT id, forum_id, subject, priority, created_at,",
            "(SELECT #{order.name}) AS comment_order,",
            "(SELECT #{allVersions}) AS all_ver, (SELECT #{unpublished}) AS unpublished",
            "FROM messages_tree WHERE id = #{id}"
    })
    @Results(id = "optionsTree",
            value = {
                    @Result(property = "id", column = "id", javaType = int.class),
                    @Result(property = "forum", column = "forum_id", javaType = Forum.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.ForumMapper.getById",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "rootMessage",
                            column = "{treeId = id, order = comment_order, allVersions = all_ver, unpublished = unpublished}",
                            javaType = MessageItem.class,
                            one = @One(
                                    select = "net.thumbtack.forums.mappers.MessageTreeMapper.getRootWithOptions",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "tags", column = "id", javaType = List.class,
                            many = @Many(
                                    select = "net.thumbtack.forums.mappers.TagMapper.getMessageTreeTags",
                                    fetchType = FetchType.LAZY
                            )
                    ),
                    @Result(property = "createdAt", column = "created_at", javaType = LocalDateTime.class),
            }
    )
    MessageTree getTreeWithOptions(
            @Param("id") int id,
            @Param("order") MessageOrder order,
            @Param("allVersions") boolean allVersions,
            @Param("unpublished") boolean unpublished
    );

    @Select({"SELECT id, forum_id, subject, priority, created_at",
            "FROM messages_tree WHERE forum_id = #{forumId}",
            "ORDER BY priority DESC, created_at #{order.name}",
            "OFFSET #{offset} LIMIT #{limit}"
    })
    @ResultMap("treeResult")
    List<MessageTree> getTreeList(@Param("forumId") int forumId,
                                  @Param("order") MessageOrder order,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

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
