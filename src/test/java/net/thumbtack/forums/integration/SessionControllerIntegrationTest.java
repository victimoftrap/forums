package net.thumbtack.forums.integration;

import net.thumbtack.forums.dto.requests.user.LoginUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.responses.user.UserDtoResponse;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.exception.ErrorCode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SessionControllerIntegrationTest extends BaseIntegrationEnvironment {
    private RestTemplate restTemplate = new RestTemplate();

    @Test
    void testLogoutUser() {
        RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> responseEntity = restTemplate.postForEntity(
                SERVER_URL + "/users", request, UserDtoResponse.class
        );
        String cookie = responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, cookie);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<EmptyDtoResponse> logoutResponse = restTemplate.exchange(
                SERVER_URL + "/sessions", HttpMethod.DELETE, httpEntity, EmptyDtoResponse.class
        );
        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());
    }

    @Test
    void testLogoutUser_noSessionCookieInHeader_shouldReturnBadRequest() {
        RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> responseEntity = restTemplate.postForEntity(
                SERVER_URL + "/users", request, UserDtoResponse.class
        );
        String cookie = responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        try {
            restTemplate.exchange(
                    SERVER_URL + "/sessions", HttpMethod.DELETE, httpEntity, EmptyDtoResponse.class
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains("cookie"));
        }
    }

    @Test
    void testLogoutUser_logoutNotExistedUser_shouldReturnBadRequest() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, "JAVASESSIONID=bebebe");
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        try {
            restTemplate.exchange(
                    SERVER_URL + "/sessions", HttpMethod.DELETE, httpEntity, EmptyDtoResponse.class
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.WRONG_SESSION_TOKEN.name()));
        }
    }

    @Test
    void testLoginUser() {
        RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> responseEntity = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest, UserDtoResponse.class
        );
        String cookie = responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, cookie);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);
        restTemplate.exchange(SERVER_URL + "/sessions", HttpMethod.DELETE, httpEntity, EmptyDtoResponse.class);

        LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                registerRequest.getName(), registerRequest.getPassword()
        );

        ResponseEntity<UserDtoResponse> loginResponse = restTemplate.postForEntity(
                SERVER_URL + "/sessions", loginRequest, UserDtoResponse.class
        );
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertEquals(loginRequest.getName(), loginResponse.getBody().getName());
        assertEquals(registerRequest.getEmail(), loginResponse.getBody().getEmail());
        assertNull(loginResponse.getBody().getSessionToken());
    }

    @Test
    void testLoginUser_userNotExists_shouldReturnBadRequest() {
        LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "testUsername", "w3ryStr0nGPa55wD"
        );
        try {
            restTemplate.postForEntity(SERVER_URL + "/sessions", loginRequest, UserDtoResponse.class);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_NOT_FOUND.getErrorCauseField()));
        }
    }

    @Test
    void testLoginUser_userDeleted_shouldReturnBadRequest() {
        RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> responseEntity = restTemplate.postForEntity(
                SERVER_URL + "/users", request, UserDtoResponse.class
        );
        String cookie = responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, cookie);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);
        restTemplate.exchange(SERVER_URL + "/users", HttpMethod.DELETE, httpEntity, EmptyDtoResponse.class);

        LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "testUsername", "w3ryStr0nGPa55wD"
        );
        try {
            restTemplate.postForEntity(SERVER_URL + "/sessions", loginRequest, UserDtoResponse.class);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_NOT_FOUND.getErrorCauseField()));
        }
    }

    @Test
    void testLoginUser_passwordNotMatches_shouldReturnBadRequest() {
        RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> responseEntity = restTemplate.postForEntity(
                SERVER_URL + "/users", request, UserDtoResponse.class
        );
        String cookie = responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, cookie);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);
        restTemplate.exchange(SERVER_URL + "/sessions", HttpMethod.DELETE, httpEntity, EmptyDtoResponse.class);

        LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "testUsername", "anyOtherPassword"
        );
        try {
            restTemplate.postForEntity(SERVER_URL + "/sessions", loginRequest, UserDtoResponse.class);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.INVALID_PASSWORD.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.INVALID_PASSWORD.getErrorCauseField()));
        }
    }
}
