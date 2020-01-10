package net.thumbtack.forums.integration;

import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.dto.requests.message.CreateCommentDtoRequest;
import net.thumbtack.forums.dto.requests.message.CreateMessageDtoRequest;
import net.thumbtack.forums.dto.responses.message.MessageDtoResponse;
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

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ForumControllerIntegrationTest extends BaseIntegrationEnvironment {
    private RestTemplate restTemplate = new RestTemplate();

    @Test
    void testCreateForum() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final String userCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(createForumRequest, userCookie);
        assertEquals(HttpStatus.OK, createForumResponse.getStatusCode());
        assertNotNull(createForumResponse.getBody());
        assertEquals(createForumRequest.getName(), createForumResponse.getBody().getName());
        assertEquals(createForumRequest.getType(), createForumResponse.getBody().getType());
    }

    @Test
    void testCreateForum_invalidForumName_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final String userCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "", ForumType.UNMODERATED.name()
        );
        try {
            createForum(createForumRequest, userCookie);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.INVALID_REQUEST_DATA.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ValidatedRequestFieldName.FORUM_NAME.getName()));
        }
    }

    @Test
    void testCreateForum_forumNameAlreadyUsed_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerRequest1 = new RegisterUserDtoRequest(
                "UserOne", "UserOne@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse1 = registerUser(registerRequest1);
        final String userCookie1 = registerResponse1.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final CreateForumDtoRequest createForumRequest1 = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );

        final ResponseEntity<ForumDtoResponse> createForumResponse1 = createForum(createForumRequest1, userCookie1);
        assertEquals(HttpStatus.OK, createForumResponse1.getStatusCode());
        assertNotNull(createForumResponse1.getBody());
        assertEquals(createForumRequest1.getName(), createForumResponse1.getBody().getName());

        final RegisterUserDtoRequest registerRequest2 = new RegisterUserDtoRequest(
                "UserTwo", "UserTwo@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse2 = registerUser(registerRequest2);
        final String userCookie2 = registerResponse2.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final CreateForumDtoRequest createForumRequest2 = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        try {
            createForum(createForumRequest2, userCookie2);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NAME_ALREADY_USED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ValidatedRequestFieldName.FORUM_NAME.getName()));
        }
    }

    @Test
    void testGetEmptyForumById() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final String userCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(createForumRequest, userCookie);
        final int forumId = createForumResponse.getBody().getId();

        final ResponseEntity<ForumInfoDtoResponse> forumInfoResponse = getForum(userCookie, forumId);
        assertEquals(HttpStatus.OK, forumInfoResponse.getStatusCode());

        final ForumInfoDtoResponse response = forumInfoResponse.getBody();
        assertNotNull(response);
        assertEquals(createForumResponse.getBody().getId(), response.getId());
        assertEquals(createForumResponse.getBody().getName(), response.getName());
        assertEquals(createForumResponse.getBody().getType(), response.getType());
        assertEquals(registerRequest.getName(), response.getCreatorName());
        assertFalse(response.isReadonly());
        assertEquals(0, response.getMessageCount());
        assertEquals(0, response.getCommentCount());
    }

    @Test
    void testGetForumWithMessages() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final String userCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(createForumRequest, userCookie);
        final int forumId = createForumResponse.getBody().getId();

        final CreateMessageDtoRequest messageRequest1 = new CreateMessageDtoRequest(
                "Subject #1", "Body #1", null, null
        );
        ResponseEntity<MessageDtoResponse> response1 = createMessage(userCookie, forumId, messageRequest1);

        final CreateMessageDtoRequest messageRequest2 = new CreateMessageDtoRequest(
                "Subject #2", "Body #2", null, Arrays.asList("Tag1", "Tag2")
        );
        createMessage(userCookie, forumId, messageRequest2);

        final CreateCommentDtoRequest comment1 = new CreateCommentDtoRequest("Comment #1");
        createComment(userCookie, response1.getBody().getId(), comment1);

        final CreateCommentDtoRequest comment2 = new CreateCommentDtoRequest("Comment #2");
        createComment(userCookie, response1.getBody().getId(), comment2);

        final ResponseEntity<ForumInfoDtoResponse> forumInfoResponse = getForum(userCookie, forumId);
        assertEquals(HttpStatus.OK, forumInfoResponse.getStatusCode());

        final ForumInfoDtoResponse response = forumInfoResponse.getBody();
        assertNotNull(response);
        assertEquals(createForumResponse.getBody().getId(), response.getId());
        assertEquals(createForumResponse.getBody().getName(), response.getName());
        assertEquals(createForumResponse.getBody().getType(), response.getType());
        assertEquals(registerRequest.getName(), response.getCreatorName());
        assertFalse(response.isReadonly());
        assertEquals(2, response.getMessageCount());
        assertEquals(2, response.getCommentCount());
    }

    @Test
    void testGetForumById_userNotFoundByToken_shouldReturnExceptionDto() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final String userCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        createForum(createForumRequest, userCookie);

        final String cookieNotExistedUser = String.format("JAVASESSIONID=%s; HttpOnly", UUID.randomUUID().toString());
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
        ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
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
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.getMessage()));
        }
    }

    @Test
    void testDeleteForum() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final String userCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );

        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(createForumRequest, userCookie);
        final int forumId = createForumResponse.getBody().getId();

        final ResponseEntity<EmptyDtoResponse> deleteResponse = deleteForum(userCookie, forumId);
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        assertNotNull(deleteResponse.getBody());
        assertEquals("{}", deleteResponse.getBody().toString());

        try {
            getForum(userCookie, createForumResponse.getBody().getId());
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.getErrorCauseField()));
        }
    }

    @Test
    void testDeleteForum_forumNotFound_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final String userCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        try {
            deleteForum(userCookie, 132546);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.getMessage()));
        }
    }

    @Test
    void testDeleteForum_notOwnerTryingToDeleteForum_shouldReturnExceptionDto() {
        final RegisterUserDtoRequest forumCreatorRegisterRequest = new RegisterUserDtoRequest(
                "user1", "user1@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse1 = registerUser(forumCreatorRegisterRequest);
        final String forumCreatorCookie = registerResponse1.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final RegisterUserDtoRequest otherUserRegisterRequest = new RegisterUserDtoRequest(
                "user2", "user2@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse2 = registerUser(otherUserRegisterRequest);
        final String otherUserCookie = registerResponse2.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(createForumRequest, forumCreatorCookie);
        try {
            deleteForum(otherUserCookie, createForumResponse.getBody().getId());
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getMessage()));
        }
    }

    @Test
    void testDeleteForum_userPermanentlyBanned_shouldReturnExceptionDto() {
        final RegisterUserDtoRequest forumCreatorRequest = new RegisterUserDtoRequest(
                "ForumCreator", "creator@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(forumCreatorRequest);
        final int forumCreatorId = registerResponse.getBody().getId();
        final String forumCreatorCookie = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(createForumRequest, forumCreatorCookie);

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        final ResponseEntity<UserDtoResponse> loginResponse = loginUser(loginRequest);
        final String superUserCookie = loginResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        banUser(superUserCookie, forumCreatorId);
        banUser(superUserCookie, forumCreatorId);
        banUser(superUserCookie, forumCreatorId);
        banUser(superUserCookie, forumCreatorId);
        banUser(superUserCookie, forumCreatorId);

        try {
            deleteForum(forumCreatorCookie, createForumResponse.getBody().getId());
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_PERMANENTLY_BANNED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_PERMANENTLY_BANNED.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_PERMANENTLY_BANNED.getErrorCauseField()));
        }
    }
}
