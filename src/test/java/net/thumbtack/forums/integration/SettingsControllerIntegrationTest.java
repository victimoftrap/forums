package net.thumbtack.forums.integration;

import net.thumbtack.forums.dto.requests.user.LoginUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.responses.user.UserDtoResponse;
import net.thumbtack.forums.dto.responses.settings.SettingsDtoResponse;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SettingsControllerIntegrationTest extends BaseIntegrationEnvironment {
    private RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private ServerConfigurationProperties properties;

    @Test
    void testGetSettings_requestFromNotSignedInUser_shouldReturnPartOfSettings() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        final HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        final ResponseEntity<SettingsDtoResponse> responseEntity = restTemplate.exchange(
                SERVER_URL + "/settings",
                HttpMethod.GET,
                httpEntity,
                SettingsDtoResponse.class
        );
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        final SettingsDtoResponse response = responseEntity.getBody();
        assertNotNull(response);
        assertNull(response.getBanTime());
        assertNull(response.getMaxBanCount());
        assertEquals(properties.getMaxNameLength(), response.getMaxNameLength());
        assertEquals(properties.getMinPasswordLength(), response.getMinPasswordLength());
    }

    @Test
    void testGetSettings_requestFromRegularUser_shouldReturnPartOfSettings() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "user", "a.user@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponseEntity = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest, UserDtoResponse.class
        );
        final String cookie = registerResponseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, cookie);
        final HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        final ResponseEntity<SettingsDtoResponse> responseEntity = restTemplate.exchange(
                SERVER_URL + "/settings",
                HttpMethod.GET,
                httpEntity,
                SettingsDtoResponse.class
        );
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        final SettingsDtoResponse response = responseEntity.getBody();
        assertNotNull(response);
        assertNull(response.getBanTime());
        assertNull(response.getMaxBanCount());
        assertEquals(properties.getMaxNameLength(), response.getMaxNameLength());
        assertEquals(properties.getMinPasswordLength(), response.getMinPasswordLength());
    }

    @Test
    void testGetSettings_requestFromSuperuser_shouldReturnPartOfSettings() {
        final LoginUserDtoRequest adminLoginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        final ResponseEntity<UserDtoResponse> adminLoginResponse = restTemplate.postForEntity(
                SERVER_URL + "/sessions", adminLoginRequest, UserDtoResponse.class
        );
        final String adminCookie = adminLoginResponse
                .getHeaders()
                .getFirst(HttpHeaders.SET_COOKIE);

        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, adminCookie);
        final HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        final ResponseEntity<SettingsDtoResponse> responseEntity = restTemplate.exchange(
                SERVER_URL + "/settings",
                HttpMethod.GET,
                httpEntity,
                SettingsDtoResponse.class
        );
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());

        final SettingsDtoResponse response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(properties.getBanTime(), response.getBanTime());
        assertEquals(properties.getMaxBanCount(), response.getMaxBanCount());
        assertEquals(properties.getMaxNameLength(), response.getMaxNameLength());
        assertEquals(properties.getMinPasswordLength(), response.getMinPasswordLength());
    }
}
