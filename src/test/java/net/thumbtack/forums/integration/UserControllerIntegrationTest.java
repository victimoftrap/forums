package net.thumbtack.forums.integration;

import net.thumbtack.forums.dto.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.user.UpdatePasswordDtoRequest;
import net.thumbtack.forums.dto.user.UserDtoResponse;
import net.thumbtack.forums.dto.EmptyDtoResponse;
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
public class UserControllerIntegrationTest extends BaseIntegrationEnvironment {
    private RestTemplate restTemplate = new RestTemplate();

    @Test
    void testRegisterUser() {
        RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );

        ResponseEntity<UserDtoResponse> responseEntity = restTemplate.postForEntity(
                SERVER_URL + "/users", request, UserDtoResponse.class
        );
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getHeaders().containsKey(HttpHeaders.SET_COOKIE));

        UserDtoResponse response = responseEntity.getBody();
        assertEquals(request.getName(), response.getName());
        assertEquals(request.getEmail(), response.getEmail());
        assertNull(response.getSessionToken());
    }

    @Test
    void testRegisterUser_invalidRequestData_shouldReturnBadRequest() {
        RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "", "test-email@email.com", "w3ryStr0nGPa55wD"
        );

        try {
            restTemplate.postForEntity(SERVER_URL + "/users", request, UserDtoResponse.class);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains("name"));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.INVALID_REQUEST_DATA.name()));
        }
    }

    @Test
    void testRegisterUser_registerExistingUser_shouldReturnBadRequest() {
        RegisterUserDtoRequest firstRequest = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        restTemplate.postForEntity(SERVER_URL + "/users", firstRequest, UserDtoResponse.class);

        RegisterUserDtoRequest secondRequest = new RegisterUserDtoRequest(
                "", "test-email@email.com", "w3ryStr0nGPa55wD"
        );

        try {
            restTemplate.postForEntity(
                    SERVER_URL + "/users", secondRequest, UserDtoResponse.class
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains("name"));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.INVALID_REQUEST_DATA.name()));
        }
    }

    @Test
    void testDeleteUser() {
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

        ResponseEntity<EmptyDtoResponse> deleteResponseEntity = restTemplate.exchange(
                SERVER_URL + "/users",
                HttpMethod.DELETE,
                httpEntity,
                EmptyDtoResponse.class
        );
        assertEquals(HttpStatus.OK, deleteResponseEntity.getStatusCode());
    }

    @Test
    void testDeleteUser_noSessionCookieInHeader_shouldReturnBadRequest() {
        RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> responseEntity = restTemplate.postForEntity(
                SERVER_URL + "/users", request, UserDtoResponse.class
        );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        try {
            restTemplate.exchange(
                    SERVER_URL + "/users",
                    HttpMethod.DELETE,
                    httpEntity,
                    EmptyDtoResponse.class
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains("cookie"));
        }
    }

    @Test
    void testDeleteUser_deletingAlreadyDeletedUser_shouldReturnBadRequest() {
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

        try {
            restTemplate.exchange(
                    SERVER_URL + "/users",
                    HttpMethod.DELETE,
                    httpEntity,
                    EmptyDtoResponse.class
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.WRONG_SESSION_TOKEN.name()));
        }
    }

    @Test
    void testUpdateUser() {
        RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> responseEntity = restTemplate.postForEntity(
                SERVER_URL + "/users", request, UserDtoResponse.class
        );
        String cookie = responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        UpdatePasswordDtoRequest updateRequest = new UpdatePasswordDtoRequest(
                request.getName(), request.getPassword(), "an0th3rStr0ngPa55w0r|)"
        );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, cookie);
        HttpEntity<Object> httpEntity = new HttpEntity<>(updateRequest, httpHeaders);

        ResponseEntity<UserDtoResponse> updateResponse = restTemplate.exchange(
                SERVER_URL + "/users", HttpMethod.PUT, httpEntity, UserDtoResponse.class
        );
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertEquals(updateRequest.getName(), updateResponse.getBody().getName());
        assertEquals(request.getEmail(), updateResponse.getBody().getEmail());
        assertNull(updateResponse.getBody().getSessionToken());
    }

    @Test
    void testUpdateUser_invalidRequestData_shouldReturnBadRequest() {
        RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> responseEntity = restTemplate.postForEntity(
                SERVER_URL + "/users", request, UserDtoResponse.class
        );
        String cookie = responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        UpdatePasswordDtoRequest updateRequest = new UpdatePasswordDtoRequest(
                request.getName(), request.getPassword(), ""
        );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, cookie);
        HttpEntity<Object> httpEntity = new HttpEntity<>(updateRequest, httpHeaders);

        try {
            restTemplate.exchange(
                    SERVER_URL + "/users", HttpMethod.PUT, httpEntity, UserDtoResponse.class
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains("password"));
        }
    }
}
