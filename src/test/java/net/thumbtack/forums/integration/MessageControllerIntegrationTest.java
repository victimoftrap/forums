package net.thumbtack.forums.integration;

import net.thumbtack.forums.dto.requests.message.CreateCommentDtoRequest;
import net.thumbtack.forums.dto.requests.user.LoginUserDtoRequest;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.model.enums.MessageState;
import net.thumbtack.forums.model.enums.MessagePriority;
import net.thumbtack.forums.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.requests.forum.CreateForumDtoRequest;
import net.thumbtack.forums.dto.requests.message.CreateMessageDtoRequest;
import net.thumbtack.forums.dto.responses.user.UserDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumDtoResponse;
import net.thumbtack.forums.dto.responses.message.MessageDtoResponse;

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
            String forumType, boolean isMessageCreatesForumOwner, String messageState
    ) {
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

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                isMessageCreatesForumOwner ? forumOwnerToken : messageCreatorToken,
                createForumResponse.getBody().getId(),
                createMessageRequest
        );
        assertEquals(HttpStatus.OK, createMessageResponse.getStatusCode());
        assertNotNull(createMessageResponse.getBody());
        assertEquals(messageState, createMessageResponse.getBody().getState());
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

    @Test
    void testCreateComment() {
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
}
