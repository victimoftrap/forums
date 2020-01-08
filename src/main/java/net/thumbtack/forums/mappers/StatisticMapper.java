package net.thumbtack.forums.mappers;

import net.thumbtack.forums.view.MessageRatingView;
import net.thumbtack.forums.view.UserRatingView;
import net.thumbtack.forums.mappers.provider.StatisticSqlProvider;

import org.apache.ibatis.annotations.*;

import java.util.List;

public interface StatisticMapper {
    @Select({"<script>",
            "SELECT COUNT(DISTINCT message_id) AS forum_pmc FROM message_history",
            "WHERE message_id IN (",
            "SELECT id FROM messages WHERE parent_message IS NULL AND tree_id IN (",
            "SELECT id FROM messages_tree WHERE forum_id = #{forumId} )",
            ")",
            "<if test='unpublished == false'>",
            "AND state = 'PUBLISHED'",
            "</if>",
            "</script>"
    })
    int getMessagesCountInForum(
            @Param("forumId") int forumId,
            @Param("unpublished") boolean unpublished
    );

    @Select({"<script>",
            "SELECT COUNT(DISTINCT message_id) AS forum_pcc FROM message_history",
            "WHERE message_id IN (",
            "SELECT id FROM messages WHERE parent_message IS NOT NULL AND tree_id IN (",
            "SELECT id FROM messages_tree WHERE forum_id = #{forumId} )",
            ")",
            "<if test='unpublished == false'>",
            "AND state = 'PUBLISHED'",
            "</if>",
            "</script>"
    })
    int getCommentCountInForum(
            @Param("forumId") int forumId,
            @Param("unpublished") boolean unpublished
    );

    @Select({"<script>",
            "SELECT COUNT(DISTINCT message_id) AS pmc FROM message_history",
            "WHERE message_id IN (",
            "SELECT id FROM messages WHERE parent_message IS NULL",
            ")",
            "<if test='unpublished == false'>",
            "AND state = 'PUBLISHED'",
            "</if>",
            "</script>"
    })
    int getMessagesCount(@Param("unpublished") boolean unpublished);

    @Select({"<script>",
            "SELECT COUNT(DISTINCT message_id) AS pcc FROM message_history",
            "WHERE message_id IN (",
            "SELECT id FROM messages WHERE parent_message IS NOT NULL",
            ")",
            "<if test='unpublished == false'>",
            "AND state = 'PUBLISHED'",
            "</if>",
            "</script>"
    })
    int getCommentCount(@Param("unpublished") boolean unpublished);

    @SelectProvider(method = "getMessagesWithRatings", type = StatisticSqlProvider.class)
    @Results(id = "messagesRatingsViewResult",
            value = {
                    @Result(property = "messageId", column = "msg_id", javaType = int.class),
                    @Result(property = "isMessage", column = "is_message", javaType = boolean.class),
                    @Result(property = "rating", column = "avg_rating", javaType = double.class),
                    @Result(property = "rated", column = "rated", javaType = int.class)
            }
    )
    @ConstructorArgs(value = {
            @Arg(name = "messageId", column = "msg_id", javaType = int.class),
            @Arg(name = "isMessage", column = "is_message", javaType = boolean.class),
            @Arg(name = "rating", column = "avg_rating", javaType = double.class),
            @Arg(name = "rated", column = "rated", javaType = int.class)
    })
    List<MessageRatingView> getMessagesRatings(
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    @SelectProvider(method = "getMessagesWithRatingsInForum", type = StatisticSqlProvider.class)
    @ResultMap("messagesRatingsViewResult")
    List<MessageRatingView> getMessagesRatingsInForum(
            @Param("forumId") int forumId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    @SelectProvider(method = "getUsersWithRatings", type = StatisticSqlProvider.class)
    @Results(id = "userRatingsViewResult",
            value = {
                    @Result(property = "userId", column = "rated_user_id", javaType = int.class),
                    @Result(property = "username", column = "username", javaType = String.class),
                    @Result(property = "rating", column = "avg_rating", javaType = double.class),
                    @Result(property = "rated", column = "rated", javaType = int.class)
            }
    )
    @ConstructorArgs(value = {
            @Arg(name = "userId", column = "rated_user_id", javaType = int.class),
            @Arg(name = "username", column = "username", javaType = String.class),
            @Arg(name = "rating", column = "avg_rating", javaType = double.class),
            @Arg(name = "rated", column = "rated", javaType = int.class)
    })
    List<UserRatingView> getUsersRatings(
            @Param("offset") int offset,
            @Param("limit") int limit
    );

    @SelectProvider(method = "getUsersWithRatingsInForum", type = StatisticSqlProvider.class)
    @ResultMap("userRatingsViewResult")
    List<UserRatingView> getUsersRatingsInForum(
            @Param("forumId") int forumId,
            @Param("offset") int offset,
            @Param("limit") int limit
    );
}
