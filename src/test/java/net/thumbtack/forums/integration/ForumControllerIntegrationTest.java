package net.thumbtack.forums.integration;

import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.dto.requests.forum.CreateForumDtoRequest;
import net.thumbtack.forums.dto.requests.user.LoginUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.requests.message.CreateMessageDtoRequest;
import net.thumbtack.forums.dto.requests.message.CreateCommentDtoRequest;
import net.thumbtack.forums.dto.responses.forum.ForumDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumInfoDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumInfoListDtoResponse;
import net.thumbtack.forums.dto.responses.user.UserDtoResponse;
import net.thumbtack.forums.dto.responses.message.MessageDtoResponse;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class ForumControllerIntegrationTest extends BaseIntegrationEnvironment {
    @Test
    void testCreateForum() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final String userToken = getSessionTokenFromHeaders(registerResponse);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(createForumRequest, userToken);
        assertEquals(HttpStatus.OK, createForumResponse.getStatusCode());
        assertNotNull(createForumResponse.getBody());
        assertEquals(createForumRequest.getName(), createForumResponse.getBody().getName());
        assertEquals(createForumRequest.getType(), createForumResponse.getBody().getType());
    }

    @Test
    void testCreateForum_forumWithRequestedNameWasDeleted_shouldCreateNewForum() {
        final RegisterUserDtoRequest registerRequest1 = new RegisterUserDtoRequest(
                "UserOne", "UserOne@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse1 = registerUser(registerRequest1);
        final String firstForumCreatorToken = getSessionTokenFromHeaders(registerResponse1);

        final CreateForumDtoRequest createForumRequest1 = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse1 = createForum(
                createForumRequest1, firstForumCreatorToken
        );
        assertEquals(HttpStatus.OK, createForumResponse1.getStatusCode());
        assertNotNull(createForumResponse1.getBody());

        final int forumId1 = createForumResponse1.getBody().getId();
        deleteForum(firstForumCreatorToken, forumId1);

        final RegisterUserDtoRequest registerRequest2 = new RegisterUserDtoRequest(
                "UserTwo", "UserTwo@email.com", "hbklGDkjbkjb2leb345ebV"
        );
        final ResponseEntity<UserDtoResponse> registerResponse2 = registerUser(registerRequest2);
        final String secondForumCreatorToken = getSessionTokenFromHeaders(registerResponse2);

        final CreateForumDtoRequest createForumRequest2 = new CreateForumDtoRequest(
                createForumRequest1.getName(), ForumType.MODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse2 = createForum(
                createForumRequest2, secondForumCreatorToken
        );
        assertEquals(HttpStatus.OK, createForumResponse2.getStatusCode());
        assertNotNull(createForumResponse2.getBody());

        final ForumDtoResponse newForumResponse = createForumResponse2.getBody();
        assertEquals(createForumRequest2.getName(), newForumResponse.getName());
        assertEquals(createForumRequest2.getType(), newForumResponse.getType());

        assertNotEquals(forumId1, newForumResponse.getId());
        assertNotEquals(createForumRequest1.getType(), newForumResponse.getType());
    }

    static Stream<Arguments> createForumInvalidParams() {
        return Stream.of(
                Arguments.arguments(null, ForumType.MODERATED.name(),
                        ValidatedRequestFieldName.FORUM_NAME.getName()
                ),
                Arguments.arguments("", ForumType.UNMODERATED.name(),
                        ValidatedRequestFieldName.FORUM_NAME.getName()
                ),
                Arguments.arguments("ForumName", "MIXED",
                        ValidatedRequestFieldName.FORUM_TYPE.getName()
                ),
                Arguments.arguments("ForumName", "",
                        ValidatedRequestFieldName.FORUM_TYPE.getName()
                ),
                Arguments.arguments("ForumName", null,
                        ValidatedRequestFieldName.FORUM_TYPE.getName()
                )
        );
    }

    @ParameterizedTest
    @MethodSource("createForumInvalidParams")
    void testCreateForum_invalidForumParams_shouldReturnBadRequest(
            String forumName, String forumType, String errorField) {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final String userToken = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(forumName, forumType);
        try {
            createForum(createForumRequest, userToken);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.INVALID_REQUEST_DATA.name()));
            assertTrue(ce.getResponseBodyAsString().contains(errorField));
        }
    }

    @Test
    void testCreateForum_userHaventSession_shouldReturnBadRequestExceptionDto() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final String userToken = getSessionTokenFromHeaders(registerResponse);
        logoutUser(userToken);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        try {
            createForum(createForumRequest, userToken);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getErrorCauseField()));
        }
    }

    @Test
    void testCreateForum_userBanned_shouldReturnBadRequestExceptionDto() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final int userId = registerResponse.getBody().getId();
        final String userToken = getSessionTokenFromHeaders(registerResponse);

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        final ResponseEntity<UserDtoResponse> loginResponse = loginUser(loginRequest);
        final String superUserToken = getSessionTokenFromHeaders(loginResponse);
        banUser(superUserToken, userId);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        try {
            createForum(createForumRequest, userToken);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.getErrorCauseField()));
        }
    }

    @Test
    void testCreateForum_forumNameAlreadyUsed_shouldReturnBadRequestExceptionDto() {
        final RegisterUserDtoRequest registerRequest1 = new RegisterUserDtoRequest(
                "UserOne", "UserOne@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse1 = registerUser(registerRequest1);
        final String userToken1 = getSessionTokenFromHeaders(registerResponse1);

        final CreateForumDtoRequest createForumRequest1 = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse1 = createForum(createForumRequest1, userToken1);
        assertEquals(HttpStatus.OK, createForumResponse1.getStatusCode());
        assertNotNull(createForumResponse1.getBody());
        assertEquals(createForumRequest1.getName(), createForumResponse1.getBody().getName());

        final RegisterUserDtoRequest registerRequest2 = new RegisterUserDtoRequest(
                "UserTwo", "UserTwo@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse2 = registerUser(registerRequest2);
        final String userToken2 = getSessionTokenFromHeaders(registerResponse2);

        final CreateForumDtoRequest createForumRequest2 = new CreateForumDtoRequest(
                createForumRequest1.getName(), ForumType.UNMODERATED.name()
        );
        try {
            createForum(createForumRequest2, userToken2);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NAME_ALREADY_USED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ValidatedRequestFieldName.FORUM_NAME.getName()));
        }
    }

    @Test
    void testGetForumById_forumAreEmpty_shouldReturnInfoWithZeroMessagesAndComments() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final String userToken = getSessionTokenFromHeaders(registerResponse);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(createForumRequest, userToken);
        final int forumId = createForumResponse.getBody().getId();

        final ResponseEntity<ForumInfoDtoResponse> forumInfoResponse = getForum(userToken, forumId);
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
    void testGetForumById_forumHasMessages_shouldReturnInfoWithMessagesAndCommentsCount() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final String userToken = getSessionTokenFromHeaders(registerResponse);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(createForumRequest, userToken);
        final int forumId = createForumResponse.getBody().getId();

        final CreateMessageDtoRequest messageRequest1 = new CreateMessageDtoRequest(
                "Subject #1", "Body #1", null, null
        );
        final ResponseEntity<MessageDtoResponse> response1 = createMessage(userToken, forumId, messageRequest1);

        final CreateMessageDtoRequest messageRequest2 = new CreateMessageDtoRequest(
                "Subject #2", "Body #2", null, Arrays.asList("Tag1", "Tag2")
        );
        createMessage(userToken, forumId, messageRequest2);

        final CreateCommentDtoRequest comment1 = new CreateCommentDtoRequest("Comment #1");
        createComment(userToken, response1.getBody().getId(), comment1);

        final CreateCommentDtoRequest comment2 = new CreateCommentDtoRequest("Comment #2");
        createComment(userToken, response1.getBody().getId(), comment2);

        final ResponseEntity<ForumInfoDtoResponse> forumInfoResponse = getForum(userToken, forumId);
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
        final String userToken = registerResponse.getHeaders().getFirst(HttpHeaders.SET_COOKIE);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(createForumRequest, userToken);

        try {
            final String tokenUserWithoutSession = String.format(
                    "JAVASESSIONID=%s; HttpOnly", UUID.randomUUID().toString()
            );
            getForum(tokenUserWithoutSession, createForumResponse.getBody().getId());
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            String res = ce.getResponseBodyAsString();
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getMessage()));
        }
    }

    @Test
    void testGetForumById_forumNotFound_shouldReturnNotFoundExceptionDto() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final String userToken = getSessionTokenFromHeaders(registerResponse);

        try {
            getForum(userToken, 123456);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.getMessage()));
        }
    }

    @Test
    void testGetForumById_userHaventSession_shouldReturnBadRequestExceptionDto() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final String userToken = getSessionTokenFromHeaders(registerResponse);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(createForumRequest, userToken);
        final int forumId = createForumResponse.getBody().getId();
        logoutUser(userToken);

        try {
            getForum(userToken, forumId);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getErrorCauseField()));
        }
    }

    @Test
    void testDeleteForum() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final String userToken = getSessionTokenFromHeaders(registerResponse);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );

        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(createForumRequest, userToken);
        final int forumId = createForumResponse.getBody().getId();

        final ResponseEntity<EmptyDtoResponse> deleteResponse = deleteForum(userToken, forumId);
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        assertNotNull(deleteResponse.getBody());
        assertEquals("{}", deleteResponse.getBody().toString());

        try {
            getForum(userToken, createForumResponse.getBody().getId());
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.getErrorCauseField()));
        }
    }

    @Test
    void testDeleteForum_forumHasMessages_shouldDeleteForumAndMessages() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(createForumRequest, forumOwnerToken);
        final int forumId = createForumResponse.getBody().getId();

        final CreateMessageDtoRequest messageRequest1 = new CreateMessageDtoRequest(
                "Subject #1", "Body #1", null, null
        );
        final ResponseEntity<MessageDtoResponse> response1 = createMessage(forumOwnerToken, forumId, messageRequest1);
        final int firstMessageId = response1.getBody().getId();

        final CreateMessageDtoRequest messageRequest2 = new CreateMessageDtoRequest(
                "Subject #2", "Body #2", null, Arrays.asList("Tag1", "Tag2")
        );
        final ResponseEntity<MessageDtoResponse> response2 = createMessage(forumOwnerToken, forumId, messageRequest2);
        final int secondMessageId = response2.getBody().getId();

        final CreateCommentDtoRequest comment1 = new CreateCommentDtoRequest("Comment #1");
        createComment(forumOwnerToken, firstMessageId, comment1);

        final CreateCommentDtoRequest comment2 = new CreateCommentDtoRequest("Comment #2");
        final int comment2Id = createComment(forumOwnerToken, firstMessageId, comment2).getBody().getId();

        final CreateCommentDtoRequest comment3 = new CreateCommentDtoRequest("Comment #3");
        createComment(forumOwnerToken, secondMessageId, comment3);

        final CreateCommentDtoRequest comment4 = new CreateCommentDtoRequest("Comment #F");
        createComment(forumOwnerToken, comment2Id, comment4);

        final ResponseEntity<EmptyDtoResponse> deleteResponse = deleteForum(forumOwnerToken, forumId);
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());
        assertNotNull(deleteResponse.getBody());
        assertEquals("{}", deleteResponse.getBody().toString());

        try {
            getForum(forumOwnerToken, forumId);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.getMessage()));
        }

        try {
            getMessage(forumOwnerToken, firstMessageId);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getMessage()));
        }
    }

    @Test
    void testDeleteForum_superuserTryingToDeleteForum_shouldDeleteForum() {
        final RegisterUserDtoRequest forumCreatorRegisterRequest = new RegisterUserDtoRequest(
                "user1", "user1@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(forumCreatorRegisterRequest);
        final String forumCreatorToken = getSessionTokenFromHeaders(registerResponse);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(createForumRequest, forumCreatorToken);
        assertEquals(HttpStatus.OK, createForumResponse.getStatusCode());
        assertNotNull(createForumResponse.getBody());
        final int forumId = createForumResponse.getBody().getId();

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        final ResponseEntity<UserDtoResponse> loginResponse = loginUser(loginRequest);
        final String superUserToken = getSessionTokenFromHeaders(loginResponse);

        final ResponseEntity<EmptyDtoResponse> deleteForumResponse = deleteForum(superUserToken, forumId);
        assertEquals(HttpStatus.OK, deleteForumResponse.getStatusCode());
        assertNotNull(deleteForumResponse.getBody());
        assertEquals("{}", deleteForumResponse.getBody().toString());

        try {
            getForum(forumCreatorToken, forumId);
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
        final String forumCreatorCookie = getSessionTokenFromHeaders(registerResponse1);

        final RegisterUserDtoRequest otherUserRegisterRequest = new RegisterUserDtoRequest(
                "user2", "user2@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse2 = registerUser(otherUserRegisterRequest);
        final String otherUserCookie = getSessionTokenFromHeaders(registerResponse2);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumCreatorCookie
        );
        try {
            deleteForum(otherUserCookie, createForumResponse.getBody().getId());
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getErrorCauseField()));
        }
    }

    @Test
    void testDeleteForum_userHaventSession_shouldReturnBadRequestExceptionDto() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final String userToken = getSessionTokenFromHeaders(registerResponse);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );

        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(createForumRequest, userToken);
        final int forumId = createForumResponse.getBody().getId();
        logoutUser(userToken);

        try {
            deleteForum(userToken, forumId);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getErrorCauseField()));
        }
    }

    @Test
    void testDeleteForum_forumNotFound_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerRequest = new RegisterUserDtoRequest(
                "TestUser", "testuser@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(registerRequest);
        final String userToken = getSessionTokenFromHeaders(registerResponse);

        try {
            deleteForum(userToken, 132546);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.getErrorCauseField()));
        }
    }

    @Test
    void testDeleteForum_userPermanentlyBanned_shouldReturnExceptionDto() {
        final RegisterUserDtoRequest forumCreatorRequest = new RegisterUserDtoRequest(
                "ForumCreator", "creator@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse = registerUser(forumCreatorRequest);
        final int forumCreatorId = registerResponse.getBody().getId();
        final String forumCreatorToken = getSessionTokenFromHeaders(registerResponse);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(createForumRequest, forumCreatorToken);

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        final ResponseEntity<UserDtoResponse> loginResponse = loginUser(loginRequest);
        final String superUserToken = getSessionTokenFromHeaders(loginResponse);

        banUser(superUserToken, forumCreatorId);
        banUser(superUserToken, forumCreatorId);
        banUser(superUserToken, forumCreatorId);
        banUser(superUserToken, forumCreatorId);
        banUser(superUserToken, forumCreatorId);

        try {
            deleteForum(forumCreatorToken, createForumResponse.getBody().getId());
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_PERMANENTLY_BANNED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_PERMANENTLY_BANNED.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_PERMANENTLY_BANNED.getErrorCauseField()));
        }
    }

    @Test
    void testGetForums() {
        final RegisterUserDtoRequest registerRequest1 = new RegisterUserDtoRequest(
                "ForumOwner1", "satori2000@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse1 = registerUser(registerRequest1);
        final String userToken1 = getSessionTokenFromHeaders(registerResponse1);

        final CreateForumDtoRequest createForumRequest1 = new CreateForumDtoRequest(
                "FirstForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse1 = createForum(createForumRequest1, userToken1);
        final int forumId1 = createForumResponse1.getBody().getId();

        final RegisterUserDtoRequest registerRequest2 = new RegisterUserDtoRequest(
                "ForumOwner2", "ownerX@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse2 = registerUser(registerRequest2);
        final String userToken2 = getSessionTokenFromHeaders(registerResponse2);

        final CreateForumDtoRequest createForumRequest2 = new CreateForumDtoRequest(
                "SecondForumName", ForumType.MODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse2 = createForum(createForumRequest2, userToken2);
        final int forumId2 = createForumResponse2.getBody().getId();

        final CreateMessageDtoRequest messageRequest1 = new CreateMessageDtoRequest(
                "Subject #1", "Body #1", null, null
        );
        final ResponseEntity<MessageDtoResponse> response1 = createMessage(userToken1, forumId1, messageRequest1);

        final CreateMessageDtoRequest messageRequest2 = new CreateMessageDtoRequest(
                "Subject #2", "Body #2", null, null
        );
        final ResponseEntity<MessageDtoResponse> response2 = createMessage(userToken2, forumId2, messageRequest2);

        final CreateCommentDtoRequest comment1 = new CreateCommentDtoRequest("Comment #1");
        createComment(userToken1, response1.getBody().getId(), comment1);

        final ResponseEntity<ForumInfoListDtoResponse> forumsListResponse = getForums(userToken1);
        assertEquals(HttpStatus.OK, forumsListResponse.getStatusCode());
        assertNotNull(forumsListResponse.getBody());

        final List<ForumInfoDtoResponse> forumsList = forumsListResponse.getBody().getForums();
        assertEquals(2, forumsList.size());

        final ForumInfoDtoResponse forum1 = forumsList.get(0);
        assertEquals(createForumResponse1.getBody().getId(), forum1.getId());
        assertEquals(createForumResponse1.getBody().getName(), forum1.getName());
        assertEquals(createForumResponse1.getBody().getType(), forum1.getType());
        assertEquals(registerRequest1.getName(), forum1.getCreatorName());
        assertFalse(forum1.isReadonly());
        assertEquals(1, forum1.getMessageCount());
        assertEquals(1, forum1.getCommentCount());

        final ForumInfoDtoResponse forum2 = forumsList.get(1);
        assertEquals(createForumResponse2.getBody().getId(), forum2.getId());
        assertEquals(createForumResponse2.getBody().getName(), forum2.getName());
        assertEquals(createForumResponse2.getBody().getType(), forum2.getType());
        assertEquals(registerRequest2.getName(), forum2.getCreatorName());
        assertFalse(forum2.isReadonly());
        assertEquals(1, forum2.getMessageCount());
        assertEquals(0, forum2.getCommentCount());
    }

    @Test
    void testGetForums_noForumsInServer_shouldReturnEmptyList() {
        final RegisterUserDtoRequest registerRequest1 = new RegisterUserDtoRequest(
                "ForumOwner1", "ForumOwner1999@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse1 = registerUser(registerRequest1);
        final String userToken1 = getSessionTokenFromHeaders(registerResponse1);

        final RegisterUserDtoRequest registerRequest2 = new RegisterUserDtoRequest(
                "ForumOwner2", "ForumOwner2000@email.com", "w3ryStr0nGPa55wD"
        );
        registerUser(registerRequest2);

        final ResponseEntity<ForumInfoListDtoResponse> forumsListResponse = getForums(userToken1);
        assertEquals(HttpStatus.OK, forumsListResponse.getStatusCode());
        assertNotNull(forumsListResponse.getBody());

        final List<ForumInfoDtoResponse> forumsList = forumsListResponse.getBody().getForums();
        assertEquals(0, forumsList.size());
    }

    @Test
    void testGetForums_userHaventSession_shouldReturnBadRequestExceptionDto() {
        final RegisterUserDtoRequest registerRequest1 = new RegisterUserDtoRequest(
                "ForumOwner1", "satori2000@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse1 = registerUser(registerRequest1);
        final String userToken1 = getSessionTokenFromHeaders(registerResponse1);

        final CreateForumDtoRequest createForumRequest1 = new CreateForumDtoRequest(
                "FirstForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse1 = createForum(createForumRequest1, userToken1);
        final int forumId1 = createForumResponse1.getBody().getId();

        final RegisterUserDtoRequest registerRequest2 = new RegisterUserDtoRequest(
                "ForumOwner2", "ownerX@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerResponse2 = registerUser(registerRequest2);
        final String userToken2 = getSessionTokenFromHeaders(registerResponse2);

        final CreateForumDtoRequest createForumRequest2 = new CreateForumDtoRequest(
                "SecondForumName", ForumType.MODERATED.name()
        );
        createForum(createForumRequest2, userToken2);

        final CreateMessageDtoRequest messageRequest1 = new CreateMessageDtoRequest(
                "Subject #1", "Body #1", null, null
        );
        createMessage(userToken1, forumId1, messageRequest1);

        logoutUser(userToken1);
        try {
            getForums(userToken1);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getErrorCauseField()));
        }
    }
}
