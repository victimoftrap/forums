package net.thumbtack.forums.service;

import net.thumbtack.forums.dao.ForumDao;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.dao.StatisticDao;
import net.thumbtack.forums.view.MessageRatingView;
import net.thumbtack.forums.view.UserRatingView;
import net.thumbtack.forums.converter.StatisticsConverter;
import net.thumbtack.forums.dto.responses.statistic.MessagesCountDtoResponse;
import net.thumbtack.forums.dto.responses.statistic.CommentsCountDtoResponse;
import net.thumbtack.forums.dto.responses.statistic.MessageRatingListDtoResponse;
import net.thumbtack.forums.dto.responses.statistic.UserRatingListDtoResponse;
import net.thumbtack.forums.configuration.ConstantsProperties;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;
import net.thumbtack.forums.exception.ServerException;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.List;

@Service("statisticService")
public class StatisticService extends ServiceBase {
    private final StatisticDao statisticDao;

    @Autowired
    public StatisticService(
            final StatisticDao statisticDao,
            final SessionDao sessionDao,
            final ForumDao forumDao,
            final ServerConfigurationProperties serverProperties,
            final ConstantsProperties constantsProperties) {
        super(sessionDao, forumDao, serverProperties, constantsProperties);
        this.statisticDao = statisticDao;
    }

    public MessagesCountDtoResponse getMessagesCount(
            final String sessionToken,
            @Nullable final Integer forumId
    ) throws ServerException {
        getUserBySession(sessionToken);

        final int count;
        if (forumId != null) {
            getForumById(forumId);
            count = statisticDao.getMessagesCount(forumId);
        } else {
            count = statisticDao.getMessagesCount();
        }
        return new MessagesCountDtoResponse(count);
    }

    public CommentsCountDtoResponse getCommentsCount(
            final String sessionToken,
            @Nullable final Integer forumId
    ) throws ServerException {
        getUserBySession(sessionToken);

        final int count;
        if (forumId != null) {
            getForumById(forumId);
            count = statisticDao.getCommentsCount(forumId);
        } else {
            count = statisticDao.getCommentsCount();
        }
        return new CommentsCountDtoResponse(count);
    }

    public MessageRatingListDtoResponse getMessagesRatings(
            final String sessionToken,
            @Nullable final Integer forumId,
            @Nullable final Integer offset,
            @Nullable final Integer limit
    ) throws ServerException {
        getUserBySession(sessionToken);

        final int realOffset = getPaginationOffset(offset);
        final int realLimit = getPaginationLimit(limit);

        final List<MessageRatingView> ratings;
        if (forumId != null) {
            getForumById(forumId);
            ratings = statisticDao.getMessagesRatingsInForum(forumId, realOffset, realLimit);
        } else {
            ratings = statisticDao.getMessagesRatings(realOffset, realLimit);
        }
        return StatisticsConverter.messagesRatingsToResponse(ratings);
    }

    public UserRatingListDtoResponse getUsersRatings(
            final String sessionToken,
            @Nullable final Integer forumId,
            @Nullable final Integer offset,
            @Nullable final Integer limit
    ) throws ServerException {
        getUserBySession(sessionToken);

        final int realOffset = getPaginationOffset(offset);
        final int realLimit = getPaginationLimit(limit);

        final List<UserRatingView> ratings;
        if (forumId != null) {
            getForumById(forumId);
            ratings = statisticDao.getUsersRatingsInForum(forumId, realOffset, realLimit);
        } else {
            ratings = statisticDao.getUsersRatings(realOffset, realLimit);
        }
        return StatisticsConverter.usersRatingsToResponse(ratings);
    }
}
