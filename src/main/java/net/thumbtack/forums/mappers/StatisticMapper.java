package net.thumbtack.forums.mappers;

import net.thumbtack.forums.view.MessagesCountView;
import net.thumbtack.forums.view.MessageRatingView;
import net.thumbtack.forums.view.UserRatingView;
import net.thumbtack.forums.mappers.provider.StatisticSqlProvider;

import org.apache.ibatis.annotations.*;

import java.util.List;

public interface StatisticMapper {
    @Select({"SELECT COUNT(DISTINCT message_id) AS pmc FROM message_history",
            "WHERE state = 'PUBLISHED' AND message_id IN (",
            "SELECT id FROM messages WHERE parent_message IS NULL",
            ")"
    })
    int getMessagesCount();

    @Select({"SELECT COUNT(DISTINCT message_id) AS pcc FROM message_history",
            "WHERE state = 'PUBLISHED' AND message_id IN (",
            "SELECT id FROM messages WHERE parent_message IS NOT NULL",
            ")"
    })
    int getCommentsCount();

    @SelectProvider(method = "getMessagesAndCommentsCount", type = StatisticSqlProvider.class)
    @Results(id = "countsViewResult",
            value = {
                    @Result(property = "messagesCount", column = "messages_count", javaType = int.class),
                    @Result(property = "commentsCount", column = "comments_count", javaType = int.class)
            }
    )
    @ConstructorArgs(value = {
            @Arg(name = "messagesCount", column = "messages_count", javaType = int.class),
            @Arg(name = "commentsCount", column = "comments_count", javaType = int.class)
    })
    MessagesCountView getMessagesAndCommentsCount();

    @SelectProvider(method = "getMessagesAndCommentsCountInForum", type = StatisticSqlProvider.class)
    @ResultMap("countsViewResult")
    MessagesCountView getMessagesAndCommentsCountInForum(@Param("forumId") int forumId);

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
