package net.thumbtack.forums.integration;

import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.LoginUserDtoRequest;
import net.thumbtack.forums.dto.requests.forum.CreateForumDtoRequest;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.dto.responses.user.UserDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumInfoDtoResponse;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ValidatedRequestFieldName;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ForumControllerIntegrationTest extends BaseIntegrationEnvironment {
    private RestTemplate restTemplate = new RestTemplate();

    @Test
    void testCreateForum() {
        RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest, UserDtoResponse.class
        );
        String userCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, userCookie);
        HttpEntity<Object> httpEntity = new HttpEntity<>(createForumRequest, httpHeaders);

        ResponseEntity<ForumDtoResponse> createForumResponse = restTemplate.exchange(
                SERVER_URL + "/forums", HttpMethod.POST, httpEntity, ForumDtoResponse.class
        );
        assertEquals(HttpStatus.OK, createForumResponse.getStatusCode());
        assertEquals(createForumRequest.getName(), createForumResponse.getBody().getName());
        assertEquals(createForumRequest.getType(), createForumResponse.getBody().getType());
    }

    @Test
    void testCreateForum_invalidForumName_shouldReturnBadRequest() {
        RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest, UserDtoResponse.class
        );
        String userCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "", ForumType.UNMODERATED.name()
        );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, userCookie);
        HttpEntity<Object> httpEntity = new HttpEntity<>(createForumRequest, httpHeaders);

        try {
            restTemplate.exchange(
                    SERVER_URL + "/forums", HttpMethod.POST, httpEntity, ForumDtoResponse.class
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.INVALID_REQUEST_DATA.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ValidatedRequestFieldName.FORUM_NAME.getName()));
        }
    }

    @Test
    void testCreateForum_forumNameAlreadyUsed_shouldReturnBadRequest() {
        RegisterUserDtoRequest registerRequest1 = new RegisterUserDtoRequest(
                "UserOne", "UserOne@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse1 = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest1, UserDtoResponse.class
        );
        String userCookie1 = registerResponse1.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        CreateForumDtoRequest createForumRequest1 = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );

        HttpHeaders createForumsHeaders1 = new HttpHeaders();
        createForumsHeaders1.setContentType(MediaType.APPLICATION_JSON);
        createForumsHeaders1.add(HttpHeaders.COOKIE, userCookie1);
        HttpEntity<Object> createForumEntity1 = new HttpEntity<>(createForumRequest1, createForumsHeaders1);
        ResponseEntity<ForumDtoResponse> createForumResponse1 = restTemplate.exchange(
                SERVER_URL + "/forums", HttpMethod.POST, createForumEntity1, ForumDtoResponse.class
        );
        assertEquals(HttpStatus.OK, createForumResponse1.getStatusCode());
        assertEquals(createForumRequest1.getName(), createForumResponse1.getBody().getName());
        assertEquals(createForumRequest1.getType(), createForumResponse1.getBody().getType());

        RegisterUserDtoRequest registerRequest2 = new RegisterUserDtoRequest(
                "UserTwo", "UserTwo@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse2 = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest2, UserDtoResponse.class
        );
        String userCookie2 = registerResponse2.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        CreateForumDtoRequest createForumRequest2 = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );

        HttpHeaders createForumsHeaders2 = new HttpHeaders();
        createForumsHeaders2.setContentType(MediaType.APPLICATION_JSON);
        createForumsHeaders2.add(HttpHeaders.COOKIE, userCookie1);
        HttpEntity<Object> createForumEntity2 = new HttpEntity<>(createForumRequest1, createForumsHeaders1);
        try {
            restTemplate.exchange(
                    SERVER_URL + "/forums", HttpMethod.POST, createForumEntity2, ForumDtoResponse.class
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NAME_ALREADY_USED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ValidatedRequestFieldName.FORUM_NAME.getName()));
        }
    }

    @Test
    void testGetEmptyForumById() {
        RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest, UserDtoResponse.class
        );
        String userCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, userCookie);
        HttpEntity<Object> httpEntity = new HttpEntity<>(createForumRequest, httpHeaders);

        ResponseEntity<ForumDtoResponse> createForumResponse = restTemplate.exchange(
                SERVER_URL + "/forums", HttpMethod.POST, httpEntity, ForumDtoResponse.class
        );

        ResponseEntity<ForumInfoDtoResponse> forumInfoResponse = restTemplate.exchange(
                SERVER_URL + "/forums/{forum}", HttpMethod.GET,
                httpEntity, ForumInfoDtoResponse.class, createForumResponse.getBody().getId()
        );
        assertEquals(HttpStatus.OK, forumInfoResponse.getStatusCode());

        ForumInfoDtoResponse response = forumInfoResponse.getBody();
        assertEquals(createForumResponse.getBody().getId(), response.getId());
        assertEquals(createForumResponse.getBody().getName(), response.getName());
        assertEquals(createForumResponse.getBody().getType(), response.getType());
        assertEquals(registerRequest.getName(), response.getCreatorName());
        assertFalse(response.isReadonly());
        assertEquals(0, response.getMessageCount());
        assertEquals(0, response.getCommentCount());
    }

    @Test
    void testGetForumById_userNotFound_shouldReturnExceptionDto() {
        RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest, UserDtoResponse.class
        );
        String userCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, userCookie);
        HttpEntity<Object> httpEntity = new HttpEntity<>(createForumRequest, httpHeaders);

        ResponseEntity<ForumDtoResponse> createForumResponse = restTemplate.exchange(
                SERVER_URL + "/forums", HttpMethod.POST, httpEntity, ForumDtoResponse.class
        );

        String cookieNotExistedUser = String.format("JAVASESSIONID=%s; HttpOnly", UUID.randomUUID().toString());
        HttpHeaders strangeUserHttpHeaders = new HttpHeaders();
        strangeUserHttpHeaders.setContentType(MediaType.APPLICATION_JSON);
        strangeUserHttpHeaders.add(HttpHeaders.COOKIE, cookieNotExistedUser);
        HttpEntity<Object> strangeUserHttpEntity = new HttpEntity<>(strangeUserHttpHeaders);

        try {
            restTemplate.exchange(
                    SERVER_URL + "/forums/{forum}", HttpMethod.GET,
                    strangeUserHttpEntity, ForumInfoDtoResponse.class, 123456
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            String res = ce.getResponseBodyAsString();
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.WRONG_SESSION_TOKEN.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.WRONG_SESSION_TOKEN.getMessage()));
        }
    }

    @Test
    void testGetForumById_forumNotFound_shouldReturnBadRequest() {
        RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest, UserDtoResponse.class
        );
        String userCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, userCookie);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        try {
            restTemplate.exchange(
                    SERVER_URL + "/forums/{forum}", HttpMethod.GET,
                    httpEntity, ForumInfoDtoResponse.class, 123456
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.getMessage()));
        }
    }

    @Test
    void testDeleteForum() {
        RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest, UserDtoResponse.class
        );
        String userCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, userCookie);
        HttpEntity<Object> httpEntity = new HttpEntity<>(createForumRequest, httpHeaders);

        ResponseEntity<ForumDtoResponse> createForumResponse = restTemplate.exchange(
                SERVER_URL + "/forums", HttpMethod.POST, httpEntity, ForumDtoResponse.class
        );

        ResponseEntity<EmptyDtoResponse> deleteResponse = restTemplate.exchange(
                SERVER_URL + "/forums/{forum_id}", HttpMethod.DELETE,
                httpEntity, EmptyDtoResponse.class, createForumResponse.getBody().getId()
        );
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        try {
            restTemplate.exchange(
                    SERVER_URL + "/forums/{forum}", HttpMethod.GET,
                    httpEntity, ForumInfoDtoResponse.class, createForumResponse.getBody().getId()
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.getMessage()));
        }
    }

    @Test
    void testDeleteForum_forumNotFound_shouldReturnBadRequest() {
        RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerRequest, UserDtoResponse.class
        );
        String userCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, userCookie);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        try {
            restTemplate.exchange(
                    SERVER_URL + "/forums/{forum_id}", HttpMethod.DELETE,
                    httpEntity, EmptyDtoResponse.class, 123456
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.getMessage()));
        }
    }

    @Test
    void testDeleteForum_notOwnerTryingToDeleteForum_shouldReturnExceptionDto() {
        RegisterUserDtoRequest forumCreatorRegisterRequest = new RegisterUserDtoRequest(
                "user1", "user1@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", forumCreatorRegisterRequest, UserDtoResponse.class
        );
        String forumCreatorCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        RegisterUserDtoRequest otherUserRegisterRequest = new RegisterUserDtoRequest(
                "user2", "user2@email.com", "w3ryStr0nGPa55wD"
        );
        registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", otherUserRegisterRequest, UserDtoResponse.class
        );
        String otherUserCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );

        HttpHeaders forumCreatorHttpHeaders = new HttpHeaders();
        forumCreatorHttpHeaders.setContentType(MediaType.APPLICATION_JSON);
        forumCreatorHttpHeaders.add(HttpHeaders.COOKIE, forumCreatorCookie);
        HttpEntity<Object> forumCreatorHttpEntity = new HttpEntity<>(createForumRequest, forumCreatorHttpHeaders);
        ResponseEntity<ForumDtoResponse> createForumResponse = restTemplate.exchange(
                SERVER_URL + "/forums", HttpMethod.POST, forumCreatorHttpEntity, ForumDtoResponse.class
        );

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.add(HttpHeaders.COOKIE, otherUserCookie);
            HttpEntity<Object> httpEntity = new HttpEntity<>(createForumRequest, httpHeaders);
            restTemplate.exchange(
                    SERVER_URL + "/forums/{forum}", HttpMethod.DELETE,
                    httpEntity, EmptyDtoResponse.class, createForumResponse.getBody().getId()
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getMessage()));
        }
    }

    @Test
    void testDeleteForum_userPermanentlyBanned_shouldReturnExceptionDto() {
        RegisterUserDtoRequest forumCreatorRequest = new RegisterUserDtoRequest(
                "ForumCreator", "creator@email.com", "w3ryStr0nGPa55wD"
        );
        ResponseEntity<UserDtoResponse> registerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", forumCreatorRequest, UserDtoResponse.class
        );
        int forumCreatorId = registerResponse.getBody().getId();
        String forumCreatorCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );

        HttpHeaders forumCreatorHttpHeaders = new HttpHeaders();
        forumCreatorHttpHeaders.setContentType(MediaType.APPLICATION_JSON);
        forumCreatorHttpHeaders.add(HttpHeaders.COOKIE, forumCreatorCookie);
        HttpEntity<Object> forumCreatorHttpEntity = new HttpEntity<>(createForumRequest, forumCreatorHttpHeaders);

        ResponseEntity<ForumDtoResponse> createForumResponse = restTemplate.exchange(
                SERVER_URL + "/forums", HttpMethod.POST, forumCreatorHttpEntity, ForumDtoResponse.class
        );

        LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        ResponseEntity<UserDtoResponse> loginResponse = restTemplate.postForEntity(
                SERVER_URL + "/sessions", loginRequest, UserDtoResponse.class
        );
        int superUserId = loginResponse.getBody().getId();
        String superUserCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        HttpHeaders superuserHttpHeaders = new HttpHeaders();
        superuserHttpHeaders.setContentType(MediaType.APPLICATION_JSON);
        superuserHttpHeaders.add(HttpHeaders.COOKIE, superUserCookie);
        HttpEntity<Object> superuserHttpEntity = new HttpEntity<>(superuserHttpHeaders);
        restTemplate.exchange(
                SERVER_URL + "/users/{user}/restrict", HttpMethod.POST, superuserHttpEntity,
                EmptyDtoResponse.class, forumCreatorId
        );
        restTemplate.exchange(
                SERVER_URL + "/users/{user}/restrict", HttpMethod.POST, superuserHttpEntity,
                EmptyDtoResponse.class, forumCreatorId
        );
        restTemplate.exchange(
                SERVER_URL + "/users/{user}/restrict", HttpMethod.POST, superuserHttpEntity,
                EmptyDtoResponse.class, forumCreatorId
        );
        restTemplate.exchange(
                SERVER_URL + "/users/{user}/restrict", HttpMethod.POST, superuserHttpEntity,
                EmptyDtoResponse.class, forumCreatorId
        );
        restTemplate.exchange(
                SERVER_URL + "/users/{user}/restrict", HttpMethod.POST, superuserHttpEntity,
                EmptyDtoResponse.class, forumCreatorId
        );

        try {
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setContentType(MediaType.APPLICATION_JSON);
            httpHeaders.add(HttpHeaders.COOKIE, forumCreatorCookie);
            HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);
            restTemplate.exchange(
                    SERVER_URL + "/forums/{forum}", HttpMethod.DELETE,
                    httpEntity, EmptyDtoResponse.class, createForumResponse.getBody().getId()
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_PERMANENTLY_BANNED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_PERMANENTLY_BANNED.getMessage()));
        }
    }
}
