package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.Rating;

import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.FetchType;

import java.util.List;

public interface RatingMapper {
    @Insert({"INSERT INTO message_ratings (message_id, user_id, rating)",
            "VALUES(#{msg.id}, #{rater.id}, #{rating})",
            "ON DUPLICATE KEY UPDATE rating = #{rating}"
    })
    Integer upsertRating(
            @Param("msg") MessageItem message,
            @Param("rater") User rater,
            @Param("rating") int rating
    );

    @Insert({"INSERT INTO message_ratings (message_id, user_id, rating)",
            "VALUES(#{msg.id}, #{rater.id}, #{rating})"
    })
    void rate(
            @Param("msg") MessageItem message,
            @Param("rater") User rater,
            @Param("rating") int rating
    );

    @Update({"UPDATE message_ratings SET rating = #{rating}",
            "WHERE message_id = #{msg.id} AND user_id = #{rater.id}"
    })
    void changeRating(
            @Param("msg") MessageItem message,
            @Param("rater") User rater,
            @Param("rating") int rating
    );

    @Delete({"DELETE FROM message_ratings",
            "WHERE message_id = #{msg.id} AND user_id = #{rater.id}"
    })
    void deleteRate(
            @Param("msg") MessageItem message,
            @Param("rater") User rater
    );

    @Select({"SELECT IFNULL(AVG(rating), 0) AS avg_rating",
            "FROM message_ratings WHERE message_id = #{msg}"
    })
    double getMessageRating(@Param("msg") int messageId);

    @Select({"SELECT IFNULL(COUNT(*), 0) AS rated",
            "FROM message_ratings WHERE message_id = #{msg}"
    })
    int getMessageRatedCount(@Param("msg") int messageId);

    @Delete("DELETE FROM message_ratings")
    void deleteAll();
}
