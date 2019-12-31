package net.thumbtack.forums.controller;

import net.thumbtack.forums.exception.RequestFieldName;
import net.thumbtack.forums.model.enums.MessageState;
import net.thumbtack.forums.service.MessageService;
import net.thumbtack.forums.dto.requests.message.CreateCommentDtoRequest;
import net.thumbtack.forums.dto.responses.message.MessageDtoResponse;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.Cookie;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = MessageController.class)
@Import(ServerConfigurationProperties.class)
class MessageControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private MessageService mockMessageService;

    private final String COOKIE_NAME = "JAVASESSIONID";
    private final String COOKIE_VALUE = UUID.randomUUID().toString();

    @Test
    void testCreateComment() throws Exception {
        final CreateCommentDtoRequest request = new CreateCommentDtoRequest("I'm a body!!!");
        final MessageDtoResponse response = new MessageDtoResponse(
                147258, MessageState.UNPUBLISHED.name()
        );

        when(mockMessageService.addComment(anyString(), anyInt(), any(CreateCommentDtoRequest.class)))
                .thenReturn(response);

        mvc.perform(
                post("/api/messages/{id}", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.state").value(response.getState()));

        verify(mockMessageService)
                .addComment(anyString(), anyInt(), any(CreateCommentDtoRequest.class));
    }

    static Stream<Arguments> createCommentInvalidParams() {
        return Stream.of(
                null,
                Arguments.arguments("")
        );
    }

    @ParameterizedTest
    @MethodSource("createCommentInvalidParams")
    void testCreateComment_invalidBodyParams_shouldReturnExceptionDto(String invalidBody) throws Exception {
        final CreateCommentDtoRequest request = new CreateCommentDtoRequest(invalidBody);
        mvc.perform(
                post("/api/messages/{id}", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.INVALID_REQUEST_DATA.name()))
                .andExpect(jsonPath("$.errors[0].field").value(RequestFieldName.MESSAGE_BODY.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());

        verify(mockMessageService, never())
                .addComment(anyString(), anyInt(), any(CreateCommentDtoRequest.class));
    }
}