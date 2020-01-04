package net.thumbtack.forums.service;

import net.thumbtack.forums.model.User;
import net.thumbtack.forums.model.enums.UserRole;
import net.thumbtack.forums.dao.SessionDao;
import net.thumbtack.forums.dto.responses.settings.SettingsDtoResponse;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SettingsServiceTest {
    private SessionDao mockSessionDao;
    private ServerConfigurationProperties mockServerConfigurationProperties;
    private SettingsService settingsService;

    @BeforeEach
    void initMocks() {
        mockSessionDao = mock(SessionDao.class);
        mockServerConfigurationProperties = mock(ServerConfigurationProperties.class);
        settingsService = new SettingsService(mockSessionDao, mockServerConfigurationProperties);
    }

    @Test
    void testGetSettings_requestFromSuperuser_shouldReturnAllSettings() throws ServerException {
        final int banTime = 10;
        final int maxBanCount = 5;
        final int maxNameLength = 50;
        final int minPasswordLength = 10;
        final String sessionToken = "token";
        final User superuser = new User(
                "Cuaron", "a.cuaron@orozco.mx", "strongPassword9874"
        );
        superuser.setRole(UserRole.SUPERUSER);

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(superuser);
        when(mockServerConfigurationProperties.getBanTime())
                .thenReturn(banTime);
        when(mockServerConfigurationProperties.getMaxBanCount())
                .thenReturn(maxBanCount);
        when(mockServerConfigurationProperties.getMaxNameLength())
                .thenReturn(maxNameLength);
        when(mockServerConfigurationProperties.getMinPasswordLength())
                .thenReturn(minPasswordLength);

        final SettingsDtoResponse expectedResponse = new SettingsDtoResponse(
                banTime, maxBanCount, maxNameLength, minPasswordLength
        );
        final SettingsDtoResponse actualResponse = settingsService.getSettings(sessionToken);

        assertEquals(expectedResponse, actualResponse);
        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockServerConfigurationProperties)
                .getMaxNameLength();
        verify(mockServerConfigurationProperties)
                .getMinPasswordLength();
        verify(mockServerConfigurationProperties)
                .getBanTime();
        verify(mockServerConfigurationProperties)
                .getMaxBanCount();
    }

    @Test
    void testGetSettings_requestFromRegularUser_shouldReturnPartOfSettings() throws ServerException {
        final int maxNameLength = 50;
        final int minPasswordLength = 10;
        final String sessionToken = "token";
        final User superuser = new User(
                "Regular", "michael.bay@boom.com", "strongPassword9874"
        );

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(superuser);
        when(mockServerConfigurationProperties.getMaxNameLength())
                .thenReturn(maxNameLength);
        when(mockServerConfigurationProperties.getMinPasswordLength())
                .thenReturn(minPasswordLength);

        final SettingsDtoResponse expectedResponse = new SettingsDtoResponse(
                null, null, maxNameLength, minPasswordLength
        );
        final SettingsDtoResponse actualResponse = settingsService.getSettings(sessionToken);

        assertEquals(expectedResponse, actualResponse);
        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockServerConfigurationProperties)
                .getMaxNameLength();
        verify(mockServerConfigurationProperties)
                .getMinPasswordLength();

        verify(mockServerConfigurationProperties, never())
                .getBanTime();
        verify(mockServerConfigurationProperties, never())
                .getMaxBanCount();
    }

    @Test
    void testGetSettings_requestNotSignedInUser_shouldReturnPartOfSettings() throws ServerException {
        final int maxNameLength = 50;
        final int minPasswordLength = 10;
        final String sessionToken = "token";

        when(mockSessionDao.getUserByToken(anyString()))
                .thenReturn(null);
        when(mockServerConfigurationProperties.getMaxNameLength())
                .thenReturn(maxNameLength);
        when(mockServerConfigurationProperties.getMinPasswordLength())
                .thenReturn(minPasswordLength);

        final SettingsDtoResponse expectedResponse = new SettingsDtoResponse(
                null, null, maxNameLength, minPasswordLength
        );
        final SettingsDtoResponse actualResponse = settingsService.getSettings(sessionToken);

        assertEquals(expectedResponse, actualResponse);
        verify(mockSessionDao).getUserByToken(anyString());
        verify(mockServerConfigurationProperties)
                .getMaxNameLength();
        verify(mockServerConfigurationProperties)
                .getMinPasswordLength();

        verify(mockServerConfigurationProperties, never())
                .getBanTime();
        verify(mockServerConfigurationProperties, never())
                .getMaxBanCount();
    }
}