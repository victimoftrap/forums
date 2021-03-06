package net.thumbtack.forums.service;

import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.UserRole;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.dto.responses.settings.SettingsDtoResponse;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("settingsService")
public class SettingsService {
    private final SessionDao sessionDao;
    private final ServerConfigurationProperties properties;

    @Autowired
    public SettingsService(final SessionDao sessionDao,
                           final ServerConfigurationProperties properties) {
        this.sessionDao = sessionDao;
        this.properties = properties;
    }

    public SettingsDtoResponse getSettings(final String sessionToken) throws ServerException {
        final User user = sessionDao.getUserByToken(sessionToken);
        final int maxNameLength = properties.getMaxNameLength();
        final int minPasswordLength = properties.getMinPasswordLength();

        if (user == null) {
            return new SettingsDtoResponse(
                    null, null,
                    maxNameLength, minPasswordLength
            );
        }
        if (user.getRole() == UserRole.SUPERUSER) {
            return new SettingsDtoResponse(
                    properties.getBanTime(), properties.getMaxBanCount(),
                    maxNameLength, minPasswordLength
            );
        }
        return new SettingsDtoResponse(
                null, null,
                maxNameLength, minPasswordLength
        );
    }
}
