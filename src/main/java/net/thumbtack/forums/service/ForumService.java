package net.thumbtack.forums.service;

import net.thumbtack.forums.dao.ForumDao;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.dto.forum.CreateForumDtoRequest;
import net.thumbtack.forums.dto.forum.ForumDtoResponse;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.model.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service("forumService")
public class ForumService {
    private final ForumDao forumDao;
    private final SessionDao sessionDao;

    @Autowired
    public ForumService(final ForumDao forumDao, final SessionDao sessionDao) {
        this.forumDao = forumDao;
        this.sessionDao = sessionDao;
    }

    private User getUserBySessionOrThrowException(final String token) {
        return Optional
                .ofNullable(sessionDao.getUserByToken(token))
                .orElseThrow(() -> new ServerException(ErrorCode.WRONG_SESSION_TOKEN));
    }

    public ForumDtoResponse createForum(final String token, final CreateForumDtoRequest request) {
        final User user = getUserBySessionOrThrowException(token);
        if (user.getBannedUntil() != null) {
            throw new ServerException(ErrorCode.USER_BANNED);
        }

        final Forum forum = new Forum(
                request.getType(), user, request.getName(), LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        forumDao.save(forum);
        return new ForumDtoResponse(forum.getId(), forum.getName(), forum.getType());
    }
}
