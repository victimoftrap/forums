package net.thumbtack.forums.controller;

import net.thumbtack.forums.service.SettingsService;
import net.thumbtack.forums.dto.settings.SettingsDtoResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.Cookie;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SettingsController.class)
class SettingsControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private SettingsService settingsService;

    private final String COOKIE_NAME = "JAVASESSIONID";

    @Test
    public void testGetSettings_requestFromRegularUser_shouldReturnLengthData() throws Exception {
        final String token = "token";
        final SettingsDtoResponse response = new SettingsDtoResponse(
                null, null,
                20, 8
        );
        when(settingsService.getSettings(anyString()))
                .thenReturn(response);

        mvc.perform(
                get("/api/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, token))
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.maxNameLength").value(response.getMaxNameLength()))
                .andExpect(jsonPath("$.minPasswordLength").value(response.getMinPasswordLength()))
                .andExpect(jsonPath("$.banTime").doesNotExist())
                .andExpect(jsonPath("$.maxBanCount").doesNotExist());

        verify(settingsService).getSettings(anyString());
    }

    @Test
    public void testGetSettings_requestFromSuperuser_shouldReturnAllData() throws Exception {
        final String token = "token";
        final SettingsDtoResponse response = new SettingsDtoResponse(
                7, 5,
                20, 8
        );
        when(settingsService.getSettings(anyString()))
                .thenReturn(response);
        mvc.perform(
                get("/api/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, token))
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.banTime").value(response.getBanTime()))
                .andExpect(jsonPath("$.maxBanCount").value(response.getMaxBanCount()))
                .andExpect(jsonPath("$.maxNameLength").value(response.getMaxNameLength()))
                .andExpect(jsonPath("$.minPasswordLength").value(response.getMinPasswordLength()));


        verify(settingsService).getSettings(anyString());
    }

    @Test
    public void testGetSettings_requestFromNotSignedInUser_shouldReturnLengthData() throws Exception {
        final SettingsDtoResponse response = new SettingsDtoResponse(
                null, null,
                20, 8
        );
        when(settingsService.getSettings(null))
                .thenReturn(response);

        mvc.perform(
                get("/api/settings")
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.maxNameLength").value(response.getMaxNameLength()))
                .andExpect(jsonPath("$.minPasswordLength").value(response.getMinPasswordLength()))
                .andExpect(jsonPath("$.banTime").doesNotExist())
                .andExpect(jsonPath("$.maxBanCount").doesNotExist());

        verify(settingsService).getSettings(null);
    }
}