package net.thumbtack.forums.integration;

import net.thumbtack.forums.dto.requests.forum.CreateForumDtoRequest;
import net.thumbtack.forums.dto.requests.user.LoginUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.UpdatePasswordDtoRequest;
import net.thumbtack.forums.dto.responses.forum.ForumDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumInfoDtoResponse;
import net.thumbtack.forums.dto.responses.user.UserDetailsDtoResponse;
import net.thumbtack.forums.dto.responses.user.UserDetailsListDtoResponse;
import net.thumbtack.forums.dto.responses.user.UserDtoResponse;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.model.enums.UserStatus;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ValidatedRequestFieldName;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class UserControllerIntegrationTest extends BaseIntegrationEnvironment {
    private RestTemplate restTemplate = new RestTemplate();

    @Test
    void testRegisterUser() {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> responseEntity = registerUser(request);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertTrue(responseEntity.getHeaders().containsKey(HttpHeaders.SET_COOKIE));

        final UserDtoResponse response = responseEntity.getBody();
        assertNotNull(response);
        assertEquals(request.getName(), response.getName());
        assertEquals(request.getEmail(), response.getEmail());
        assertNull(response.getSessionToken());
    }

    static Stream<Arguments> registerParams() {
        return Stream.of(
                Arguments.arguments(
                        "0123456789_0123456789_0123456789_0123456789_0123456789",
                        "ahoi@savemail.com", "strong_pass_454", ValidatedRequestFieldName.USERNAME
                ),
                Arguments.arguments("", "ahoi@savemail.com", "strong_pass_454", ValidatedRequestFieldName.USERNAME),
                Arguments.arguments(null, "ahoi@savemail.com", "strong_pass_454", ValidatedRequestFieldName.USERNAME),
                Arguments.arguments("username", "ahoi@savemail.com", "weak", ValidatedRequestFieldName.PASSWORD),
                Arguments.arguments("username", "ahoi@savemail.com", "", ValidatedRequestFieldName.PASSWORD),
                Arguments.arguments("username", "ahoi@savemail.com", null, ValidatedRequestFieldName.PASSWORD)
        );
    }

    @ParameterizedTest
    @MethodSource("registerParams")
    void testRegisterUser_invalidRequestData_shouldReturnBadRequest(
            String username, String email, String password, ValidatedRequestFieldName errorField) {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                username, email, password
        );
        try {
            registerUser(request);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.INVALID_REQUEST_DATA.name()));
            assertTrue(ce.getResponseBodyAsString().contains(errorField.getName()));
        }
    }

    @Test
    void testRegisterUser_registerExistingUser_shouldReturnBadRequest() {
        RegisterUserDtoRequest firstRegisterRequest = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        registerUser(firstRegisterRequest);

        RegisterUserDtoRequest secondRegisterRequest = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        try {
            registerUser(secondRegisterRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_NAME_ALREADY_USED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_NAME_ALREADY_USED.getErrorCauseField()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_NAME_ALREADY_USED.getMessage()));
        }
    }

    @Test
    void testDeleteUser() {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> responseEntity = registerUser(request);
        final String sessionToken = responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final ResponseEntity<EmptyDtoResponse> deleteResponseEntity = deleteUser(sessionToken);
        assertEquals(HttpStatus.OK, deleteResponseEntity.getStatusCode());
        assertNotNull(deleteResponseEntity.getBody());
        assertEquals("{}", deleteResponseEntity.getBody().toString());
    }

    @Test
    void testDeleteUser_noSessionCookieInHeader_shouldReturnBadRequest() {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        registerUser(request);

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
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> responseEntity = registerUser(request);
        final String sessionToken = responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        deleteUser(sessionToken);
        try {
            deleteUser(sessionToken);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.WRONG_SESSION_TOKEN.name()));
        }
    }

    @Test
    void testDeleteUser_userHasModeratedForums_shouldMakeForumsReadOnly() {
        final RegisterUserDtoRequest registerUser = new RegisterUserDtoRequest(
                "User", "user@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> userResponseEntity = registerUser(registerUser);
        final String userToken = userResponseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final RegisterUserDtoRequest registerForumOwner = new RegisterUserDtoRequest(
                "ForumOwner", "forum-owner@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> forumOwnerResponseEntity = registerUser(registerForumOwner);
        final String forumOwnerToken = forumOwnerResponseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final CreateForumDtoRequest createForumRequest1 = new CreateForumDtoRequest(
                "Acme Forum", ForumType.MODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> moderatedForumResponse = createForum(
                createForumRequest1, forumOwnerToken
        );

        final CreateForumDtoRequest createForumRequest2 = new CreateForumDtoRequest(
                "Brooklyn Forum", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> unmoderatedForumResponse = createForum(
                createForumRequest2, forumOwnerToken
        );

        final ResponseEntity<EmptyDtoResponse> deleteResponseEntity = deleteUser(forumOwnerToken);
        assertEquals(HttpStatus.OK, deleteResponseEntity.getStatusCode());
        assertNotNull(deleteResponseEntity.getBody());
        assertEquals("{}", deleteResponseEntity.getBody().toString());

        final ResponseEntity<ForumInfoDtoResponse> moderatedForum = getForum(
                userToken, moderatedForumResponse.getBody().getId()
        );
        assertTrue(moderatedForum.getBody().isReadonly());

        final ResponseEntity<ForumInfoDtoResponse> unmoderatedForum = getForum(
                userToken, unmoderatedForumResponse.getBody().getId()
        );
        assertFalse(unmoderatedForum.getBody().isReadonly());
    }

    @Test
    void testUpdateUserPassword() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> responseEntity = registerUser(registerRequest);
        final String token = responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final UpdatePasswordDtoRequest updateRequest = new UpdatePasswordDtoRequest(
                registerRequest.getName(), registerRequest.getPassword(), "an0th3rStr0ngPa55w0r|)"
        );
        final ResponseEntity<UserDtoResponse> updateResponse = executeRequest(
                SERVER_URL + "/users", HttpMethod.PUT, token,
                updateRequest, UserDtoResponse.class
        );
        assertEquals(HttpStatus.OK, updateResponse.getStatusCode());
        assertNotNull(updateResponse.getBody());
        assertEquals(updateRequest.getName(), updateResponse.getBody().getName());
        assertEquals(registerRequest.getEmail(), updateResponse.getBody().getEmail());
        assertNull(updateResponse.getBody().getSessionToken());
    }

    @Test
    void testUpdateUserPassword_invalidRequestData_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "testUsername", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> responseEntity = registerUser(registerRequest);
        final String cookie = responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final UpdatePasswordDtoRequest updateRequest = new UpdatePasswordDtoRequest(
                registerRequest.getName(), registerRequest.getPassword(), ""
        );
        try {
            executeRequest(
                    SERVER_URL + "/users", HttpMethod.PUT, cookie,
                    updateRequest, UserDtoResponse.class
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ValidatedRequestFieldName.PASSWORD.getName()));
        }
    }

    @Test
    void testGetUsers_requestFromRegularUser_sensitiveFieldsAreNull() {
        final RegisterUserDtoRequest registerRequest1 = new RegisterUserDtoRequest(
                "user1", "user1@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse1 = registerUser(registerRequest1);
        final int userId1 = registerResponse1.getBody().getId();
        String userCookie1 = registerResponse1.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final RegisterUserDtoRequest registerRequest2 = new RegisterUserDtoRequest(
                "user2", "user2@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest2);
        final int userId2 = registerResponse.getBody().getId();

        final ResponseEntity<UserDetailsListDtoResponse> usersResponse = executeRequest(
                SERVER_URL + "/users", HttpMethod.GET, userCookie1,
                null, UserDetailsListDtoResponse.class
        );
        assertEquals(HttpStatus.OK, usersResponse.getStatusCode());
        assertNotNull(usersResponse.getBody());

        final List<UserDetailsDtoResponse> userDetails = usersResponse.getBody().getUsers();
        assertEquals(3, userDetails.size());
        assertEquals("admin", userDetails.get(0).getName());
        assertEquals(UserStatus.FULL.name(), userDetails.get(0).getStatus());
        assertEquals(0, userDetails.get(0).getBanCount());
        assertFalse(userDetails.get(0).isDeleted());
        assertNull(userDetails.get(0).isSuper());
        assertNull(userDetails.get(0).getTimeBanExit());

        assertEquals(userId1, userDetails.get(1).getId());
        assertEquals(UserStatus.FULL.name(), userDetails.get(1).getStatus());
        assertEquals(0, userDetails.get(1).getBanCount());
        assertTrue(userDetails.get(1).isOnline());
        assertFalse(userDetails.get(1).isDeleted());
        assertNull(userDetails.get(1).isSuper());
        assertNull(userDetails.get(1).getEmail());
        assertNull(userDetails.get(1).getTimeBanExit());

        assertEquals(userId2, userDetails.get(2).getId());
        assertEquals(UserStatus.FULL.name(), userDetails.get(2).getStatus());
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
        ResponseEntity<UserDtoResponse> loginResponse = loginUser(loginRequest);
        String superUserCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        RegisterUserDtoRequest registerRequest1 = new RegisterUserDtoRequest(
                "user1", "user1@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest1);
        int userId1 = registerResponse.getBody().getId();

        RegisterUserDtoRequest registerRequest2 = new RegisterUserDtoRequest(
                "user2", "user2@email.com", "w3ryStr0nGPa55wD"
        );
        registerResponse = registerUser(registerRequest2);
        int userId2 = registerResponse.getBody().getId();

        ResponseEntity<UserDetailsListDtoResponse> usersResponse = executeRequest(
                SERVER_URL + "/users",
                HttpMethod.GET,
                superUserCookie,
                null,
                UserDetailsListDtoResponse.class
        );
        assertEquals(HttpStatus.OK, usersResponse.getStatusCode());
        assertNotNull(usersResponse.getBody());

        List<UserDetailsDtoResponse> userDetails = usersResponse.getBody().getUsers();
        assertEquals(3, userDetails.size());
        assertEquals("admin", userDetails.get(0).getName());
        assertEquals(UserStatus.FULL.name(), userDetails.get(0).getStatus());
        assertEquals(0, userDetails.get(0).getBanCount());
        assertTrue(userDetails.get(0).isOnline());
        assertFalse(userDetails.get(0).isDeleted());
        assertTrue(userDetails.get(0).isSuper());
        assertNull(userDetails.get(0).getTimeBanExit());

        assertEquals(userId1, userDetails.get(1).getId());
        assertEquals(UserStatus.FULL.name(), userDetails.get(1).getStatus());
        assertEquals(registerRequest1.getEmail(), userDetails.get(1).getEmail());
        assertEquals(0, userDetails.get(1).getBanCount());
        assertTrue(userDetails.get(1).isOnline());
        assertFalse(userDetails.get(1).isDeleted());
        assertFalse(userDetails.get(1).isSuper());
        assertNull(userDetails.get(1).getTimeBanExit());

        assertEquals(userId2, userDetails.get(2).getId());
        assertEquals(UserStatus.FULL.name(), userDetails.get(2).getStatus());
        assertEquals(registerRequest2.getEmail(), userDetails.get(2).getEmail());
        assertEquals(0, userDetails.get(2).getBanCount());
        assertTrue(userDetails.get(2).isOnline());
        assertFalse(userDetails.get(2).isDeleted());
        assertFalse(userDetails.get(2).isSuper());
        assertNull(userDetails.get(2).getTimeBanExit());
    }

    @Test
    void testMadeSuperuser() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "regular", "regular@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final int regularUserId = registerResponse.getBody().getId();

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        final ResponseEntity<UserDtoResponse> loginResponse = loginUser(loginRequest);
        final String superUserCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final ResponseEntity<EmptyDtoResponse> madeSuperuserResponse = madeSuperuser(superUserCookie, regularUserId);
        assertEquals(HttpStatus.OK, madeSuperuserResponse.getStatusCode());
        assertNotNull(madeSuperuserResponse.getBody());
        assertEquals("{}", madeSuperuserResponse.getBody().toString());

        final ResponseEntity<UserDetailsListDtoResponse> usersResponse = executeRequest(
                SERVER_URL + "/users",
                HttpMethod.GET,
                superUserCookie,
                null,
                UserDetailsListDtoResponse.class
        );
        final UserDetailsDtoResponse regularUser = usersResponse.getBody().getUsers().get(1);
        assertTrue(regularUser.isSuper());
    }

    @Test
    void testMadeSuperuser_requestNotFromSuperuser_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerFirstUserRequest = new RegisterUserDtoRequest(
                "regular", "regular@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerFirstUserResponse = registerUser(registerFirstUserRequest);
        final String firstUserCookie = registerFirstUserResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final RegisterUserDtoRequest registerSecondUserRequest = new RegisterUserDtoRequest(
                "regular2", "regular2@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerSecondUserResponse = registerUser(registerSecondUserRequest);
        final int secondUserId = registerSecondUserResponse.getBody().getId();

        try {
            madeSuperuser(firstUserCookie, secondUserId);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getErrorCauseField()));
        }
    }

    @Test
    void testBanUser() {
        final RegisterUserDtoRequest registerRequest1 = new RegisterUserDtoRequest(
                "user1", "user1@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest1);
        final int userId = registerResponse.getBody().getId();

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        final ResponseEntity<UserDtoResponse> loginResponse = loginUser(loginRequest);
        final String superUserCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final ResponseEntity<EmptyDtoResponse> banResponse = banUser(superUserCookie, userId);
        assertEquals(HttpStatus.OK, banResponse.getStatusCode());
        assertNotNull(banResponse.getBody());
        assertEquals("{}", banResponse.getBody().toString());

        final ResponseEntity<UserDetailsListDtoResponse> usersResponse = executeRequest(
                SERVER_URL + "/users", HttpMethod.GET, superUserCookie,
                null, UserDetailsListDtoResponse.class
        );
        final List<UserDetailsDtoResponse> userDetails = usersResponse.getBody().getUsers();
        assertEquals(userId, userDetails.get(1).getId());
        assertEquals(UserStatus.LIMITED.name(), userDetails.get(1).getStatus());
    }

    @Test
    void testBanUser_requestNotFromSuperuser_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerRequest1 = new RegisterUserDtoRequest(
                "user1", "user1@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest1);
        final int userId1 = registerResponse.getBody().getId();

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        final ResponseEntity<UserDtoResponse> loginResponse = loginUser(loginRequest);
        final String superUserCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        try {
            banUser(superUserCookie, userId1);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getErrorCauseField()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getMessage()));
        }
    }

    @Test
    void testBanUser_tryingToBanSuperuser_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerRequest1 = new RegisterUserDtoRequest(
                "user1", "user1@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest1);
        final int userId1 = registerResponse.getBody().getId();

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        final ResponseEntity<UserDtoResponse> loginResponse = loginUser(loginRequest);
        final String superUserCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        madeSuperuser(superUserCookie, userId1);
        try {
            banUser(superUserCookie, userId1);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getErrorCauseField()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getMessage()));
        }
    }
}
