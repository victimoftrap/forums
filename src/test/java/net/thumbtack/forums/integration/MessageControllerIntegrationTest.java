package net.thumbtack.forums.integration;

import net.thumbtack.forums.dto.responses.message.*;
import net.thumbtack.forums.model.enums.*;
import net.thumbtack.forums.dto.requests.message.*;
import net.thumbtack.forums.dto.requests.user.LoginUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.requests.forum.CreateForumDtoRequest;
import net.thumbtack.forums.dto.responses.user.UserDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumDtoResponse;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.exception.ErrorCode;

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
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class MessageControllerIntegrationTest extends BaseIntegrationEnvironment {
    static Stream<Arguments> userAndForumParams() {
        return Stream.of(
                Arguments.arguments(ForumType.MODERATED.name(), true, MessageState.PUBLISHED.name()),
                Arguments.arguments(ForumType.UNMODERATED.name(), true, MessageState.PUBLISHED.name()),
                Arguments.arguments(ForumType.UNMODERATED.name(), false, MessageState.PUBLISHED.name()),
                Arguments.arguments(ForumType.MODERATED.name(), false, MessageState.UNPUBLISHED.name())
        );
    }

    @ParameterizedTest
    @MethodSource("userAndForumParams")
    void testCreateMessage(
            String forumType, boolean isMessageCreatesForumOwner, String expectedMessageState) {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerForumOwnerResponse = registerUser(registerForumOwnerRequest);
        final String forumOwnerToken = getSessionTokenFromHeaders(registerForumOwnerResponse);

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerMessageCreatorResponse = registerUser(
                registerMessageCreatorRequest
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerMessageCreatorResponse);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest("ForumName", forumType);
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        final int forumId = createForumResponse.getBody().getId();

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                isMessageCreatesForumOwner ? forumOwnerToken : messageCreatorToken,
                forumId, createMessageRequest
        );
        assertEquals(HttpStatus.OK, createMessageResponse.getStatusCode());
        assertNotNull(createMessageResponse.getBody());
        assertEquals(expectedMessageState, createMessageResponse.getBody().getState());
    }

    @Test
    void testCreateMessage_userBanned_shouldReturnBadRequestExceptionDto() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerForumOwnerResponse = registerUser(registerForumOwnerRequest);
        final String forumOwnerToken = getSessionTokenFromHeaders(registerForumOwnerResponse);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        final int forumId = createForumResponse.getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerMessageCreatorResponse = registerUser(
                registerMessageCreatorRequest
        );
        final int messageCreatorId = registerMessageCreatorResponse.getBody().getId();
        final String messageCreatorToken = getSessionTokenFromHeaders(registerMessageCreatorResponse);

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        final ResponseEntity<UserDtoResponse> loginResponse = loginUser(loginRequest);
        final String superUserToken = getSessionTokenFromHeaders(loginResponse);
        banUser(superUserToken, messageCreatorId);

        try {
            final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                    "Subject", "Message Body",
                    MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
            );
            createMessage(messageCreatorToken, forumId, createMessageRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.getErrorCauseField()));
        }
    }
    @Test
    void testCreateMessage_noUserSession_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        final int forumId = createForumResponse.getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

       logoutUser(messageCreatorToken);
        try {
            final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                    "Subject", "Message Body",
                    MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
            );
            createMessage(messageCreatorToken, forumId, createMessageRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getErrorCauseField()));
        }
    }

    @Test
    void testCreateMessage_forumNotFound_shouldReturnNotFound() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerForumOwnerResponse = registerUser(registerForumOwnerRequest);
        final String forumOwnerToken = getSessionTokenFromHeaders(registerForumOwnerResponse);

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerMessageCreatorResponse = registerUser(
                registerMessageCreatorRequest
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerMessageCreatorResponse);

        deleteUser(forumOwnerToken);
        try {
            final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                    "Subject", "Message Body",
                    MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
            );
            createMessage(messageCreatorToken, 48151623, createMessageRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_NOT_FOUND.getErrorCauseField()));
        }
    }

    @Test
    void testCreateMessage_forumReadOnly_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerForumOwnerResponse = registerUser(registerForumOwnerRequest);
        final String forumOwnerToken = getSessionTokenFromHeaders(registerForumOwnerResponse);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        final int forumId = createForumResponse.getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerMessageCreatorResponse = registerUser(
                registerMessageCreatorRequest
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerMessageCreatorResponse);

        deleteUser(forumOwnerToken);
        try {
            final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                    "Subject", "Message Body",
                    MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
            );
            createMessage(messageCreatorToken, forumId, createMessageRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_READ_ONLY.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_READ_ONLY.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_READ_ONLY.getErrorCauseField()));
        }
    }

    @ParameterizedTest
    @MethodSource("userAndForumParams")
    void testCreateComment_differentCreatorsAndForumType_shouldCreateCommentWithDifferentState(
            String forumType, boolean isMessageCreatesForumOwner, String expectedMessageState) {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                new CreateForumDtoRequest("ForumName", forumType), forumOwnerToken
        );
        final int forumId = createForumResponse.getBody().getId();

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                forumOwnerToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        final ResponseEntity<MessageDtoResponse> createCommentResponse = createComment(
                isMessageCreatesForumOwner ? forumOwnerToken : messageCreatorToken,
                messageId, new CreateCommentDtoRequest("Comment #1")
        );
        assertEquals(HttpStatus.OK, createCommentResponse.getStatusCode());
        assertNotNull(createCommentResponse.getBody());
        assertEquals(expectedMessageState, createCommentResponse.getBody().getState());
    }

    @Test
    void testCreateComment_userBanned_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body", null, null
        );
        final int messageId = createMessage(messageCreatorToken, forumId, createMessageRequest).getBody().getId();

        final RegisterUserDtoRequest registerCommentCreator = new RegisterUserDtoRequest(
                "CommentCreator", "CommentCreator@email.com", "oi66uWnk9zJ"
        );
        final ResponseEntity<UserDtoResponse> registerCommentCreatorResponse = registerUser(registerCommentCreator);
        final String commentCreatorToken = getSessionTokenFromHeaders(registerCommentCreatorResponse);
        final int commentCreatorId = registerCommentCreatorResponse.getBody().getId();

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        final ResponseEntity<UserDtoResponse> loginResponse = loginUser(loginRequest);
        final String superUserToken = getSessionTokenFromHeaders(loginResponse);
        banUser(superUserToken, commentCreatorId);

        try {
            final CreateCommentDtoRequest createCommentRequest = new CreateCommentDtoRequest("Comment #1");
            createComment(commentCreatorToken, messageId, createCommentRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.getErrorCauseField()));
        }
    }

    @Test
    void testCreateComment_parentMessageNotPublished_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerForumOwnerResponse = registerUser(registerForumOwnerRequest);
        final String forumOwnerToken = getSessionTokenFromHeaders(registerForumOwnerResponse);
        assertEquals(HttpStatus.OK, registerForumOwnerResponse.getStatusCode());

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        assertEquals(HttpStatus.OK, createForumResponse.getStatusCode());
        assertNotNull(createForumResponse.getBody());
        final int forumId = createForumResponse.getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerMessageCreatorResponse = registerUser(
                registerMessageCreatorRequest
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerMessageCreatorResponse);
        assertEquals(HttpStatus.OK, registerMessageCreatorResponse.getStatusCode());

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(HttpStatus.OK, createMessageResponse.getStatusCode());
        assertNotNull(createMessageResponse.getBody());
        assertEquals(MessageState.UNPUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        try {
            final CreateCommentDtoRequest createCommentRequest = new CreateCommentDtoRequest("Comment #1");
            createComment(forumOwnerToken, messageId, createCommentRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_PUBLISHED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_PUBLISHED.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_PUBLISHED.getErrorCauseField()));
        }
    }

    @Test
    void testDeleteMessage_itsRootMessageWithoutComments_shouldDelete() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerForumOwnerResponse = registerUser(registerForumOwnerRequest);
        final String forumOwnerToken = getSessionTokenFromHeaders(registerForumOwnerResponse);
        assertEquals(HttpStatus.OK, registerForumOwnerResponse.getStatusCode());

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        assertEquals(HttpStatus.OK, createForumResponse.getStatusCode());
        assertNotNull(createForumResponse.getBody());
        final int forumId = createForumResponse.getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerMessageCreatorResponse = registerUser(
                registerMessageCreatorRequest
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerMessageCreatorResponse);
        assertEquals(HttpStatus.OK, registerMessageCreatorResponse.getStatusCode());

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(HttpStatus.OK, createMessageResponse.getStatusCode());
        assertNotNull(createMessageResponse.getBody());
        final int messageId = createMessageResponse.getBody().getId();

        final ResponseEntity<EmptyDtoResponse> deleteMessageResponse = deleteMessage(messageCreatorToken, messageId);
        assertEquals(HttpStatus.OK, deleteMessageResponse.getStatusCode());
        assertNotNull(deleteMessageResponse.getBody());
        assertEquals("{}", deleteMessageResponse.getBody().toString());

        try {
            getMessage(messageCreatorToken, messageId);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getErrorCauseField()));
        }
    }

    @Test
    void testDeleteMessage_itsComment_shouldDelete() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerForumOwnerResponse = registerUser(registerForumOwnerRequest);
        final String forumOwnerToken = getSessionTokenFromHeaders(registerForumOwnerResponse);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        final int forumId = createForumResponse.getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerMessageCreatorResponse = registerUser(
                registerMessageCreatorRequest
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerMessageCreatorResponse);
        assertEquals(HttpStatus.OK, registerMessageCreatorResponse.getStatusCode());

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(HttpStatus.OK, createMessageResponse.getStatusCode());
        assertNotNull(createMessageResponse.getBody());
        assertEquals(MessageState.PUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        final CreateCommentDtoRequest createCommentRequest = new CreateCommentDtoRequest("Comment #1");
        final ResponseEntity<MessageDtoResponse> createCommentResponse = createComment(
                forumOwnerToken, messageId, createCommentRequest
        );
        assertEquals(HttpStatus.OK, createCommentResponse.getStatusCode());
        assertNotNull(createCommentResponse.getBody());
        assertEquals(MessageState.PUBLISHED.name(), createCommentResponse.getBody().getState());
        final int commentId = createCommentResponse.getBody().getId();

        try {
            deleteMessage(forumOwnerToken, commentId);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_HAS_COMMENTS.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_HAS_COMMENTS.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_HAS_COMMENTS.getErrorCauseField()));
        }
    }

    @Test
    void testDeleteMessage_messageUnpublished_shouldDeleteMessage() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerForumOwnerResponse = registerUser(registerForumOwnerRequest);
        final String forumOwnerToken = getSessionTokenFromHeaders(registerForumOwnerResponse);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        final int forumId = createForumResponse.getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerMessageCreatorResponse = registerUser(
                registerMessageCreatorRequest
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerMessageCreatorResponse);
        assertEquals(HttpStatus.OK, registerMessageCreatorResponse.getStatusCode());

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(HttpStatus.OK, createMessageResponse.getStatusCode());
        assertNotNull(createMessageResponse.getBody());
        assertEquals(MessageState.UNPUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        final ResponseEntity<EmptyDtoResponse> deleteMessageResponse = deleteMessage(messageCreatorToken, messageId);
        assertEquals(HttpStatus.OK, deleteMessageResponse.getStatusCode());
        assertNotNull(deleteMessageResponse.getBody());
        assertEquals("{}", deleteMessageResponse.getBody().toString());
    }

    @Test
    void testDeleteMessage_notMessageCreatorDeletingMessage_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerForumOwnerResponse = registerUser(registerForumOwnerRequest);
        final String forumOwnerToken = getSessionTokenFromHeaders(registerForumOwnerResponse);
        assertEquals(HttpStatus.OK, registerForumOwnerResponse.getStatusCode());

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        assertEquals(HttpStatus.OK, createForumResponse.getStatusCode());
        assertNotNull(createForumResponse.getBody());
        final int forumId = createForumResponse.getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerMessageCreatorResponse = registerUser(
                registerMessageCreatorRequest
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerMessageCreatorResponse);
        assertEquals(HttpStatus.OK, registerMessageCreatorResponse.getStatusCode());

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(HttpStatus.OK, createMessageResponse.getStatusCode());
        assertNotNull(createMessageResponse.getBody());
        final int messageId = createMessageResponse.getBody().getId();

        final RegisterUserDtoRequest registerGoofyUser = new RegisterUserDtoRequest(
                "Goofy", "goofy@fool.com", "hjhvjwhbehvbbv"
        );
        final ResponseEntity<UserDtoResponse> registerGoofyUserResponse = registerUser(registerGoofyUser);
        final String goofyUserToken = getSessionTokenFromHeaders(registerGoofyUserResponse);
        try {
            deleteMessage(goofyUserToken, messageId);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getErrorCauseField()));
        }
    }

    @Test
    void testDeleteMessage_messageNotFound_shouldReturnNotFound() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerForumOwnerResponse = registerUser(registerForumOwnerRequest);
        final String forumOwnerToken = getSessionTokenFromHeaders(registerForumOwnerResponse);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        createForum(createForumRequest, forumOwnerToken);

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerMessageCreatorResponse = registerUser(
                registerMessageCreatorRequest
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerMessageCreatorResponse);

        try {
            deleteMessage(messageCreatorToken, 48151623);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getErrorCauseField()));
        }
    }

    @Test
    void testDeleteMessage_messageHasPublishedComments_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerForumOwnerResponse = registerUser(registerForumOwnerRequest);
        final String forumOwnerToken = getSessionTokenFromHeaders(registerForumOwnerResponse);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        final int forumId = createForumResponse.getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerMessageCreatorResponse = registerUser(
                registerMessageCreatorRequest
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerMessageCreatorResponse);
        assertEquals(HttpStatus.OK, registerMessageCreatorResponse.getStatusCode());

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(HttpStatus.OK, createMessageResponse.getStatusCode());
        assertNotNull(createMessageResponse.getBody());
        assertEquals(MessageState.PUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        final CreateCommentDtoRequest createCommentRequest = new CreateCommentDtoRequest("Comment #1");
        final ResponseEntity<MessageDtoResponse> createCommentResponse = createComment(
                forumOwnerToken, messageId, createCommentRequest
        );
        assertEquals(HttpStatus.OK, createCommentResponse.getStatusCode());
        assertNotNull(createCommentResponse.getBody());
        assertEquals(MessageState.PUBLISHED.name(), createCommentResponse.getBody().getState());

        try {
            deleteMessage(messageCreatorToken, messageId);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_HAS_COMMENTS.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_HAS_COMMENTS.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_HAS_COMMENTS.getErrorCauseField()));
        }
    }

    @Test
    void testDeleteMessage_messageHasUnpublishedComments_shouldDelete() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        final int forumId = createForumResponse.getBody().getId();

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                forumOwnerToken, forumId, createMessageRequest
        );
        assertEquals(MessageState.PUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        final RegisterUserDtoRequest registerCommentCreatorRequest = new RegisterUserDtoRequest(
                "CommentCreator", "CommentCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String commentCreatorToken = getSessionTokenFromHeaders(registerUser(registerCommentCreatorRequest));

        final CreateCommentDtoRequest createCommentRequest = new CreateCommentDtoRequest("Comment #1");
        final ResponseEntity<MessageDtoResponse> createCommentResponse = createComment(
                commentCreatorToken, messageId, createCommentRequest
        );
        assertEquals(HttpStatus.OK, createCommentResponse.getStatusCode());
        assertNotNull(createCommentResponse.getBody());
        assertEquals(MessageState.UNPUBLISHED.name(), createCommentResponse.getBody().getState());

        final ResponseEntity<EmptyDtoResponse> deleteResponse = deleteMessage(forumOwnerToken, messageId);
        assertEquals(HttpStatus.OK, deleteResponse.getStatusCode());

        try {
            getMessage(commentCreatorToken, messageId);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getErrorCauseField()));
        }
    }

    @Test
    void testDeleteMessage_forumReadOnly_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        final int forumId = createForumResponse.getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        publishMessage(forumOwnerToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.YES.name()));
        deleteUser(forumOwnerToken);

        try {
            deleteMessage(messageCreatorToken, messageId);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_READ_ONLY.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_READ_ONLY.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_READ_ONLY.getErrorCauseField()));
        }
    }

    @Test
    void testDeleteMessage_noUserSession_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        final int forumId = createForumResponse.getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        publishMessage(forumOwnerToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.YES.name()));
        logoutUser(messageCreatorToken);
        try {
            deleteMessage(messageCreatorToken, messageId);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getErrorCauseField()));
        }
    }

    @Test
    void testEditMessage_messagePublished_shouldAddNewHistory() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "Fragile", "fragile@email.com", "hgfxh9ckjlDF5e"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Fragile", "I'm Fragile, but not that fragile", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        final EditMessageOrCommentDtoRequest editMessageRequest = new EditMessageOrCommentDtoRequest(
                "Don't be so quick to walk away"
        );
        final ResponseEntity<EditMessageOrCommentDtoResponse> editMessageResponse = editMessage(
                messageCreatorToken, messageId, editMessageRequest
        );
        assertEquals(HttpStatus.OK, editMessageResponse.getStatusCode());
        assertNotNull(editMessageResponse.getBody());
        assertEquals(MessageState.PUBLISHED.name(), editMessageResponse.getBody().getState());

        final ResponseEntity<MessageInfoDtoResponse> messageWithLatestHistory = getMessage(messageCreatorToken, messageId);
        assertEquals(HttpStatus.OK, messageWithLatestHistory.getStatusCode());
        assertNotNull(messageWithLatestHistory.getBody());
        assertEquals(editMessageRequest.getBody(), messageWithLatestHistory.getBody().getBody().get(0));

        final ResponseEntity<MessageInfoDtoResponse> messageWithAllHistory = getMessage(
                messageCreatorToken, messageId,
                true, false, false, MessageOrder.DESC.name()
        );
        assertEquals(HttpStatus.OK, messageWithAllHistory.getStatusCode());
        assertNotNull(messageWithAllHistory.getBody());
        assertEquals(2, messageWithAllHistory.getBody().getBody().size());
        assertEquals(editMessageRequest.getBody(), messageWithAllHistory.getBody().getBody().get(0));
        assertEquals(createMessageRequest.getBody(), messageWithAllHistory.getBody().getBody().get(1));
    }

    @Test
    void testEditMessage_messageUnpublished_shouldReplaceHistory() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "Fragile", "fragile@email.com", "hgfxh9ckjlDF5e"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Fragile", "I'm Fragile, but not that fragile", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertNotNull(createMessageResponse.getBody());
        assertEquals(MessageState.UNPUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        final EditMessageOrCommentDtoRequest editMessageRequest = new EditMessageOrCommentDtoRequest(
                "Don't be so quick to walk away"
        );
        final ResponseEntity<EditMessageOrCommentDtoResponse> editMessageResponse = editMessage(
                messageCreatorToken, messageId, editMessageRequest
        );
        assertEquals(HttpStatus.OK, editMessageResponse.getStatusCode());
        assertNotNull(editMessageResponse.getBody());
        assertEquals(MessageState.UNPUBLISHED.name(), editMessageResponse.getBody().getState());

        final ResponseEntity<MessageInfoDtoResponse> messageWithLatestHistory = getMessage(forumOwnerToken, messageId);
        assertEquals(HttpStatus.OK, messageWithLatestHistory.getStatusCode());
        assertNotNull(messageWithLatestHistory.getBody());
        assertEquals(0, messageWithLatestHistory.getBody().getBody().size());

        final ResponseEntity<MessageInfoDtoResponse> messageWithAllHistory = getMessage(
                forumOwnerToken, messageId,
                true, false, true, MessageOrder.DESC.name()
        );
        assertEquals(HttpStatus.OK, messageWithAllHistory.getStatusCode());
        assertNotNull(messageWithAllHistory.getBody());
        assertEquals(1, messageWithAllHistory.getBody().getBody().size());
        assertTrue(messageWithAllHistory.getBody().getBody().get(0).contains(editMessageRequest.getBody()));
        assertTrue(messageWithAllHistory.getBody().getBody().get(0).contains("[UNPUBLISHED]"));
    }

    @Test
    void testEditMessage_userBanned_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "Fragile", "fragile@email.com", "hgfxh9ckjlDF5e"
        );
        final ResponseEntity<UserDtoResponse> messageCreatorResponse = registerUser(registerMessageCreatorRequest);
        final String messageCreatorToken = getSessionTokenFromHeaders(messageCreatorResponse);
        final int messageCreatorId = messageCreatorResponse.getBody().getId();

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Fragile", "I'm Fragile, but not that fragile", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        final ResponseEntity<UserDtoResponse> loginResponse = loginUser(loginRequest);
        final String superUserToken = getSessionTokenFromHeaders(loginResponse);
        banUser(superUserToken, messageCreatorId);

        try {
            final EditMessageOrCommentDtoRequest editMessageRequest = new EditMessageOrCommentDtoRequest(
                    "Don't be so quick to walk away"
            );
            editMessage(messageCreatorToken, messageId, editMessageRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.getErrorCauseField()));
        }
    }

    @Test
    void testEditMessage_notMessageCreatorEditingMessage_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "Fragile", "fragile@email.com", "hgfxh9ckjlDF5e"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Fragile", "I'm Fragile, but not that fragile", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        final RegisterUserDtoRequest registerGoofyUser = new RegisterUserDtoRequest(
                "Goofy", "goofy@fool.com", "hjhvjwhbehvbbv"
        );
        final ResponseEntity<UserDtoResponse> registerGoofyUserResponse = registerUser(registerGoofyUser);
        final String goofyUserToken = getSessionTokenFromHeaders(registerGoofyUserResponse);

        try {
            final EditMessageOrCommentDtoRequest editMessageRequest = new EditMessageOrCommentDtoRequest(
                    "Don't be so quick to walk away"
            );
            editMessage(goofyUserToken, messageId, editMessageRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getErrorCauseField()));
        }
    }

    @Test
    void testEditMessage_forumReadOnly_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "Fragile", "fragile@email.com", "hgfxh9ckjlDF5e"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Fragile", "I'm Fragile, but not that fragile", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        publishMessage(forumOwnerToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.YES.name()));
        getMessage(messageCreatorToken, messageId);

        deleteUser(forumOwnerToken);
        try {
            final EditMessageOrCommentDtoRequest editMessageRequest = new EditMessageOrCommentDtoRequest(
                    "Don't be so quick to walk away"
            );
            editMessage(messageCreatorToken, messageId, editMessageRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_READ_ONLY.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_READ_ONLY.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_READ_ONLY.getErrorCauseField()));
        }
    }

    @Test
    void testEditMessage_noUserSession_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "Fragile", "fragile@email.com", "hgfxh9ckjlDF5e"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Fragile", "I'm Fragile, but not that fragile", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        publishMessage(forumOwnerToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.YES.name()));
        getMessage(messageCreatorToken, messageId);

        logoutUser(messageCreatorToken);
        try {
            final EditMessageOrCommentDtoRequest editMessageRequest = new EditMessageOrCommentDtoRequest(
                    "Don't be so quick to walk away"
            );
            editMessage(messageCreatorToken, messageId, editMessageRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getErrorCauseField()));
        }
    }

    @Test
    void testEditMessage_messageNotFound_shouldReturnNotFound() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        try {
            final EditMessageOrCommentDtoRequest editMessageRequest = new EditMessageOrCommentDtoRequest(
                    "Don't be so quick to walk away"
            );
            editMessage(forumOwnerToken, 123456, editMessageRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getErrorCauseField()));
        }
    }

    @Test
    void testChangeMessagePriority() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "Fragile", "fragile@email.com", "hgfxh9ckjlDF5e"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Fragile", "I'm Fragile, but not that fragile", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        final ResponseEntity<MessageInfoDtoResponse> getMessageWithDefaultPriority = getMessage(
                messageCreatorToken, messageId
        );
        assertEquals(HttpStatus.OK, getMessageWithDefaultPriority.getStatusCode());
        assertNotNull(getMessageWithDefaultPriority.getBody());
        assertEquals(MessagePriority.NORMAL.name(), getMessageWithDefaultPriority.getBody().getPriority());

        final ResponseEntity<EmptyDtoResponse> changePriorityResponse = changePriority(
                messageCreatorToken, messageId,
                new ChangeMessagePriorityDtoRequest(MessagePriority.HIGH.name())
        );
        assertEquals(HttpStatus.OK, changePriorityResponse.getStatusCode());
        assertNotNull(changePriorityResponse.getBody());
        assertEquals("{}", changePriorityResponse.getBody().toString());

        final ResponseEntity<MessageInfoDtoResponse> getMessageWithNewPriority = getMessage(
                messageCreatorToken, messageId
        );
        assertEquals(HttpStatus.OK, getMessageWithNewPriority.getStatusCode());
        assertNotNull(getMessageWithNewPriority.getBody());
        assertEquals(MessagePriority.HIGH.name(), getMessageWithNewPriority.getBody().getPriority());
    }

    @Test
    void testChangeMessagePriority_userBanned_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "DOM", ForumType.MODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "Sam", "sam@bridges.com", "brhbwerDAreb4"
        );
        final ResponseEntity<UserDtoResponse> messageCreatorResponse = registerUser(registerMessageCreatorRequest);
        final String messageCreatorToken = getSessionTokenFromHeaders(messageCreatorResponse);
        final int messageCreatorId = messageCreatorResponse.getBody().getId();

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Fragile", "I'm Fragile, but not that fragile", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        publishMessage(forumOwnerToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.YES.name()));

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        final ResponseEntity<UserDtoResponse> loginResponse = loginUser(loginRequest);
        final String superUserToken = getSessionTokenFromHeaders(loginResponse);
        banUser(superUserToken, messageCreatorId);

        try {
            changePriority(
                    messageCreatorToken, messageId,
                    new ChangeMessagePriorityDtoRequest(MessagePriority.HIGH.name())
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.getErrorCauseField()));
        }
    }

    @Test
    void testChangeMessagePriority_messageNotFound_shouldReturnNotFound() {
        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "Fragile", "fragile@email.com", "hgfxh9ckjlDF5e"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        try {
            changePriority(
                    messageCreatorToken, 48151623,
                    new ChangeMessagePriorityDtoRequest(MessagePriority.HIGH.name())
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getErrorCauseField()));
        }
    }

    @Test
    void testChangeMessagePriority_tryingToChangePriorityOfComment_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        final int forumId = createForumResponse.getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        final CreateCommentDtoRequest createCommentRequest = new CreateCommentDtoRequest("Comment #1");
        final ResponseEntity<MessageDtoResponse> createCommentResponse = createComment(
                forumOwnerToken, messageId, createCommentRequest
        );
        assertEquals(HttpStatus.OK, createCommentResponse.getStatusCode());
        assertNotNull(createCommentResponse.getBody());
        assertEquals(MessageState.PUBLISHED.name(), createCommentResponse.getBody().getState());
        final int commentId = createCommentResponse.getBody().getId();

        try {
            changePriority(forumOwnerToken, commentId,
                    new ChangeMessagePriorityDtoRequest(MessagePriority.HIGH.name())
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.UNABLE_OPERATION_FOR_COMMENT.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.UNABLE_OPERATION_FOR_COMMENT.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.UNABLE_OPERATION_FOR_COMMENT.getErrorCauseField()));
        }
    }

    @Test
    void testChangeMessagePriority_notMessageCreatorChangingMessagePriority_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "Fragile", "fragile@email.com", "hgfxh9ckjlDF5e"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Fragile", "I'm Fragile, but not that fragile", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        final RegisterUserDtoRequest registerGoofyUser = new RegisterUserDtoRequest(
                "Goofy", "goofy@fool.com", "hjhvjwhbehvbbv"
        );
        final ResponseEntity<UserDtoResponse> registerGoofyUserResponse = registerUser(registerGoofyUser);
        final String goofyUserToken = getSessionTokenFromHeaders(registerGoofyUserResponse);

        try {
            changePriority(goofyUserToken, messageId,
                    new ChangeMessagePriorityDtoRequest(MessagePriority.HIGH.name())
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getErrorCauseField()));
        }
    }

    @Test
    void testChangeMessagePriority_forumReadOnly_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "Fragile", "fragile@email.com", "hgfxh9ckjlDF5e"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Fragile", "I'm Fragile, but not that fragile", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        publishMessage(forumOwnerToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.YES.name()));
        getMessage(messageCreatorToken, messageId);

        deleteUser(forumOwnerToken);
        try {
            changePriority(messageCreatorToken, messageId,
                    new ChangeMessagePriorityDtoRequest(MessagePriority.HIGH.name())
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_READ_ONLY.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_READ_ONLY.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORUM_READ_ONLY.getErrorCauseField()));
        }
    }

    @Test
    void testMadeNewBranch() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        final int forumId = createForumResponse.getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        final CreateCommentDtoRequest createCommentRequest = new CreateCommentDtoRequest("Comment #1");
        final ResponseEntity<MessageDtoResponse> createCommentResponse = createComment(
                forumOwnerToken, messageId, createCommentRequest
        );
        assertEquals(HttpStatus.OK, createCommentResponse.getStatusCode());
        assertNotNull(createCommentResponse.getBody());
        assertEquals(MessageState.PUBLISHED.name(), createCommentResponse.getBody().getState());
        final int commentId = createCommentResponse.getBody().getId();

        final MadeBranchFromCommentDtoRequest madeBranchRequest = new MadeBranchFromCommentDtoRequest(
                "Dreams", MessagePriority.HIGH.name(), Collections.emptyList()
        );
        final ResponseEntity<MadeBranchFromCommentDtoResponse> madeBranchResponse = newBranchFromComment(
                forumOwnerToken, commentId, madeBranchRequest
        );
        assertEquals(HttpStatus.OK, madeBranchResponse.getStatusCode());
        assertNotNull(madeBranchResponse.getBody());
        assertEquals(commentId, madeBranchResponse.getBody().getId());

        final ResponseEntity<MessageInfoDtoResponse> getMessageResponse = getMessage(messageCreatorToken, commentId);
        assertEquals(HttpStatus.OK, getMessageResponse.getStatusCode());
        assertNotNull(getMessageResponse.getBody());

        final MessageInfoDtoResponse response = getMessageResponse.getBody();
        assertEquals(commentId, response.getId());
        assertEquals(madeBranchRequest.getSubject(), response.getSubject());
        assertEquals(madeBranchRequest.getPriority(), response.getPriority());
        assertEquals(createCommentRequest.getBody(), response.getBody().get(0));
    }

    @Test
    void testMadeNewBranch_messageNotFound_shouldReturnNotFound() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );

        try {
            final MadeBranchFromCommentDtoRequest madeBranchRequest = new MadeBranchFromCommentDtoRequest(
                    "Dreams", null, Arrays.asList("Music", "70s", "Pop")
            );
            newBranchFromComment(
                    forumOwnerToken, 48151623, madeBranchRequest
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getErrorCauseField()));
        }
    }

    @Test
    void testMadeNewBranch_userBanned_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerForumOwner = registerUser(registerForumOwnerRequest);
        final String forumOwnerToken = getSessionTokenFromHeaders(registerForumOwner);
        final int forumCreatorId = registerForumOwner.getBody().getId();

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "Fragile", "fragile@email.com", "hgfxh9ckjlDF5e"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Fragile", "I'm Fragile, but not that fragile", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        publishMessage(forumOwnerToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.YES.name()));
        getMessage(messageCreatorToken, messageId);

        final RegisterUserDtoRequest registerCommentCreatorRequest = new RegisterUserDtoRequest(
                "CommentCreator", "CommentCreator@email.com", "bbwcEWwn45jw"
        );
        final String commentCreatorToken = getSessionTokenFromHeaders(registerUser(registerCommentCreatorRequest));

        final CreateCommentDtoRequest createCommentRequest = new CreateCommentDtoRequest("I'm a comment");
        final ResponseEntity<MessageDtoResponse> createCommentResponse = createComment(
                commentCreatorToken, messageId, createCommentRequest
        );
        assertEquals(HttpStatus.OK, createCommentResponse.getStatusCode());
        assertNotNull(createCommentResponse.getBody());
        assertEquals(MessageState.UNPUBLISHED.name(), createCommentResponse.getBody().getState());
        final int commentId = createCommentResponse.getBody().getId();

        publishMessage(forumOwnerToken, commentId, new PublicationDecisionDtoRequest(PublicationDecision.YES.name()));

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        final ResponseEntity<UserDtoResponse> loginResponse = loginUser(loginRequest);
        final String superUserToken = getSessionTokenFromHeaders(loginResponse);
        banUser(superUserToken, forumCreatorId);

        try {
            final MadeBranchFromCommentDtoRequest madeBranchRequest = new MadeBranchFromCommentDtoRequest(
                    "NewBranch", MessagePriority.HIGH.name(), null
            );
            newBranchFromComment(forumOwnerToken, commentId, madeBranchRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_BANNED.getErrorCauseField()));
        }
    }

    @Test
    void testMadeNewBranch_tryingToMadeBranchFromBranch_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        final int forumId = createForumResponse.getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Tag1", "Tag2")
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        final CreateCommentDtoRequest createCommentRequest = new CreateCommentDtoRequest("Lennon");
        final ResponseEntity<MessageDtoResponse> createCommentResponse = createComment(
                forumOwnerToken, messageId, createCommentRequest
        );
        assertEquals(HttpStatus.OK, createCommentResponse.getStatusCode());
        assertNotNull(createCommentResponse.getBody());
        assertEquals(MessageState.PUBLISHED.name(), createCommentResponse.getBody().getState());

        try {
            final MadeBranchFromCommentDtoRequest madeBranchRequest = new MadeBranchFromCommentDtoRequest(
                    "Imagine", MessagePriority.NORMAL.name(), Arrays.asList("Tag3", "Tag2")
            );
            newBranchFromComment(forumOwnerToken, messageId, madeBranchRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_ALREADY_BRANCH.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_ALREADY_BRANCH.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_ALREADY_BRANCH.getErrorCauseField()));
        }
    }

    @Test
    void testMadeNewBranch_notForumOwnerSendRequest_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        final int forumId = createForumResponse.getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        final CreateCommentDtoRequest createCommentRequest = new CreateCommentDtoRequest("Comment #1");
        final ResponseEntity<MessageDtoResponse> createCommentResponse = createComment(
                forumOwnerToken, messageId, createCommentRequest
        );
        assertEquals(HttpStatus.OK, createCommentResponse.getStatusCode());
        assertNotNull(createCommentResponse.getBody());
        assertEquals(MessageState.PUBLISHED.name(), createCommentResponse.getBody().getState());
        final int commentId = createCommentResponse.getBody().getId();

        try {
            final MadeBranchFromCommentDtoRequest madeBranchRequest = new MadeBranchFromCommentDtoRequest(
                    "Dreams", MessagePriority.HIGH.name(), Collections.emptyList()
            );
            newBranchFromComment(messageCreatorToken, commentId, madeBranchRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getErrorCauseField()));
        }
    }

    @Test
    void testMadeNewBranch_noUserSession_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(
                createForumRequest, forumOwnerToken
        );
        final int forumId = createForumResponse.getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        final CreateCommentDtoRequest createCommentRequest = new CreateCommentDtoRequest("Comment #1");
        final ResponseEntity<MessageDtoResponse> createCommentResponse = createComment(
                forumOwnerToken, messageId, createCommentRequest
        );
        assertEquals(HttpStatus.OK, createCommentResponse.getStatusCode());
        assertNotNull(createCommentResponse.getBody());
        assertEquals(MessageState.PUBLISHED.name(), createCommentResponse.getBody().getState());
        final int commentId = createCommentResponse.getBody().getId();

        logoutUser(messageCreatorToken);
        try {
            final MadeBranchFromCommentDtoRequest madeBranchRequest = new MadeBranchFromCommentDtoRequest(
                    "Dreams", MessagePriority.HIGH.name(), Collections.emptyList()
            );
            newBranchFromComment(messageCreatorToken, commentId, madeBranchRequest);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getErrorCauseField()));
        }
    }

    @Test
    void testPublishMessage() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(MessageState.UNPUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        final ResponseEntity<EmptyDtoResponse> publishMessageResponse = publishMessage(
                forumOwnerToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.YES.name())
        );
        assertEquals(HttpStatus.OK, publishMessageResponse.getStatusCode());
        assertNotNull(publishMessageResponse.getBody());
        assertEquals("{}", publishMessageResponse.getBody().toString());

        final ResponseEntity<MessageInfoDtoResponse> getMessageResponse = getMessage(forumOwnerToken, messageId);
        assertEquals(HttpStatus.OK, getMessageResponse.getStatusCode());
        assertNotNull(getMessageResponse.getBody());
    }

    @Test
    void testUnpublishMessage_shouldDeleteFromServer() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(MessageState.UNPUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        final ResponseEntity<EmptyDtoResponse> publishMessageResponse = publishMessage(
                forumOwnerToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.NO.name())
        );
        assertEquals(HttpStatus.OK, publishMessageResponse.getStatusCode());
        assertNotNull(publishMessageResponse.getBody());
        assertEquals("{}", publishMessageResponse.getBody().toString());

        try {
            getMessage(messageCreatorToken, messageId);
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getErrorCauseField()));
        }
    }

    @Test
    void testUnpublishComment_shouldDeleteFromServer() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));
        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));
        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(MessageState.UNPUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        final ResponseEntity<EmptyDtoResponse> publishMessageResponse = publishMessage(
                forumOwnerToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.YES.name())
        );
        assertEquals(HttpStatus.OK, publishMessageResponse.getStatusCode());

        final CreateCommentDtoRequest createCommentRequest = new CreateCommentDtoRequest("Comment #1");
        final ResponseEntity<MessageDtoResponse> createCommentResponse = createComment(
                messageCreatorToken, messageId, createCommentRequest
        );
        assertEquals(HttpStatus.OK, createCommentResponse.getStatusCode());
        final int commentId = createCommentResponse.getBody().getId();

        final ResponseEntity<EmptyDtoResponse> unpublishCommentResponse = publishMessage(
                forumOwnerToken, commentId, new PublicationDecisionDtoRequest(PublicationDecision.NO.name())
        );
        assertEquals(HttpStatus.OK, unpublishCommentResponse.getStatusCode());

        final ResponseEntity<MessageInfoDtoResponse> message = getMessage(messageCreatorToken, messageId);
        assertTrue(message.getBody().getComments().isEmpty());
    }

    @Test
    void testUnpublishHistory_shouldDeleteItFromServer() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));
        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));
        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(MessageState.UNPUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        final ResponseEntity<EmptyDtoResponse> publishMessageResponse = publishMessage(
                forumOwnerToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.YES.name())
        );
        assertEquals(HttpStatus.OK, publishMessageResponse.getStatusCode());

        editMessage(messageCreatorToken, messageId, new EditMessageOrCommentDtoRequest("EDITED"));
        final ResponseEntity<EmptyDtoResponse> unpublishCommentResponse = publishMessage(
                forumOwnerToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.NO.name())
        );
        assertEquals(HttpStatus.OK, unpublishCommentResponse.getStatusCode());

        final ResponseEntity<MessageInfoDtoResponse> message = getMessage(forumOwnerToken, messageId);
        assertEquals(1, message.getBody().getBody().size());
        assertEquals(createMessageRequest.getBody(), message.getBody().getBody().get(0));
    }

    @Test
    void testPublishMessage_forumOwnerBanned_stillAvailableToPublishMessages() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerForumOwner = registerUser(registerForumOwnerRequest);
        final String forumOwnerToken = getSessionTokenFromHeaders(registerForumOwner);
        final int forumCreatorId = registerForumOwner.getBody().getId();

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "Fragile", "fragile@email.com", "hgfxh9ckjlDF5e"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Fragile", "I'm Fragile, but not that fragile", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        publishMessage(forumOwnerToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.YES.name()));
        getMessage(messageCreatorToken, messageId);

        final RegisterUserDtoRequest registerCommentCreatorRequest = new RegisterUserDtoRequest(
                "CommentCreator", "CommentCreator@email.com", "bbwcEWwn45jw"
        );
        final String commentCreatorToken = getSessionTokenFromHeaders(registerUser(registerCommentCreatorRequest));

        final CreateCommentDtoRequest createCommentRequest = new CreateCommentDtoRequest("I'm a comment");
        final ResponseEntity<MessageDtoResponse> createCommentResponse = createComment(
                commentCreatorToken, messageId, createCommentRequest
        );
        assertEquals(HttpStatus.OK, createCommentResponse.getStatusCode());
        assertNotNull(createCommentResponse.getBody());
        assertEquals(MessageState.UNPUBLISHED.name(), createCommentResponse.getBody().getState());
        final int commentId = createCommentResponse.getBody().getId();

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        final ResponseEntity<UserDtoResponse> loginResponse = loginUser(loginRequest);
        final String superUserToken = getSessionTokenFromHeaders(loginResponse);
        banUser(superUserToken, forumCreatorId);

        final ResponseEntity<EmptyDtoResponse> publicationAfterBanResponse = publishMessage(
                forumOwnerToken, commentId, new PublicationDecisionDtoRequest(PublicationDecision.NO.name())
        );
        assertEquals(HttpStatus.OK, publicationAfterBanResponse.getStatusCode());
        assertNotNull(publicationAfterBanResponse.getBody());
        assertEquals("{}", publicationAfterBanResponse.getBody().toString());
    }

    @Test
    void testPublishMessage_forumOwnerBannedPermanently_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerForumOwner = registerUser(registerForumOwnerRequest);
        final String forumOwnerToken = getSessionTokenFromHeaders(registerForumOwner);
        final int forumCreatorId = registerForumOwner.getBody().getId();

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "Fragile", "fragile@email.com", "hgfxh9ckjlDF5e"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Fragile", "I'm Fragile, but not that fragile", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        final int messageId = createMessageResponse.getBody().getId();

        publishMessage(forumOwnerToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.YES.name()));
        getMessage(messageCreatorToken, messageId);

        final RegisterUserDtoRequest registerCommentCreatorRequest = new RegisterUserDtoRequest(
                "CommentCreator", "CommentCreator@email.com", "bbwcEWwn45jw"
        );
        final String commentCreatorToken = getSessionTokenFromHeaders(registerUser(registerCommentCreatorRequest));

        final CreateCommentDtoRequest createCommentRequest = new CreateCommentDtoRequest("I'm a comment");
        final ResponseEntity<MessageDtoResponse> createCommentResponse = createComment(
                commentCreatorToken, messageId, createCommentRequest
        );
        assertEquals(HttpStatus.OK, createCommentResponse.getStatusCode());
        assertNotNull(createCommentResponse.getBody());
        assertEquals(MessageState.UNPUBLISHED.name(), createCommentResponse.getBody().getState());
        final int commentId = createCommentResponse.getBody().getId();

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
            publishMessage(forumOwnerToken, commentId,
                    new PublicationDecisionDtoRequest(PublicationDecision.NO.name())
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_PERMANENTLY_BANNED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_PERMANENTLY_BANNED.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_PERMANENTLY_BANNED.getErrorCauseField()));
        }
    }

    @Test
    void testPublishMessage_messagePublished_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(MessageState.UNPUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        final ResponseEntity<EmptyDtoResponse> publishMessageResponse = publishMessage(
                forumOwnerToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.YES.name())
        );
        assertEquals(HttpStatus.OK, publishMessageResponse.getStatusCode());
        assertNotNull(publishMessageResponse.getBody());
        assertEquals("{}", publishMessageResponse.getBody().toString());

        try {
            publishMessage(
                    forumOwnerToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.NO.name())
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_ALREADY_PUBLISHED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_ALREADY_PUBLISHED.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_ALREADY_PUBLISHED.getErrorCauseField()));
        }
    }

    @Test
    void testPublishMessage_notForumOwnerSendRequest_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(MessageState.UNPUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        try {
            publishMessage(
                    messageCreatorToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.NO.name())
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.FORBIDDEN_OPERATION.getErrorCauseField()));
        }
    }

    @Test
    void testPublishMessage_nuUserSession_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(MessageState.UNPUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        logoutUser(forumOwnerToken);
        try {
            publishMessage(
                    forumOwnerToken, messageId, new PublicationDecisionDtoRequest(PublicationDecision.NO.name())
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getErrorCauseField()));
        }
    }

    @Test
    void testRateMessage() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(MessageState.PUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        final ResponseEntity<EmptyDtoResponse> rateResponse1 = rateMessage(
                forumOwnerToken, messageId, new RateMessageDtoRequest(5)
        );
        assertEquals(HttpStatus.OK, rateResponse1.getStatusCode());
        assertNotNull(rateResponse1.getBody());
        assertEquals("{}", rateResponse1.getBody().toString());

        final ResponseEntity<MessageInfoDtoResponse> getMessageResponse = getMessage(forumOwnerToken, messageId);
        assertEquals(HttpStatus.OK, getMessageResponse.getStatusCode());
        assertNotNull(getMessageResponse.getBody());
        assertEquals(5, getMessageResponse.getBody().getRating());
        assertEquals(1, getMessageResponse.getBody().getRated());

        final ResponseEntity<EmptyDtoResponse> rateResponse2 = rateMessage(
                forumOwnerToken, messageId, new RateMessageDtoRequest(3)
        );
        assertEquals(HttpStatus.OK, rateResponse2.getStatusCode());
        assertNotNull(rateResponse2.getBody());
        assertEquals("{}", rateResponse2.getBody().toString());

        final ResponseEntity<MessageInfoDtoResponse> getMessageResponse2 = getMessage(forumOwnerToken, messageId);
        assertEquals(HttpStatus.OK, getMessageResponse.getStatusCode());
        assertNotNull(getMessageResponse.getBody());
        assertEquals(3, getMessageResponse2.getBody().getRating());
        assertEquals(1, getMessageResponse2.getBody().getRated());
    }

    @Test
    void testUnrateMessage() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(MessageState.PUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        final ResponseEntity<EmptyDtoResponse> rateResponse1 = rateMessage(
                forumOwnerToken, messageId, new RateMessageDtoRequest(5)
        );
        assertEquals(HttpStatus.OK, rateResponse1.getStatusCode());
        assertNotNull(rateResponse1.getBody());
        assertEquals("{}", rateResponse1.getBody().toString());

        final ResponseEntity<MessageInfoDtoResponse> getMessageResponse = getMessage(forumOwnerToken, messageId);
        assertEquals(HttpStatus.OK, getMessageResponse.getStatusCode());
        assertNotNull(getMessageResponse.getBody());
        assertEquals(5, getMessageResponse.getBody().getRating());
        assertEquals(1, getMessageResponse.getBody().getRated());

        final ResponseEntity<EmptyDtoResponse> rateResponse2 = rateMessage(
                forumOwnerToken, messageId, new RateMessageDtoRequest(null)
        );
        assertEquals(HttpStatus.OK, rateResponse2.getStatusCode());
        assertNotNull(rateResponse2.getBody());
        assertEquals("{}", rateResponse2.getBody().toString());

        final ResponseEntity<MessageInfoDtoResponse> getMessageResponse2 = getMessage(forumOwnerToken, messageId);
        assertEquals(HttpStatus.OK, getMessageResponse.getStatusCode());
        assertNotNull(getMessageResponse.getBody());
        assertEquals(0, getMessageResponse2.getBody().getRating());
        assertEquals(0, getMessageResponse2.getBody().getRated());
    }

    @Test
    void testRateMessageFromDifferentUsers() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(MessageState.PUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        final ResponseEntity<EmptyDtoResponse> rateResponse1 = rateMessage(
                forumOwnerToken, messageId, new RateMessageDtoRequest(5)
        );
        assertEquals(HttpStatus.OK, rateResponse1.getStatusCode());
        assertNotNull(rateResponse1.getBody());
        assertEquals("{}", rateResponse1.getBody().toString());

        final RegisterUserDtoRequest registerSomeUserRequest = new RegisterUserDtoRequest(
                "SomeUser", "SomeUser@email.com", "ggehjoHJHwomq"
        );
        final String someUserToken = getSessionTokenFromHeaders(registerUser(registerSomeUserRequest));

        final RegisterUserDtoRequest registerOtherUserRequest = new RegisterUserDtoRequest(
                "OtherUser", "OtherUser@email.com", "oqmnAHhn2e7"
        );
        final String otherUserToken = getSessionTokenFromHeaders(registerUser(registerOtherUserRequest));

        final ResponseEntity<EmptyDtoResponse> rateResponse2 = rateMessage(
                someUserToken, messageId, new RateMessageDtoRequest(4)
        );
        final ResponseEntity<EmptyDtoResponse> rateResponse3 = rateMessage(
                otherUserToken, messageId, new RateMessageDtoRequest(3)
        );

        final ResponseEntity<MessageInfoDtoResponse> getMessageResponse = getMessage(forumOwnerToken, messageId);
        assertEquals(HttpStatus.OK, getMessageResponse.getStatusCode());
        assertNotNull(getMessageResponse.getBody());
        assertEquals(4, getMessageResponse.getBody().getRating());
        assertEquals(3, getMessageResponse.getBody().getRated());
    }

    @Test
    void testRateMessage_ratesHimself_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(MessageState.PUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        try {
            rateMessage(
                    messageCreatorToken, messageId, new RateMessageDtoRequest(5)
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString()
                    .contains(ErrorCode.MESSAGE_CREATOR_RATES_HIS_MESSAGE.name()));
            assertTrue(ce.getResponseBodyAsString()
                    .contains(ErrorCode.MESSAGE_CREATOR_RATES_HIS_MESSAGE.getMessage()));
            assertTrue(ce.getResponseBodyAsString()
                    .contains(ErrorCode.MESSAGE_CREATOR_RATES_HIS_MESSAGE.getErrorCauseField()));
        }
    }

    @Test
    void testRateMessage_ratesUnpublishedMessage_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.MODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(MessageState.UNPUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        final RegisterUserDtoRequest registerGoofyUser = new RegisterUserDtoRequest(
                "Goofy", "goofy@fool.com", "hjhvjwhbehvbbv"
        );
        final ResponseEntity<UserDtoResponse> registerGoofyUserResponse = registerUser(registerGoofyUser);
        final String goofyUserToken = getSessionTokenFromHeaders(registerGoofyUserResponse);
        try {
            rateMessage(
                    goofyUserToken, messageId, new RateMessageDtoRequest(4)
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_PUBLISHED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_PUBLISHED.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_PUBLISHED.getErrorCauseField()));
        }
    }

    @Test
    void testRateMessage_userBannedPermanently_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(MessageState.PUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        final RegisterUserDtoRequest registerGoofyUser = new RegisterUserDtoRequest(
                "Goofy", "goofy@fool.com", "hjhvjwhbehvbbv"
        );
        final ResponseEntity<UserDtoResponse> registerGoofyUserResponse = registerUser(registerGoofyUser);
        final String goofyUserToken = getSessionTokenFromHeaders(registerGoofyUserResponse);
        final int goofyUserId = registerGoofyUserResponse.getBody().getId();

        final LoginUserDtoRequest loginRequest = new LoginUserDtoRequest(
                "admin", "admin_strong_pass"
        );
        final String superUserToken = getSessionTokenFromHeaders(loginUser(loginRequest));
        banUser(superUserToken, goofyUserId);
        banUser(superUserToken, goofyUserId);
        banUser(superUserToken, goofyUserId);
        banUser(superUserToken, goofyUserId);
        banUser(superUserToken, goofyUserId);

        try {
            rateMessage(goofyUserToken, messageId, new RateMessageDtoRequest(5));
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_PERMANENTLY_BANNED.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_PERMANENTLY_BANNED.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.USER_PERMANENTLY_BANNED.getErrorCauseField()));
        }
    }

    @Test
    void testRateMessage_messageNotFound_shouldReturnNotFound() {
        final RegisterUserDtoRequest registerUser = new RegisterUserDtoRequest(
                "user", "user@email.com", "w3ryStr0nGPa55wD"
        );
        final String userToken = getSessionTokenFromHeaders(registerUser(registerUser));

        try {
            rateMessage(userToken, 481516, new RateMessageDtoRequest(3));
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getErrorCauseField()));
        }
    }

    @Test
    void testRateMessage_noUserSession_shouldReturnBadRequest() {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken = getSessionTokenFromHeaders(registerUser(registerForumOwnerRequest));

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", ForumType.UNMODERATED.name()
        );
        final int forumId = createForum(createForumRequest, forumOwnerToken).getBody().getId();

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final String messageCreatorToken = getSessionTokenFromHeaders(registerUser(registerMessageCreatorRequest));

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body", null, null
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                messageCreatorToken, forumId, createMessageRequest
        );
        assertEquals(MessageState.PUBLISHED.name(), createMessageResponse.getBody().getState());
        final int messageId = createMessageResponse.getBody().getId();

        try {
            rateMessage(forumOwnerToken, messageId, new RateMessageDtoRequest(5));
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.BAD_REQUEST, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.NO_USER_SESSION.getErrorCauseField()));
        }
    }

    @Test
    void testGetForumMessage_withAndWithoutComments() {
        final RegisterUserDtoRequest registerForumOwner1 = new RegisterUserDtoRequest(
                "ForumOwner1", "fo1@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken1 = getSessionTokenFromHeaders(registerUser(registerForumOwner1));

        final RegisterUserDtoRequest registerForumOwner2 = new RegisterUserDtoRequest(
                "ForumOwner2", "fo2@email.com", "utubfm43mWEa"
        );
        final String forumOwnerToken2 = getSessionTokenFromHeaders(registerUser(registerForumOwner2));

        final CreateForumDtoRequest createForumRequest1 = new CreateForumDtoRequest(
                "FirstForum", ForumType.UNMODERATED.name()
        );
        final int forumId1 = createForum(createForumRequest1, forumOwnerToken1).getBody().getId();

        final CreateForumDtoRequest createForumRequest2 = new CreateForumDtoRequest(
                "SecondForum", ForumType.MODERATED.name()
        );
        final int forumId2 = createForum(createForumRequest2, forumOwnerToken2).getBody().getId();

        final CreateMessageDtoRequest createMessage1 = new CreateMessageDtoRequest(
                "Subject #1", "Body #1", null, null
        );
        final int messageId1 = createMessage(forumOwnerToken1, forumId1, createMessage1).getBody().getId();

        final CreateMessageDtoRequest createMessage2 = new CreateMessageDtoRequest(
                "Subject #2", "Body #2", MessagePriority.HIGH.name(), null
        );
        final int messageId2 = createMessage(forumOwnerToken2, forumId1, createMessage2).getBody().getId();
        rateMessage(forumOwnerToken2, messageId1, new RateMessageDtoRequest(4));

        rateMessage(forumOwnerToken1, messageId2, new RateMessageDtoRequest(3));
        final int commentId1 = createComment(forumOwnerToken1, messageId2,
                new CreateCommentDtoRequest("Comment #1")
        ).getBody().getId();

        rateMessage(forumOwnerToken2, commentId1, new RateMessageDtoRequest(5));
        final int commentId2 = createComment(forumOwnerToken2, commentId1,
                new CreateCommentDtoRequest("Comment #2")
        ).getBody().getId();

        final ResponseEntity<MessageInfoDtoResponse> getMessage2WithComments = getMessage(
                forumOwnerToken1, messageId2,
                true, false, false, MessageOrder.ASC.name()
        );
        assertEquals(HttpStatus.OK, getMessage2WithComments.getStatusCode());
        assertNotNull(getMessage2WithComments.getBody());

        final MessageInfoDtoResponse message2 = getMessage2WithComments.getBody();
        assertEquals(messageId2, message2.getId());
        assertEquals(createMessage2.getSubject(), message2.getSubject());
        assertEquals(createMessage2.getPriority(), message2.getPriority());
        assertEquals(registerForumOwner2.getName(), message2.getCreator());
        assertEquals(1, message2.getRated());
        assertEquals(3, message2.getRating());
        assertEquals(1, message2.getComments().size());

        final CommentInfoDtoResponse comment1 = message2.getComments().get(0);
        assertEquals(commentId1, comment1.getId());
        assertEquals(registerForumOwner1.getName(), comment1.getCreator());
        assertEquals(1, comment1.getRated());
        assertEquals(5, comment1.getRating());
        assertEquals(1, comment1.getComments().size());

        final CommentInfoDtoResponse comment2 = comment1.getComments().get(0);
        assertEquals(commentId2, comment2.getId());
        assertEquals(registerForumOwner2.getName(), comment2.getCreator());
        assertEquals(0, comment2.getRated());
        assertEquals(0, comment2.getRating());
        assertTrue(comment2.getComments().isEmpty());

        final ResponseEntity<MessageInfoDtoResponse> getMessage2WithoutComments = getMessage(
                forumOwnerToken1, messageId2,
                true, true, false, MessageOrder.ASC.name()
        );
        assertEquals(HttpStatus.OK, getMessage2WithoutComments.getStatusCode());
        assertNotNull(getMessage2WithoutComments.getBody());

        final MessageInfoDtoResponse message2WithoutComments = getMessage2WithoutComments.getBody();
        assertEquals(messageId2, message2WithoutComments.getId());
        assertEquals(createMessage2.getSubject(), message2WithoutComments.getSubject());
        assertEquals(createMessage2.getPriority(), message2WithoutComments.getPriority());
        assertEquals(registerForumOwner2.getName(), message2WithoutComments.getCreator());
        assertEquals(1, message2WithoutComments.getRated());
        assertEquals(3, message2WithoutComments.getRating());
        assertTrue(message2WithoutComments.getComments().isEmpty());
    }

    @Test
    void testGetForumMessage_withAndWithoutAllVersions() {
        final RegisterUserDtoRequest registerForumOwner1 = new RegisterUserDtoRequest(
                "ForumOwner1", "fo1@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken1 = getSessionTokenFromHeaders(registerUser(registerForumOwner1));

        final RegisterUserDtoRequest registerForumOwner2 = new RegisterUserDtoRequest(
                "ForumOwner2", "fo2@email.com", "utubfm43mWEa"
        );
        final String forumOwnerToken2 = getSessionTokenFromHeaders(registerUser(registerForumOwner2));

        final CreateForumDtoRequest createForumRequest1 = new CreateForumDtoRequest(
                "FirstForum", ForumType.MODERATED.name()
        );
        final int forumId1 = createForum(createForumRequest1, forumOwnerToken1).getBody().getId();

        final CreateMessageDtoRequest createMessage = new CreateMessageDtoRequest(
                "Subject #1", "Body #1", null, null
        );
        final int messageId = createMessage(forumOwnerToken2, forumId1, createMessage).getBody().getId();
        publishMessage(forumOwnerToken1, messageId, new PublicationDecisionDtoRequest(PublicationDecision.YES.name()));

        final EditMessageOrCommentDtoRequest edit1 = new EditMessageOrCommentDtoRequest("Edit #1");
        editMessage(forumOwnerToken2, messageId, edit1);
        publishMessage(forumOwnerToken1, messageId, new PublicationDecisionDtoRequest(PublicationDecision.YES.name()));

        final EditMessageOrCommentDtoRequest edit2 = new EditMessageOrCommentDtoRequest("Edit #2");
        editMessage(forumOwnerToken2, messageId, edit2);

        final ResponseEntity<MessageInfoDtoResponse> getMessageAllPublishedVersions = getMessage(
                forumOwnerToken1, messageId,
                true, true, false, MessageOrder.DESC.name()
        );
        assertEquals(HttpStatus.OK, getMessageAllPublishedVersions.getStatusCode());
        assertNotNull(getMessageAllPublishedVersions.getBody());

        final MessageInfoDtoResponse message = getMessageAllPublishedVersions.getBody();
        assertEquals(messageId, message.getId());
        assertEquals(createMessage.getSubject(), message.getSubject());
        assertEquals(MessagePriority.NORMAL.name(), message.getPriority());
        assertEquals(registerForumOwner2.getName(), message.getCreator());
        assertEquals(0, message.getRated());
        assertEquals(0, message.getRating());
        assertEquals(0, message.getComments().size());
        assertEquals(2, message.getBody().size());
        assertEquals(edit1.getBody(), message.getBody().get(0));
        assertEquals(createMessage.getBody(), message.getBody().get(1));

        final ResponseEntity<MessageInfoDtoResponse> getMessageAllVersions = getMessage(
                forumOwnerToken1, messageId,
                true, true, true, MessageOrder.DESC.name()
        );
        assertEquals(HttpStatus.OK, getMessageAllVersions.getStatusCode());
        assertNotNull(getMessageAllVersions.getBody());

        final MessageInfoDtoResponse message1 = getMessageAllVersions.getBody();
        assertEquals(messageId, message1.getId());
        assertEquals(createMessage.getSubject(), message1.getSubject());
        assertEquals(MessagePriority.NORMAL.name(), message1.getPriority());
        assertEquals(registerForumOwner2.getName(), message1.getCreator());
        assertEquals(0, message1.getRated());
        assertEquals(0, message1.getRating());
        assertEquals(0, message1.getComments().size());
        assertEquals(3, message1.getBody().size());
        assertTrue(message1.getBody().get(0).contains(edit2.getBody()));
        assertEquals(edit1.getBody(), message1.getBody().get(1));
        assertEquals(createMessage.getBody(), message1.getBody().get(2));
    }

    @Test
    void testGetForumMessage_messageNotFound_shouldReturnNotFound() {
        final RegisterUserDtoRequest registerForumOwner1 = new RegisterUserDtoRequest(
                "ForumOwner1", "fo1@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken1 = getSessionTokenFromHeaders(registerUser(registerForumOwner1));

        try {
            final ResponseEntity<MessageInfoDtoResponse> getMessageAllPublishedVersions = getMessage(
                    forumOwnerToken1, 48151623,
                    true, true, false, MessageOrder.DESC.name()
            );
        } catch (HttpClientErrorException ce) {
            assertEquals(HttpStatus.NOT_FOUND, ce.getStatusCode());
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.name()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getMessage()));
            assertTrue(ce.getResponseBodyAsString().contains(ErrorCode.MESSAGE_NOT_FOUND.getErrorCauseField()));
        }
    }

    @Test
    void testGetMessagesList_testNoCommentsOrderOffsetLimitAndTagsParams() {
        final RegisterUserDtoRequest registerUser1 = new RegisterUserDtoRequest(
                "user1", "user1@email.com", "utubfm43mWEa"
        );
        final String userToken1 = getSessionTokenFromHeaders(registerUser(registerUser1));
        final RegisterUserDtoRequest registerUser2 = new RegisterUserDtoRequest(
                "user2", "user2@email.com", "utubfm43mWEa"
        );
        final String userToken2 = getSessionTokenFromHeaders(registerUser(registerUser2));
        final RegisterUserDtoRequest registerUser3 = new RegisterUserDtoRequest(
                "user3", "user3@email.com", "utubfm43mWEa"
        );
        final String userToken3 = getSessionTokenFromHeaders(registerUser(registerUser3));

        final RegisterUserDtoRequest registerForumOwner1 = new RegisterUserDtoRequest(
                "ForumOwner1", "fo1@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken1 = getSessionTokenFromHeaders(registerUser(registerForumOwner1));
        final CreateForumDtoRequest createForumRequest1 = new CreateForumDtoRequest(
                "FirstForum", ForumType.UNMODERATED.name()
        );
        final int forumId1 = createForum(createForumRequest1, forumOwnerToken1).getBody().getId();

        final CreateMessageDtoRequest createMessage1 = new CreateMessageDtoRequest(
                "Subject #1", "Body #1",
                MessagePriority.HIGH.name(), Arrays.asList("Tag1")
        );
        final int messageId1 = createMessage(userToken1, forumId1, createMessage1).getBody().getId();
        final int commentId1 = createComment(userToken2, messageId1,
                new CreateCommentDtoRequest("Comment #1")).getBody().getId();
        final int commentId2 = createComment(forumOwnerToken1, messageId1,
                new CreateCommentDtoRequest("Comment #2")).getBody().getId();
        final int commentId3 = createComment(userToken1, commentId1,
                new CreateCommentDtoRequest("Comment #3")).getBody().getId();

        final CreateMessageDtoRequest createMessage2 = new CreateMessageDtoRequest(
                "Subject #2", "Body #2",
                null, Arrays.asList("Tag2", "Tag1")
        );
        final int messageId2 = createMessage(userToken2, forumId1, createMessage2).getBody().getId();
        final int commentId4 = createComment(userToken3, messageId2,
                new CreateCommentDtoRequest("Comment #4")).getBody().getId();
        final int commentId5 = createComment(forumOwnerToken1, commentId4,
                new CreateCommentDtoRequest("Comment #5")).getBody().getId();

        final ResponseEntity<ListMessageInfoDtoResponse> messagesResponse1 = getMessageList(
                userToken1, forumId1, false, false, false,
                MessageOrder.DESC.name(), null, 300, 0
        );
        assertEquals(HttpStatus.OK, messagesResponse1.getStatusCode());
        assertNotNull(messagesResponse1.getBody());

        final List<MessageInfoDtoResponse> messages1 = messagesResponse1.getBody().getMessages();
        assertEquals(2, messages1.size());
        assertEquals(messageId1, messages1.get(0).getId());
        assertEquals(registerUser1.getName(), messages1.get(0).getCreator());
        assertEquals(1, messages1.get(0).getBody().size());
        assertEquals(createMessage1.getBody(), messages1.get(0).getBody().get(0));
        assertEquals(2, messages1.get(0).getComments().size());

        assertEquals(messageId2, messages1.get(1).getId());
        assertEquals(registerUser2.getName(), messages1.get(1).getCreator());
        assertEquals(1, messages1.get(1).getBody().size());
        assertEquals(createMessage2.getBody(), messages1.get(1).getBody().get(0));
        assertEquals(1, messages1.get(1).getComments().size());

        final ResponseEntity<ListMessageInfoDtoResponse> messagesResponse2 = getMessageList(
                userToken1, forumId1, false, true, false,
                MessageOrder.DESC.name(), null, 300, 0
        );
        assertEquals(HttpStatus.OK, messagesResponse2.getStatusCode());
        assertNotNull(messagesResponse2.getBody());

        final List<MessageInfoDtoResponse> messages2 = messagesResponse2.getBody().getMessages();
        assertEquals(2, messages2.size());
        assertEquals(messageId1, messages2.get(0).getId());
        assertEquals(registerUser1.getName(), messages2.get(0).getCreator());
        assertEquals(1, messages2.get(0).getBody().size());
        assertEquals(createMessage1.getBody(), messages2.get(0).getBody().get(0));
        assertEquals(0, messages2.get(0).getComments().size());

        assertEquals(messageId2, messages2.get(1).getId());
        assertEquals(registerUser2.getName(), messages2.get(1).getCreator());
        assertEquals(1, messages2.get(1).getBody().size());
        assertEquals(createMessage2.getBody(), messages2.get(1).getBody().get(0));
        assertEquals(0, messages2.get(1).getComments().size());

        final ResponseEntity<ListMessageInfoDtoResponse> messagesResponse3 = getMessageList(
                userToken1, forumId1, false, true, false,
                MessageOrder.ASC.name(), null, 300, 1
        );
        assertEquals(HttpStatus.OK, messagesResponse3.getStatusCode());
        assertNotNull(messagesResponse3.getBody());

        final List<MessageInfoDtoResponse> messages3 = messagesResponse3.getBody().getMessages();
        assertEquals(1, messages3.size());
        assertEquals(messageId2, messages3.get(0).getId());
        assertEquals(registerUser2.getName(), messages3.get(0).getCreator());
        assertEquals(1, messages3.get(0).getBody().size());
        assertEquals(createMessage2.getBody(), messages3.get(0).getBody().get(0));
        assertEquals(0, messages3.get(0).getComments().size());

        final ResponseEntity<ListMessageInfoDtoResponse> messagesResponse4 = getMessageList(
                userToken1, forumId1, false, true, false,
                MessageOrder.DESC.name(), null, 300, 20
        );
        assertEquals(HttpStatus.OK, messagesResponse4.getStatusCode());
        assertNotNull(messagesResponse4.getBody());
        assertTrue(messagesResponse4.getBody().getMessages().isEmpty());

        final ResponseEntity<ListMessageInfoDtoResponse> messagesResponse5 = getMessageList(
                userToken1, forumId1, false, true, false,
                MessageOrder.DESC.name(), Arrays.asList("Tag2", "Tag4"), 300, 0
        );
        assertEquals(HttpStatus.OK, messagesResponse5.getStatusCode());
        assertNotNull(messagesResponse5.getBody());

        final List<MessageInfoDtoResponse> messages5 = messagesResponse5.getBody().getMessages();
        assertEquals(1, messages5.size());
        assertEquals(messageId2, messages5.get(0).getId());
        assertEquals(registerUser2.getName(), messages5.get(0).getCreator());
        assertEquals(1, messages5.get(0).getBody().size());
        assertEquals(createMessage2.getBody(), messages5.get(0).getBody().get(0));
        assertEquals(0, messages5.get(0).getComments().size());
    }

    @Test
    void testGetMessagesList_testAllVersionsUnpublishedAndTagsParams() {
        final RegisterUserDtoRequest registerUser1 = new RegisterUserDtoRequest(
                "user1", "user1@email.com", "utubfm43mWEa"
        );
        final String userToken1 = getSessionTokenFromHeaders(registerUser(registerUser1));

        final RegisterUserDtoRequest registerForumOwner1 = new RegisterUserDtoRequest(
                "ForumOwner1", "fo1@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken1 = getSessionTokenFromHeaders(registerUser(registerForumOwner1));
        final CreateForumDtoRequest createForumRequest1 = new CreateForumDtoRequest(
                "FirstForum", ForumType.MODERATED.name()
        );
        final int forumId1 = createForum(createForumRequest1, forumOwnerToken1).getBody().getId();

        final CreateMessageDtoRequest createMessage1 = new CreateMessageDtoRequest(
                "Subject #1", "Body #1",
                MessagePriority.HIGH.name(), Arrays.asList("Tag1")
        );
        final int messageId1 = createMessage(userToken1, forumId1, createMessage1).getBody().getId();
        publishMessage(forumOwnerToken1, messageId1, new PublicationDecisionDtoRequest(
                PublicationDecision.YES.name()
        ));
        editMessage(userToken1, messageId1, new EditMessageOrCommentDtoRequest("Edit #1"));
        publishMessage(forumOwnerToken1, messageId1, new PublicationDecisionDtoRequest(
                PublicationDecision.YES.name()
        ));
        editMessage(userToken1, messageId1, new EditMessageOrCommentDtoRequest("Edit #1.2"));

        final RegisterUserDtoRequest registerUser2 = new RegisterUserDtoRequest(
                "user2", "user2@email.com", "utubfm43mWEa"
        );
        final String userToken2 = getSessionTokenFromHeaders(registerUser(registerUser2));
        final CreateMessageDtoRequest createMessage2 = new CreateMessageDtoRequest(
                "Subject #2", "Body #2",
                null, Arrays.asList("Tag2", "Tag1")
        );
        final int messageId2 = createMessage(userToken2, forumId1, createMessage2).getBody().getId();
        publishMessage(forumOwnerToken1, messageId2, new PublicationDecisionDtoRequest(
                PublicationDecision.YES.name()
        ));
        final int commentId1 = createComment(userToken2, messageId1,
                new CreateCommentDtoRequest("Comment #1")).getBody().getId();
        publishMessage(forumOwnerToken1, commentId1, new PublicationDecisionDtoRequest(
                PublicationDecision.YES.name()
        ));

        final RegisterUserDtoRequest registerUser3 = new RegisterUserDtoRequest(
                "user3", "user3@email.com", "utubfm43mWEa"
        );
        final String userToken3 = getSessionTokenFromHeaders(registerUser(registerUser3));
        final CreateMessageDtoRequest createMessage3 = new CreateMessageDtoRequest(
                "Subject #3", "Body #3",
                MessagePriority.NORMAL.name(), Arrays.asList("Tag4")
        );
        final int messageId3 = createMessage(userToken3, forumId1, createMessage3).getBody().getId();
        publishMessage(forumOwnerToken1, messageId3, new PublicationDecisionDtoRequest(
                PublicationDecision.YES.name()
        ));
        editMessage(userToken3, messageId3, new EditMessageOrCommentDtoRequest("Edit #3"));

        final ResponseEntity<ListMessageInfoDtoResponse> messagesResponse1 = getMessageList(
                userToken1, forumId1, true, false, false,
                MessageOrder.DESC.name(), null, 300, 0
        );
        assertEquals(HttpStatus.OK, messagesResponse1.getStatusCode());
        assertNotNull(messagesResponse1.getBody());

        final List<MessageInfoDtoResponse> messages1 = messagesResponse1.getBody().getMessages();
        assertEquals(3, messages1.size());
        assertEquals(messageId1, messages1.get(0).getId());
        assertEquals(2, messages1.get(0).getBody().size());
        assertEquals(messageId3, messages1.get(1).getId());
        assertEquals(1, messages1.get(1).getBody().size());
        assertEquals(messageId2, messages1.get(2).getId());
        assertEquals(1, messages1.get(2).getBody().size());

        final ResponseEntity<ListMessageInfoDtoResponse> messagesResponse2 = getMessageList(
                userToken1, forumId1, false, false, true,
                MessageOrder.DESC.name(), null, 300, 0
        );
        assertEquals(HttpStatus.OK, messagesResponse2.getStatusCode());
        assertNotNull(messagesResponse2.getBody());

        final List<MessageInfoDtoResponse> messages2 = messagesResponse2.getBody().getMessages();
        assertEquals(3, messages2.size());
        assertEquals(messageId1, messages2.get(0).getId());
        assertEquals(1, messages2.get(0).getBody().size());
        assertEquals(messageId3, messages2.get(1).getId());
        assertEquals(1, messages2.get(1).getBody().size());
        assertEquals(messageId2, messages2.get(2).getId());
        assertEquals(1, messages2.get(2).getBody().size());

        final ResponseEntity<ListMessageInfoDtoResponse> messagesResponse3 = getMessageList(
                forumOwnerToken1, forumId1, true, false, true,
                MessageOrder.DESC.name(), null, 300, 0
        );
        assertEquals(HttpStatus.OK, messagesResponse3.getStatusCode());
        assertNotNull(messagesResponse3.getBody());

        final List<MessageInfoDtoResponse> messages3 = messagesResponse3.getBody().getMessages();
        assertEquals(3, messages3.size());
        assertEquals(messageId1, messages3.get(0).getId());
        assertEquals(3, messages3.get(0).getBody().size());
        assertEquals(messageId3, messages3.get(1).getId());
        assertEquals(2, messages3.get(1).getBody().size());
        assertEquals(messageId2, messages3.get(2).getId());
        assertEquals(1, messages3.get(2).getBody().size());

        final ResponseEntity<ListMessageInfoDtoResponse> messagesResponse4 = getMessageList(
                forumOwnerToken1, forumId1, false, false, true,
                MessageOrder.DESC.name(), null, 300, 0
        );
        assertEquals(HttpStatus.OK, messagesResponse4.getStatusCode());
        assertNotNull(messagesResponse4.getBody());

        final List<MessageInfoDtoResponse> messages4 = messagesResponse4.getBody().getMessages();
        assertEquals(3, messages4.size());
        assertEquals(messageId1, messages4.get(0).getId());
        assertEquals(1, messages4.get(0).getBody().size());
        assertEquals(messageId3, messages4.get(1).getId());
        assertEquals(1, messages4.get(1).getBody().size());
        assertEquals(messageId2, messages4.get(2).getId());
        assertEquals(1, messages4.get(2).getBody().size());

        final ResponseEntity<ListMessageInfoDtoResponse> messagesResponse5 = getMessageList(
                forumOwnerToken1, forumId1, true, false, true,
                MessageOrder.DESC.name(), Arrays.asList("Tag1"), 300, 0
        );
        assertEquals(HttpStatus.OK, messagesResponse5.getStatusCode());
        assertNotNull(messagesResponse5.getBody());

        final List<MessageInfoDtoResponse> messages5 = messagesResponse5.getBody().getMessages();
        assertEquals(2, messages5.size());
        assertEquals(messageId1, messages5.get(0).getId());
        assertEquals(3, messages5.get(0).getBody().size());
        assertEquals(messageId2, messages5.get(1).getId());
        assertEquals(1, messages5.get(1).getBody().size());

        final ResponseEntity<ListMessageInfoDtoResponse> messagesResponse6 = getMessageList(
                forumOwnerToken1, forumId1, true, false, true,
                MessageOrder.DESC.name(), Arrays.asList("Tag4", "TaNoTag"), 300, 0
        );
        assertEquals(HttpStatus.OK, messagesResponse6.getStatusCode());
        assertNotNull(messagesResponse6.getBody());

        final List<MessageInfoDtoResponse> messages6 = messagesResponse6.getBody().getMessages();
        assertEquals(1, messages6.size());
        assertEquals(messageId3, messages6.get(0).getId());
        assertEquals(2, messages6.get(0).getBody().size());
    }

    @Test
    void testGetMessagesList_noMessagesInForum_shouldReturnEmptyList() {
        final RegisterUserDtoRequest registerForumOwner1 = new RegisterUserDtoRequest(
                "ForumOwner1", "fo1@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken1 = getSessionTokenFromHeaders(registerUser(registerForumOwner1));
        final CreateForumDtoRequest createForumRequest1 = new CreateForumDtoRequest(
                "FirstForum", ForumType.MODERATED.name()
        );
        final int forumId1 = createForum(createForumRequest1, forumOwnerToken1).getBody().getId();

        final ResponseEntity<ListMessageInfoDtoResponse> messagesResponse = getMessageList(
                forumOwnerToken1, forumId1, false, false, true,
                MessageOrder.DESC.name(), null, 300, 0
        );
        assertEquals(HttpStatus.OK, messagesResponse.getStatusCode());
        assertNotNull(messagesResponse.getBody());
        assertTrue(messagesResponse.getBody().getMessages().isEmpty());
    }
}
