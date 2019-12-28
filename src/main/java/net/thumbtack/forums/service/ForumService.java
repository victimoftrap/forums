package net.thumbtack.forums.service;

import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.UserRole;
import net.thumbtack.forums.dao.ForumDao;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.converter.ForumConverter;
import net.thumbtack.forums.dto.requests.forum.CreateForumDtoRequest;
import net.thumbtack.forums.dto.responses.forum.ForumDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumInfoDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumInfoListDtoResponse;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
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

    // REVU copy/paste метода из UserService
    // сделайте класс ServiceBase, все сервисы extends ServiceBase и все эти методы туда как protected 
    private User getUserBySessionOrThrowException(final String token) throws ServerException {
        return Optional
                .ofNullable(sessionDao.getUserByToken(token))
                .orElseThrow(() -> new ServerException(ErrorCode.WRONG_SESSION_TOKEN));
    }

    private Forum getForumByIdOrThrowException(final int id) throws ServerException {
        return Optional
                .ofNullable(forumDao.getById(id))
                .orElseThrow(() -> new ServerException(ErrorCode.FORUM_NOT_FOUND));
    }

    public ForumDtoResponse createForum(final String token, final CreateForumDtoRequest request) throws ServerException {
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

    public EmptyDtoResponse deleteForum(final String token, final int forumId) throws ServerException {
        final User user = getUserBySessionOrThrowException(token);
        final Forum deletingForum = getForumByIdOrThrowException(forumId);
        if (!deletingForum.getOwner().equals(user) && user.getRole() != UserRole.SUPERUSER) {
            throw new ServerException(ErrorCode.FORBIDDEN_OPERATION);
        }

        forumDao.deleteById(forumId);
        return new EmptyDtoResponse();
    }

    public ForumInfoDtoResponse getForum(final String token, final int forumId) throws ServerException {
        getUserBySessionOrThrowException(token);
        final Forum forum = getForumByIdOrThrowException(forumId);
        return ForumConverter.forumToResponse(forum);
    }

    public ForumInfoListDtoResponse getForums(final String token) throws ServerException {
        getUserBySessionOrThrowException(token);
        final List<Forum> forums = forumDao.getAll();
        return ForumConverter.forumListToResponse(forums);
    }
}
