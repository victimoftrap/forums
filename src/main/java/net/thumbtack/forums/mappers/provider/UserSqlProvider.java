package net.thumbtack.forums.mappers.provider;

import org.apache.ibatis.jdbc.SQL;

public class UserSqlProvider {
    public String getAllUsersWithSessions() {
        return new SQL()
                .SELECT("id, role, LOWER(username) AS username, email, password")
                .SELECT("registered_at, deleted, banned_until, ban_count")
                .SELECT("session_token")
                .FROM("users_sessions")
                .RIGHT_OUTER_JOIN("users ON user_id = users.id")
                .toString();
    }
}
