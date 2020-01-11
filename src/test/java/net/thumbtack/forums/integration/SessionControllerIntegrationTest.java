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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SessionControllerIntegrationTest extends BaseIntegrationEnvironment {
    @Test
    void testLogoutUser() {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> responseEntity = registerUser(request);
        final String sessionToken = getSessionTokenFromHeaders(responseEntity);

        final ResponseEntity<EmptyDtoResponse> logoutResponse = logoutUser(sessionToken);
        assertEquals(HttpStatus.OK, logoutResponse.getStatusCode());
        assertNotNull(logoutResponse.getBody());
        assertEquals("{}", logoutResponse.getBody().toString());
    }

    @Test
    void testLogoutUser_noSessionTokenInHeader_shouldReturnBadRequest() {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        registerUser(request);

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);
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
        try {
            logoutUser("JAVASESSIONID=" + UUID.randomUUID().toString());
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.WRONG_SESSION_TOKEN.name()));
        }
    }

    @Test
    void testLoginUser() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> responseEntity = registerUser(registerRequest);
        final String sessionToken = getSessionTokenFromHeaders(responseEntity);

        logoutUser(sessionToken);

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                registerRequest.getName(), registerRequest.getPassword()
        );
        final ResponseEntity<UserDtoResponse> loginResponseEntity = loginUser(loginRequest);
        assertEquals(HttpStatus.OK, loginResponseEntity.getStatusCode());
        assertNotNull(loginResponseEntity.getBody());

        final UserDtoResponse loginResponse = loginResponseEntity.getBody();
        assertEquals(loginRequest.getName(), loginResponse.getName());
        assertEquals(registerRequest.getEmail(), loginResponse.getEmail());
        assertNull(loginResponse.getSessionToken());
    }

    @Test
    void testLoginUser_userHaveSession_shouldCreateNewSession() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponseEntity = registerUser(registerRequest);
        final String firstSessionToken = getSessionTokenFromHeaders(registerResponseEntity);

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                registerRequest.getName(), registerRequest.getPassword()
        );
        final ResponseEntity<UserDtoResponse> loginResponseEntity = loginUser(loginRequest);
        assertEquals(HttpStatus.OK, loginResponseEntity.getStatusCode());
        assertNotNull(loginResponseEntity.getBody());

        final UserDtoResponse loginResponse = loginResponseEntity.getBody();
        assertEquals(loginRequest.getName(), loginResponse.getName());
        assertEquals(registerRequest.getEmail(), loginResponse.getEmail());
        assertNull(loginResponse.getSessionToken());

        final String secondSessionToken = getSessionTokenFromHeaders(loginResponseEntity);
        assertNotEquals(firstSessionToken, secondSessionToken);
    }

    @Test
    void testLoginUser_requestWithCaseInsensitiveUsername_shouldSuccessfullyLogin() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "Username", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> responseEntity = registerUser(registerRequest);
        final String sessionToken = getSessionTokenFromHeaders(responseEntity);

        logoutUser(sessionToken);

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "uSeRnAmE", registerRequest.getPassword()
        );
        final ResponseEntity<UserDtoResponse> loginResponseEntity = loginUser(loginRequest);
        assertEquals(HttpStatus.OK, loginResponseEntity.getStatusCode());
        assertNotNull(loginResponseEntity.getBody());
        assertNotEquals(sessionToken, getSessionTokenFromHeaders(loginResponseEntity));

        final UserDtoResponse loginResponse = loginResponseEntity.getBody();
        assertEquals(registerRequest.getName(), loginResponse.getName());
        assertEquals(registerRequest.getEmail(), loginResponse.getEmail());
        assertNull(loginResponse.getSessionToken());
    }

    @Test
    void testLoginUser_userNotExists_shouldReturnNotFoundExceptionDto() {
        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "ThisUserNotExists", "w3ryStr0nGPa55wD"
        );
        try {
            loginUser(loginRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_NOT_FOUND.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_NOT_FOUND.getErrorCauseField()));
        }
    }

    @Test
    void testLoginUser_userDeleted_shouldReturnNotFoundExceptionDto() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponseEntity = registerUser(registerRequest);
        final String sessionToken = getSessionTokenFromHeaders(registerResponseEntity);
        deleteUser(sessionToken);

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "testUsername", "w3ryStr0nGPa55wD"
        );
        try {
            loginUser(loginRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_NOT_FOUND.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_NOT_FOUND.getErrorCauseField()));
        }
    }

    @Test
    void testLoginUser_passwordNotMatches_shouldReturnBadRequestExceptionDto() {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> responseEntity = registerUser(request);
        final String sessionToken = getSessionTokenFromHeaders(responseEntity);
        logoutUser(sessionToken);

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "testUsername", "anyOtherPassword"
        );
        try {
            loginUser(loginRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.INVALID_PASSWORD.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.INVALID_PASSWORD.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.INVALID_PASSWORD.getErrorCauseField()));
        }
    }
}
