package net.thumbtack.forums.integration;

import net.thumbtack.forums.dto.requests.message.*;
import net.thumbtack.forums.dto.requests.user.LoginUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.requests.forum.CreateForumDtoRequest;
import net.thumbtack.forums.dto.requests.user.UpdatePasswordDtoRequest;
import net.thumbtack.forums.dto.responses.forum.ForumInfoListDtoResponse;
import net.thumbtack.forums.dto.responses.message.EditMessageOrCommentDtoResponse;
import net.thumbtack.forums.dto.responses.message.MadeBranchFromCommentDtoResponse;
import net.thumbtack.forums.dto.responses.message.MessageInfoDtoResponse;
import net.thumbtack.forums.dto.responses.user.UserDtoResponse;
import net.thumbtack.forums.dto.responses.user.UserDetailsListDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumInfoDtoResponse;
import net.thumbtack.forums.dto.responses.message.MessageDtoResponse;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

public class BaseIntegrationEnvironment {
    protected static final String SERVER_URL = "http://localhost:8080/api";
    protected RestTemplate restTemplate = new RestTemplate();
    protected ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    public void clear() {
        restTemplate.postForObject(
                SERVER_URL + "/debug/clear", null, Void.class
        );
    }

    protected <T, V> ResponseEntity<T> executeRequest(String url, HttpMethod method,
                                                      V request, Class<T> response) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<V> httpEntity = new HttpEntity<>(request, httpHeaders);

        return restTemplate.exchange(url, method, httpEntity, response);
    }

    protected <T, V> ResponseEntity<T> executeRequest(String url, HttpMethod method,
                                                      String token, V request, Class<T> response) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, token);
        HttpEntity<V> httpEntity = new HttpEntity<>(request, httpHeaders);

        return restTemplate.exchange(url, method, httpEntity, response);
    }

    protected String getSessionTokenFromHeaders(ResponseEntity<?> responseEntity) {
        return responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
    }

    protected ResponseEntity<UserDtoResponse> registerUser(RegisterUserDtoRequest request) {
        return executeRequest(
                SERVER_URL + "/users", HttpMethod.POST,
                request, UserDtoResponse.class
        );
    }

    protected ResponseEntity<EmptyDtoResponse> deleteUser(String token) {
        return executeRequest(
                SERVER_URL + "/users",
                HttpMethod.DELETE,
                token,
                null,
                EmptyDtoResponse.class
        );
    }

    protected ResponseEntity<UserDtoResponse> loginUser(LoginUserDtoRequest request) {
        return executeRequest(
                SERVER_URL + "/sessions", HttpMethod.POST,
                request, UserDtoResponse.class
        );
    }

    protected ResponseEntity<EmptyDtoResponse> logoutUser(String token) {
        return executeRequest(
                SERVER_URL + "/sessions",
                HttpMethod.DELETE,
                token,
                null,
                EmptyDtoResponse.class
        );
    }

    protected ResponseEntity<UserDetailsListDtoResponse> getUsers(String token) {
        return executeRequest(
                SERVER_URL + "/users", HttpMethod.GET,
                token, null, UserDetailsListDtoResponse.class
        );
    }

    protected ResponseEntity<UserDtoResponse> updatePassword(String token, UpdatePasswordDtoRequest request) {
        return executeRequest(
                SERVER_URL + "/users", HttpMethod.PUT, token,
                request, UserDtoResponse.class
        );
    }

    protected ResponseEntity<EmptyDtoResponse> madeSuperuser(String token, int userId) {
        return executeRequest(
                String.format(SERVER_URL + "/users/%d/super", userId),
                HttpMethod.PUT, token,
                null, EmptyDtoResponse.class
        );
    }

    protected ResponseEntity<EmptyDtoResponse> banUser(String token, int userId) {
        return executeRequest(
                String.format(SERVER_URL + "/users/%d/restrict", userId),
                HttpMethod.POST, token,
                null, EmptyDtoResponse.class
        );
    }

    protected ResponseEntity<ForumDtoResponse> createForum(CreateForumDtoRequest request, String token) {
        return executeRequest(
                SERVER_URL + "/forums", HttpMethod.POST,
                token, request, ForumDtoResponse.class
        );
    }

    protected ResponseEntity<EmptyDtoResponse> deleteForum(String token, int forumId) {
        return executeRequest(
                String.format(SERVER_URL + "/forums/%d", forumId),
                HttpMethod.DELETE, token,
                null, EmptyDtoResponse.class
        );
    }

    protected ResponseEntity<ForumInfoDtoResponse> getForum(String token, int forumId) {
        return executeRequest(
                String.format(SERVER_URL + "/forums/%d", forumId),
                HttpMethod.GET, token,
                null, ForumInfoDtoResponse.class
        );
    }

    protected ResponseEntity<ForumInfoListDtoResponse> getForums(String token) {
        return executeRequest(
                SERVER_URL + "/forums",
                HttpMethod.GET, token,
                null, ForumInfoListDtoResponse.class
        );
    }

    protected ResponseEntity<MessageDtoResponse> createMessage(String token,
                                                               int forumId, CreateMessageDtoRequest request) {
        return executeRequest(
                String.format(SERVER_URL + "/forums/%d/messages", forumId),
                HttpMethod.POST, token,
                request, MessageDtoResponse.class
        );
    }

    protected ResponseEntity<MessageDtoResponse> createComment(String token,
                                                               int messageId, CreateCommentDtoRequest request) {
        return executeRequest(
                String.format(SERVER_URL + "/messages/%d", messageId),
                HttpMethod.POST, token,
                request, MessageDtoResponse.class
        );
    }

    protected ResponseEntity<EmptyDtoResponse> deleteMessage(String token, int messageId) {
        return executeRequest(
                String.format(SERVER_URL + "/messages/%d", messageId),
                HttpMethod.DELETE, token,
                null, EmptyDtoResponse.class
        );
    }

    protected ResponseEntity<EditMessageOrCommentDtoResponse> editMessage(
            String token, int messageId, EditMessageOrCommentDtoRequest request) {
        return executeRequest(
                String.format(SERVER_URL + "/messages/%d", messageId),
                HttpMethod.PUT, token, request, EditMessageOrCommentDtoResponse.class
        );
    }

    protected ResponseEntity<EmptyDtoResponse> changePriority(
            String token, int messageId, ChangeMessagePriorityDtoRequest request) {
        return executeRequest(
                String.format(SERVER_URL + "/messages/%d/priority", messageId),
                HttpMethod.PUT, token, request, EmptyDtoResponse.class
        );
    }

    protected ResponseEntity<MadeBranchFromCommentDtoResponse> newBranchFromComment(
            String token, int commentId, MadeBranchFromCommentDtoRequest request) {
        return executeRequest(
                String.format(SERVER_URL + "/messages/%d/up", commentId),
                HttpMethod.PUT, token, request, MadeBranchFromCommentDtoResponse.class
        );
    }

    protected ResponseEntity<EmptyDtoResponse> publishMessage(
            String token, int messageId, PublicationDecisionDtoRequest request) {
        return executeRequest(
                String.format(SERVER_URL + "/messages/%d/publish", messageId),
                HttpMethod.PUT, token, request, EmptyDtoResponse.class
        );
    }

    protected ResponseEntity<EmptyDtoResponse> rateMessage(
            String token, int messageId, RateMessageDtoRequest rateMessageDtoRequest) {
        return executeRequest(
                String.format(SERVER_URL + "/messages/%d/rating", messageId),
                HttpMethod.POST, token,
                null, EmptyDtoResponse.class
        );
    }

    protected ResponseEntity<MessageInfoDtoResponse> getMessage(String token, int messageId) {
        return executeRequest(
                String.format(SERVER_URL + "/messages/%d", messageId),
                HttpMethod.GET, token,
                null, MessageInfoDtoResponse.class
        );
    }

    protected ResponseEntity<MessageInfoDtoResponse> getMessage(
            String token, int messageId, Boolean allVersions, Boolean noComments, Boolean unpublished, String order) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.add(HttpHeaders.COOKIE, token);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpHeaders);

        Map<String, Object> params = new HashMap<>();
        params.put("allversions", allVersions);
        params.put("nocomments", noComments);
        params.put("unpublished", unpublished);
        params.put("order", order);

        return restTemplate.exchange(
                SERVER_URL + "/messages/{id}?allversions={av}&nocomments={nc}&unpublished={up}&order={o}",
                HttpMethod.GET,
                httpEntity,
                MessageInfoDtoResponse.class,
                params
        );
    }
}
