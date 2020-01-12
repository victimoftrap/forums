package net.thumbtack.forums.integration;

import net.thumbtack.forums.dto.responses.message.MadeBranchFromCommentDtoResponse;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.model.enums.MessageState;
import net.thumbtack.forums.model.enums.MessagePriority;
import net.thumbtack.forums.model.enums.PublicationDecision;
import net.thumbtack.forums.dto.requests.message.*;
import net.thumbtack.forums.dto.requests.user.LoginUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.requests.forum.CreateForumDtoRequest;
import net.thumbtack.forums.dto.responses.user.UserDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumDtoResponse;
import net.thumbtack.forums.dto.responses.message.MessageDtoResponse;
import net.thumbtack.forums.dto.responses.message.EditMessageOrCommentDtoResponse;
import net.thumbtack.forums.dto.responses.message.MessageInfoDtoResponse;
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
            String forumType, boolean isMessageCreatesForumOwner, String expectedMessageState
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
    void testEditMessage() {
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

        final ResponseEntity<MessageInfoDtoResponse> getMessageResponse = getMessage(messageCreatorToken, messageId);
        assertEquals(HttpStatus.OK, getMessageResponse.getStatusCode());
        assertNotNull(getMessageResponse.getBody());
        assertEquals(editMessageRequest.getBody(), getMessageResponse.getBody().getBody().get(0));
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
                "Dreams", MessagePriority.HIGH.name(), null
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
    void testUnpublishMessage() {
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
}
