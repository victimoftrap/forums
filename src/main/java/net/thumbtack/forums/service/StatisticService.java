package net.thumbtack.forums.service;

import net.thumbtack.forums.configuration.ConstantsProperties;
import net.thumbtack.forums.dao.ForumDao;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.dao.StatisticDao;
import net.thumbtack.forums.view.UserRatingView;
import net.thumbtack.forums.dto.responses.statistic.UserRatingListDtoResponse;
import net.thumbtack.forums.converter.StatisticsConverter;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;

import java.util.List;

@Service("statisticService")
public class StatisticService extends ServiceBase {
    private final StatisticDao statisticDao;
    private final ConstantsProperties constantsProperties;

    @Autowired
    public StatisticService(
            final StatisticDao statisticDao,
            final SessionDao sessionDao,
            final ForumDao forumDao,
            final ServerConfigurationProperties serverProperties,
            final ConstantsProperties constantsProperties) {
        super(sessionDao, forumDao, serverProperties);
        this.statisticDao = statisticDao;
        this.constantsProperties = constantsProperties;
    }

    private int getPaginationOffset(@Nullable final Integer receivedOffset) {
        if (receivedOffset == null) {
            return constantsProperties.getDefaultOffset();
        }
        return receivedOffset;
    }

    private int getPaginationLimit(@Nullable final Integer receivedLimit) {
        if (receivedLimit == null) {
            return constantsProperties.getDefaultLimit();
        }
        return receivedLimit;
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
