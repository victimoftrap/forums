package net.thumbtack.forums.mappers.provider;

import org.apache.ibatis.jdbc.SQL;

public class StatisticSqlProvider {
    public String getMessagesWithRatings(final int offset, final int limit) {
        return new SQL()
                .SELECT("messages.id AS msg_id")
                .SELECT("IF(messages.parent_message IS NULL, TRUE, FALSE) AS is_message")
                .SELECT("IFNULL(AVG(rating), 0) AS avg_rating")
                .SELECT("COUNT(rating) AS rated")
                .FROM("messages")
                .LEFT_OUTER_JOIN("message_ratings ON message_ratings.message_id = messages.id")
                .GROUP_BY("msg_id")
                .ORDER_BY("avg_rating DESC, msg_id ASC LIMIT #{limit} OFFSET #{offset}")
                .toString();
    }

    public String getMessagesWithRatingsInForum(final int forumId, final int offset, final int limit) {
        return new SQL()
                .SELECT("messages.id AS msg_id")
                .SELECT("IF(messages.parent_message IS NULL, TRUE, FALSE) AS is_message")
                .SELECT("IFNULL(AVG(rating), 0) AS avg_rating")
                .SELECT("COUNT(rating) AS rated")
                .FROM("messages")
                .LEFT_OUTER_JOIN("messages_tree ON messages.tree_id = messages_tree.id")
                .LEFT_OUTER_JOIN("message_ratings ON message_ratings.message_id = messages.id")
                .WHERE("forum_id = #{forumId}")
                .GROUP_BY("msg_id")
                .ORDER_BY("avg_rating DESC, msg_id ASC LIMIT #{limit} OFFSET #{offset}")
                .toString();
    }

    public String getUsersWithRatings(final int offset, final int limit) {
        return new SQL()
                .SELECT("users.id AS rated_user_id")
                .SELECT("username")
                .SELECT("IFNULL(AVG(rating), 0) AS avg_rating")
                .SELECT("COUNT(rating) AS rated")
                .FROM("users")
                .LEFT_OUTER_JOIN("messages ON users.id = messages.owner_id")
                .LEFT_OUTER_JOIN("message_ratings ON message_ratings.message_id = messages.id")
                .GROUP_BY("rated_user_id")
                .ORDER_BY("avg_rating DESC, rated_user_id ASC LIMIT #{limit} OFFSET #{offset}")
                .toString();
    }

    public String getUsersWithRatingsInForum(final int forumId, final int offset, final int limit) {
        return new SQL()
                .SELECT("users.id AS rated_user_id")
                .SELECT("username")
                .SELECT("IFNULL(AVG(rating), 0) AS avg_rating")
                .SELECT("COUNT(rating) AS rated")
                .FROM("users")
                .LEFT_OUTER_JOIN("messages ON users.id = messages.owner_id")
                .LEFT_OUTER_JOIN("messages_tree ON messages.tree_id = messages_tree.id")
                .LEFT_OUTER_JOIN("message_ratings ON message_ratings.message_id = messages.id")
                .WHERE("forum_id = #{forumId}")
                .GROUP_BY("rated_user_id")
                .ORDER_BY("avg_rating DESC, rated_user_id ASC LIMIT #{limit} OFFSET #{offset}")
                .toString();
    }
}
