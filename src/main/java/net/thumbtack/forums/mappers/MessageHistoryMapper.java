package net.thumbtack.forums.mappers;

import net.thumbtack.forums.model.HistoryItem;
import net.thumbtack.forums.model.MessageItem;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

public interface MessageHistoryMapper {
    @Insert({"INSERT INTO message_history",
            "(message_id, body, state, created_at)",
            "VALUES (#{hist.messageId}, #{hist.body}, #{hist.state}, #{hist.createdAt})"
    })
    void saveHistory(@Param("hist") HistoryItem history);

    @Insert({"<script>",
            "INSERT INTO message_history",
            "(message_id, body, state, created_at)",
            "VALUES",
            "<foreach item='hist' collection='item.history', separator=','>",
            "(#{hist.messageId}, #{hist.body}, #{hist.state}, #{hist.createdAt})",
            "</foreach>",
            "</script>"
    })
    void saveAllHistory(MessageItem item);
}
