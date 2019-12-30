package net.thumbtack.forums.integration;

import net.thumbtack.forums.dto.requests.user.LoginUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.UpdatePasswordDtoRequest;
import net.thumbtack.forums.dto.responses.user.UserDetailsDtoResponse;
import net.thumbtack.forums.dto.responses.user.UserDetailsListDtoResponse;
import net.thumbtack.forums.dto.responses.user.UserDtoResponse;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.dto.responses.user.UserStatus;
import net.thumbtack.forums.exception.ErrorCode;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

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
    void testUpdateUserPassword() {
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
    void testUpdateUserPassword_invalidRequestData_shouldReturnBadRequest() {
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

    @Test
    void testGetUsers_requestFromRegularUser_sensitiveFieldsAreNull() {
        RegisterUserDtoRequest registerRequest1 = new RegisterUserDtoRequest(
                "user1", "user1@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest1, UserDtoResponse.class
        );
        int userId1 = registerResponse.getBody().getId();
        String userCookie1 = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        RegisterUserDtoRequest registerRequest2 = new RegisterUserDtoRequest(
                "user2", "user2@email.com", "w3ryStr0nGPa55wD"
        );
        registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest2, UserDtoResponse.class
        );
        int userId2 = registerResponse.getBody().getId();
        String userCookie2 = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, userCookie1);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<UserDetailsListDtoResponse> usersResponse = restTemplate.exchange(
                SERVER_URL + "/users",
                HttpMethod.GET,
                httpEntity,
                UserDetailsListDtoResponse.class
        );
        assertEquals(HttpStatus.OK, usersResponse.getStatusCode());

        List<UserDetailsDtoResponse> userDetails = usersResponse.getBody().getUsers();
        assertEquals(3, userDetails.size());
        assertEquals("admin", userDetails.get(0).getName());
        assertEquals(UserStatus.FULL, userDetails.get(0).getStatus());
        assertEquals(0, userDetails.get(0).getBanCount());
        assertFalse(userDetails.get(0).isDeleted());
        assertNull(userDetails.get(0).isSuper());
        assertNull(userDetails.get(0).getTimeBanExit());

        assertEquals(userId1, userDetails.get(1).getId());
        assertEquals(UserStatus.FULL, userDetails.get(1).getStatus());
        assertEquals(0, userDetails.get(1).getBanCount());
        assertTrue(userDetails.get(1).isOnline());
        assertFalse(userDetails.get(1).isDeleted());
        assertNull(userDetails.get(1).isSuper());
        assertNull(userDetails.get(1).getEmail());
        assertNull(userDetails.get(1).getTimeBanExit());

        assertEquals(userId2, userDetails.get(2).getId());
        assertEquals(UserStatus.FULL, userDetails.get(2).getStatus());
        assertEquals(0, userDetails.get(2).getBanCount());
        assertTrue(userDetails.get(2).isOnline());
        assertFalse(userDetails.get(2).isDeleted());
        assertNull(userDetails.get(2).isSuper());
        assertNull(userDetails.get(2).getEmail());
        assertNull(userDetails.get(2).getTimeBanExit());
    }

    @Test
    void testGetUsers_requestFromSuperuser_sensitiveFieldsAreExists() {
        LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        ResponseEntity<UserDtoResponse> loginResponse = restTemplate.postForEntity(
                SERVER_URL + "/sessions", loginRequest, UserDtoResponse.class
        );
        int superUserId = loginResponse.getBody().getId();
        String superUserCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        RegisterUserDtoRequest registerRequest1 = new RegisterUserDtoRequest(
                "user1", "user1@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest1, UserDtoResponse.class
        );
        int userId1 = registerResponse.getBody().getId();

        RegisterUserDtoRequest registerRequest2 = new RegisterUserDtoRequest(
                "user2", "user2@email.com", "w3ryStr0nGPa55wD"
        );
        registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest2, UserDtoResponse.class
        );
        int userId2 = registerResponse.getBody().getId();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, superUserCookie);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<UserDetailsListDtoResponse> usersResponse = restTemplate.exchange(
                SERVER_URL + "/users",
                HttpMethod.GET,
                httpEntity,
                UserDetailsListDtoResponse.class
        );
        assertEquals(HttpStatus.OK, usersResponse.getStatusCode());

        List<UserDetailsDtoResponse> userDetails = usersResponse.getBody().getUsers();
        assertEquals(3, userDetails.size());
        assertEquals("admin", userDetails.get(0).getName());
        assertEquals(UserStatus.FULL, userDetails.get(0).getStatus());
        assertEquals(0, userDetails.get(0).getBanCount());
        assertTrue(userDetails.get(0).isOnline());
        assertFalse(userDetails.get(0).isDeleted());
        assertTrue(userDetails.get(0).isSuper());
        assertNull(userDetails.get(0).getTimeBanExit());

        assertEquals(userId1, userDetails.get(1).getId());
        assertEquals(UserStatus.FULL, userDetails.get(1).getStatus());
        assertEquals(registerRequest1.getEmail(), userDetails.get(1).getEmail());
        assertEquals(0, userDetails.get(1).getBanCount());
        assertTrue(userDetails.get(1).isOnline());
        assertFalse(userDetails.get(1).isDeleted());
        assertFalse(userDetails.get(1).isSuper());
        assertNull(userDetails.get(1).getTimeBanExit());

        assertEquals(userId2, userDetails.get(2).getId());
        assertEquals(UserStatus.FULL, userDetails.get(2).getStatus());
        assertEquals(registerRequest2.getEmail(), userDetails.get(2).getEmail());
        assertEquals(0, userDetails.get(2).getBanCount());
        assertTrue(userDetails.get(2).isOnline());
        assertFalse(userDetails.get(2).isDeleted());
        assertFalse(userDetails.get(2).isSuper());
        assertNull(userDetails.get(2).getTimeBanExit());
    }

    @Test
    void testMadeSuperuser() {
        RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "regular", "regular@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest, UserDtoResponse.class
        );
        int regularUserId = registerResponse.getBody().getId();
        String regularUserCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        ResponseEntity<UserDtoResponse> loginResponse = restTemplate.postForEntity(
                SERVER_URL + "/sessions", loginRequest, UserDtoResponse.class
        );
        int superUserId = loginResponse.getBody().getId();
        String superUserCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, superUserCookie);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<EmptyDtoResponse> madeSuperuserResponse = restTemplate.exchange(
                SERVER_URL + "/users/{user}/super",
                HttpMethod.PUT,
                httpEntity,
                EmptyDtoResponse.class,
                regularUserId
        );
        assertEquals(HttpStatus.OK, madeSuperuserResponse.getStatusCode());

        ResponseEntity<UserDetailsListDtoResponse> usersResponse = restTemplate.exchange(
                SERVER_URL + "/users",
                HttpMethod.GET,
                httpEntity,
                UserDetailsListDtoResponse.class
        );

        UserDetailsDtoResponse regularUser = usersResponse.getBody().getUsers().get(1);
        assertTrue(regularUser.isSuper());
    }

    @Test
    void testMadeSuperuser_requestNotFromSuperuser_shouldReturnBadRequest() {
        RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "regular", "regular@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest, UserDtoResponse.class
        );
        int regularUserId = registerResponse.getBody().getId();
        String regularUserCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        registerRequest = new RegisterUserDtoRequest(
                "regular2", "regular2@email.com", "w3ryStr0nGPa55wD"
        );
        registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest, UserDtoResponse.class
        );
        int regular2UserId = registerResponse.getBody().getId();

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, regularUserCookie);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        try {
            restTemplate.exchange(
                    SERVER_URL + "/users/{user}/super",
                    HttpMethod.PUT,
                    httpEntity,
                    EmptyDtoResponse.class,
                    regular2UserId
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.name()));
        }
    }

    @Test
    void testBanUser() {
        RegisterUserDtoRequest registerRequest1 = new RegisterUserDtoRequest(
                "user1", "user1@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest1, UserDtoResponse.class
        );
        int userId1 = registerResponse.getBody().getId();
        String userCookie1 = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        ResponseEntity<UserDtoResponse> loginResponse = restTemplate.postForEntity(
                SERVER_URL + "/sessions", loginRequest, UserDtoResponse.class
        );
        int superUserId = loginResponse.getBody().getId();
        String superUserCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, superUserCookie);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<EmptyDtoResponse> banResponse = restTemplate.exchange(
                SERVER_URL + "/users/{user}/restrict",
                HttpMethod.POST,
                httpEntity,
                EmptyDtoResponse.class,
                userId1
        );
        assertEquals(HttpStatus.OK, banResponse.getStatusCode());

        ResponseEntity<UserDetailsListDtoResponse> usersResponse = restTemplate.exchange(
                SERVER_URL + "/users",
                HttpMethod.GET,
                httpEntity,
                UserDetailsListDtoResponse.class
        );
        List<UserDetailsDtoResponse> userDetails = usersResponse.getBody().getUsers();
        assertEquals(userId1, userDetails.get(1).getId());
        assertEquals(UserStatus.LIMITED, userDetails.get(1).getStatus());
    }

    @Test
    void testBanUser_requestNotFromSuperuser_shouldReturnBadRequest() {
        RegisterUserDtoRequest registerRequest1 = new RegisterUserDtoRequest(
                "user1", "user1@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest1, UserDtoResponse.class
        );
        int userId1 = registerResponse.getBody().getId();
        String userCookie1 = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        ResponseEntity<UserDtoResponse> loginResponse = restTemplate.postForEntity(
                SERVER_URL + "/sessions", loginRequest, UserDtoResponse.class
        );
        int superUserId = loginResponse.getBody().getId();
        String superUserCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, userCookie1);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        try {
            restTemplate.exchange(
                    SERVER_URL + "/users/{user}/restrict",
                    HttpMethod.POST,
                    httpEntity,
                    EmptyDtoResponse.class,
                    userId1
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.name()));
        }
    }
}
