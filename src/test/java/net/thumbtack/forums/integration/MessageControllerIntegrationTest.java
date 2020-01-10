package net.thumbtack.forums.integration;

import net.thumbtack.forums.model.User;
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
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class MessageControllerIntegrationTest extends BaseIntegrationEnvironment {
    private RestTemplate restTemplate = new RestTemplate();

    static Stream<Arguments> userAndForumParams() {
        return Stream.of(
                Arguments.arguments(ForumType.MODERATED, true, MessageState.PUBLISHED),
                Arguments.arguments(ForumType.UNMODERATED, true, MessageState.PUBLISHED),
                Arguments.arguments(ForumType.UNMODERATED, false, MessageState.PUBLISHED),
                Arguments.arguments(ForumType.MODERATED, false, MessageState.UNPUBLISHED)
        );
    }

    @ParameterizedTest
    @MethodSource("userAndForumParams")
    void testCreateMessage(
            ForumType forumType, boolean isMessageCreatesForumOwner, MessageState messageState
    ) {
        final RegisterUserDtoRequest registerForumOwnerRequest = new RegisterUserDtoRequest(
                "ForumOwner", "ForumOwner@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerForumOwnerResponse = registerUser(registerForumOwnerRequest);
        final String forumOwnerCookie = registerForumOwnerResponse.getHeaders()
                .getFirst(HttpHeaders.SET_COOKIE);

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerMessageCreatorResponse = registerUser(registerMessageCreatorRequest);
        final String messageCreatorCookie = registerMessageCreatorResponse.getHeaders()
                .getFirst(HttpHeaders.SET_COOKIE);

        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", forumType.name()
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = createForum(createForumRequest, forumOwnerCookie);

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
        );
        final ResponseEntity<MessageDtoResponse> createMessageResponse = createMessage(
                isMessageCreatesForumOwner ? forumOwnerCookie : messageCreatorCookie,
                createForumResponse.getBody().getId(),
                createMessageRequest
        );
        assertEquals(HttpStatus.OK, createMessageResponse.getStatusCode());
        assertNotNull(createMessageResponse.getBody());
        assertEquals(messageState.name(), createMessageResponse.getBody().getState());
    }
}
