package net.thumbtack.forums.controller;

import net.thumbtack.forums.service.SettingsService;
import net.thumbtack.forums.dto.settings.SettingsDtoResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {
    private final SettingsService settingsService;

    private final String COOKIE_NAME = "JAVASESSIONID";

    @Autowired
    public SettingsController(final SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<SettingsDtoResponse> getSettings(
            @CookieValue(value = COOKIE_NAME, required = false) String token) {
        final SettingsDtoResponse response = settingsService.getSettings(token);
        return ResponseEntity.ok(response);
    }
}
