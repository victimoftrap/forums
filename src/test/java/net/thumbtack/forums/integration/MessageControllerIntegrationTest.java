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
        final ResponseEntity<UserDtoResponse> registerForumOwnerResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerForumOwnerRequest, UserDtoResponse.class
        );
        final String forumOwnerCookie = registerForumOwnerResponse.getHeaders()
                .getFirst(HttpHeaders.SET_COOKIE);

        final RegisterUserDtoRequest registerMessageCreatorRequest = new RegisterUserDtoRequest(
                "MessageCreator", "MessageCreator@email.com", "w3ryStr0nGPa55wD"
        );
        final ResponseEntity<UserDtoResponse> registerMessageCreatorResponse = restTemplate.postForEntity(
                SERVER_URL + "/users", registerMessageCreatorRequest, UserDtoResponse.class
        );
        final String messageCreatorCookie = registerMessageCreatorResponse.getHeaders()
                .getFirst(HttpHeaders.SET_COOKIE);

        final HttpHeaders forumOwnerHttpHeaders = new HttpHeaders();
        forumOwnerHttpHeaders.setContentType(MediaType.APPLICATION_JSON);
        forumOwnerHttpHeaders.add(HttpHeaders.COOKIE, forumOwnerCookie);
        final CreateForumDtoRequest createForumRequest = new CreateForumDtoRequest(
                "ForumName", forumType.name()
        );
        final HttpEntity<Object> createForumHttpEntity = new HttpEntity<>(
                createForumRequest, forumOwnerHttpHeaders
        );
        final ResponseEntity<ForumDtoResponse> createForumResponse = restTemplate.exchange(
                SERVER_URL + "/forums",
                HttpMethod.POST,
                createForumHttpEntity,
                ForumDtoResponse.class
        );

        final CreateMessageDtoRequest createMessageRequest = new CreateMessageDtoRequest(
                "Subject", "Message Body",
                MessagePriority.LOW.name(), Arrays.asList("Greetings", "Hello")
        );
        final HttpHeaders messageCreatorHttpHeaders = new HttpHeaders();
        messageCreatorHttpHeaders.setContentType(MediaType.APPLICATION_JSON);
        messageCreatorHttpHeaders.add(
                HttpHeaders.COOKIE,
                isMessageCreatesForumOwner ? forumOwnerCookie : messageCreatorCookie
        );
        final HttpEntity<Object> createMessageHttpEntity = new HttpEntity<>(
                createMessageRequest, messageCreatorHttpHeaders
        );

        final ResponseEntity<MessageDtoResponse> createMessageResponse = restTemplate.exchange(
                SERVER_URL + "/forums/{forum}/messages",
                HttpMethod.POST,
                createMessageHttpEntity,
                MessageDtoResponse.class,
                createForumResponse.getBody().getId()
        );
        assertEquals(HttpStatus.OK, createMessageResponse.getStatusCode());
        assertEquals(messageState.name(), createMessageResponse.getBody().getState());
    }
}
