package net.thumbtack.forums.service;

import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.model.enums.UserRole;
import net.thumbtack.forums.dao.ForumDao;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.converter.ForumConverter;
import net.thumbtack.forums.dto.requests.forum.CreateForumDtoRequest;
import net.thumbtack.forums.dto.responses.forum.ForumDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumInfoDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumInfoListDtoResponse;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service("forumService")
public class ForumService extends ServiceBase {
    private final ForumDao forumDao;

    @Autowired
    public ForumService(final ForumDao forumDao,
                        final SessionDao sessionDao,
                        final ServerConfigurationProperties serverProperties) {
        super(sessionDao, forumDao, serverProperties);
        this.forumDao = forumDao;
    }

    public ForumDtoResponse createForum(
            final String token,
            final CreateForumDtoRequest request
    ) throws ServerException {
        final User requesterUser = getUserBySession(token);
        checkUserBanned(requesterUser);

        final Forum forum = new Forum(
                ForumType.valueOf(request.getType()), requesterUser, request.getName(),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS)
        );
        forumDao.save(forum);
        return new ForumDtoResponse(forum.getId(), forum.getName(), forum.getType().name());
    }

    public EmptyDtoResponse deleteForum(
            final String token,
            final int forumId
    ) throws ServerException {
        final User requesterUser = getUserBySession(token);
        checkUserBannedPermanently(requesterUser);

        final Forum deletingForum = getForumById(forumId);
        if (!deletingForum.getOwner().equals(requesterUser) && requesterUser.getRole() != UserRole.SUPERUSER) {
            throw new ServerException(ErrorCode.FORBIDDEN_OPERATION);
        }

        forumDao.deleteById(forumId);
        return new EmptyDtoResponse();
    }

    public ForumInfoDtoResponse getForum(
            final String token,
            final int forumId
    ) throws ServerException {
        getUserBySession(token);
        final Forum forum = getForumById(forumId);
        return ForumConverter.forumToResponse(forum);
    }

    public ForumInfoListDtoResponse getForums(
            final String token
    ) throws ServerException {
        getUserBySession(token);
        final List<Forum> forums = forumDao.getAll();
        return ForumConverter.forumListToResponse(forums);
    }
}
