package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.MessageItem;
import net.thumbtack.forums.model.User;

import org.apache.ibatis.annotations.*;

public interface RatingMapper {
    @Insert({"INSERT INTO message_ratings (message_id, user_id, rating)",
            "VALUES(#{msg.id}, #{rater.id}, #{rating})"
    })
    void rate(@Param("msg") MessageItem message, @Param("rater") User user, @Param("rating") int rating);

    @Update({"UPDATE message_ratings SET rating = #{rating}",
            "WHERE message_id = #{msg.id} AND user_id = #{rater.id}"
    })
    void changeRating(@Param("msg") MessageItem message, @Param("rater") User user, @Param("rating") int rating);

    @Delete({"DELETE FROM message_ratings",
            "WHERE message_id = #{msg.id} AND user_id = #{rater.id}"
    })
    void deleteRate(@Param("msg") MessageItem message, @Param("rater") User user);

    @Select("SELECT AVG(rating) AS avg_rating FROM message_ratings WHERE message_id = #{msg}")
    int getMessageRating(@Param("msg") int messageId);

    @Delete("DELETE FROM message_ratings")
    void deleteAll();
}
