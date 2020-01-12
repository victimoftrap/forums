package net.thumbtack.forums.controller;

import net.thumbtack.forums.model.enums.MessagePriority;
import net.thumbtack.forums.model.enums.MessageState;
import net.thumbtack.forums.model.enums.PublicationDecision;
import net.thumbtack.forums.service.MessageService;
import net.thumbtack.forums.dto.requests.message.*;
import net.thumbtack.forums.dto.responses.message.MadeBranchFromCommentDtoResponse;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.dto.responses.message.MessageDtoResponse;
import net.thumbtack.forums.dto.responses.message.EditMessageOrCommentDtoResponse;
import net.thumbtack.forums.dto.responses.message.MessageInfoDtoResponse;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.exception.ValidatedRequestFieldName;

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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    static Stream<Arguments> commentBodyInvalidParams() {
        return Stream.of(
                null,
                Arguments.arguments("")
        );
    }

    @ParameterizedTest
    @MethodSource("commentBodyInvalidParams")
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
                .andExpect(jsonPath("$.errors[0].field").value(ValidatedRequestFieldName.MESSAGE_BODY.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());

        verify(mockMessageService, never())
                .addComment(anyString(), anyInt(), any(CreateCommentDtoRequest.class));
    }

    static Stream<Arguments> createCommentServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.NO_USER_SESSION, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.USER_BANNED, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.MESSAGE_NOT_FOUND, HttpStatus.NOT_FOUND),
                Arguments.arguments(ErrorCode.MESSAGE_NOT_PUBLISHED, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.FORUM_READ_ONLY, HttpStatus.BAD_REQUEST)
        );
    }

    @ParameterizedTest
    @MethodSource("createCommentServiceExceptions")
    void testCreateComment_exceptionsInService_shouldReturnExceptionDto(
            ErrorCode errorCode, HttpStatus httpStatus
    ) throws Exception {
        final CreateCommentDtoRequest request = new CreateCommentDtoRequest("I'm a comment!!");
        when(mockMessageService.addComment(anyString(), anyInt(), any(CreateCommentDtoRequest.class)))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                post("/api/messages/{id}", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().is(httpStatus.value()))
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockMessageService)
                .addComment(anyString(), anyInt(), any(CreateCommentDtoRequest.class));
    }

    @Test
    void testDeleteComment() throws Exception {
        when(mockMessageService.deleteMessage(anyString(), anyInt()))
                .thenReturn(new EmptyDtoResponse());

        mvc.perform(
                delete("/api/messages/{id}", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{}"));

        verify(mockMessageService).deleteMessage(anyString(), anyInt());
    }

    static Stream<Arguments> deleteCommentServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.NO_USER_SESSION, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.USER_PERMANENTLY_BANNED, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.MESSAGE_NOT_FOUND, HttpStatus.NOT_FOUND),
                Arguments.arguments(ErrorCode.FORBIDDEN_OPERATION, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.FORUM_READ_ONLY, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.MESSAGE_HAS_COMMENTS, HttpStatus.BAD_REQUEST)
        );
    }

    @ParameterizedTest
    @MethodSource("deleteCommentServiceExceptions")
    void testDeleteComment_exceptionsInService_shouldReturnExceptionDto(
            ErrorCode errorCode, HttpStatus httpStatus
    ) throws Exception {
        when(mockMessageService.deleteMessage(anyString(), anyInt()))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                delete("/api/messages/{id}", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
        )
                .andExpect(status().is(httpStatus.value()))
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockMessageService).deleteMessage(anyString(), anyInt());
    }

    @Test
    void testEditComment() throws Exception {
        final EditMessageOrCommentDtoRequest request = new EditMessageOrCommentDtoRequest(
                "I'm edited body"
        );
        final EditMessageOrCommentDtoResponse response = new EditMessageOrCommentDtoResponse(
                MessageState.PUBLISHED.name()
        );

        when(mockMessageService.editMessage(anyString(), anyInt(), any(EditMessageOrCommentDtoRequest.class)))
                .thenReturn(response);

        mvc.perform(
                put("/api/messages/{id}", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.state").value(response.getState()));

        verify(mockMessageService)
                .editMessage(anyString(), anyInt(), any(EditMessageOrCommentDtoRequest.class));
    }

    @ParameterizedTest
    @MethodSource("commentBodyInvalidParams")
    void testEditComment_exceptionsInService_shouldReturnExceptionDto(String invalidBody) throws Exception {
        final EditMessageOrCommentDtoRequest request = new EditMessageOrCommentDtoRequest(
                invalidBody
        );

        mvc.perform(
                put("/api/messages/{id}", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.INVALID_REQUEST_DATA.name()))
                .andExpect(jsonPath("$.errors[0].field").value(ValidatedRequestFieldName.MESSAGE_BODY.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());

        verifyZeroInteractions(mockMessageService);
    }

    static Stream<Arguments> editCommentServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.NO_USER_SESSION, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.USER_BANNED, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.MESSAGE_NOT_FOUND, HttpStatus.NOT_FOUND),
                Arguments.arguments(ErrorCode.FORBIDDEN_OPERATION, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.FORUM_READ_ONLY, HttpStatus.BAD_REQUEST)
        );
    }

    @ParameterizedTest
    @MethodSource("editCommentServiceExceptions")
    void testEditComment_exceptionsInService_shouldReturnExceptionDto(
            ErrorCode errorCode, HttpStatus httpStatus
    ) throws Exception {
        final EditMessageOrCommentDtoRequest request = new EditMessageOrCommentDtoRequest(
                "I'm edited body"
        );
        when(mockMessageService.editMessage(anyString(), anyInt(), any(EditMessageOrCommentDtoRequest.class)))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                put("/api/messages/{id}", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().is(httpStatus.value()))
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockMessageService)
                .editMessage(anyString(), anyInt(), any(EditMessageOrCommentDtoRequest.class));
    }

    @Test
    void testChangePriority() throws Exception {
        final ChangeMessagePriorityDtoRequest request = new ChangeMessagePriorityDtoRequest("HIGH");

        when(mockMessageService
                .changeMessagePriority(
                        anyString(), anyInt(), any(ChangeMessagePriorityDtoRequest.class)
                )
        )
                .thenReturn(new EmptyDtoResponse());

        mvc.perform(
                put("/api/messages/{id}/priority", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{}"));

        verify(mockMessageService)
                .changeMessagePriority(
                        anyString(), anyInt(), any(ChangeMessagePriorityDtoRequest.class)
                );
    }

    static Stream<Arguments> changePriorityInvalidArguments() {
        return Stream.of(
                null,
                Arguments.arguments(""),
                Arguments.arguments("SUPER-DUPER")
        );
    }

    @ParameterizedTest
    @MethodSource("changePriorityInvalidArguments")
    void testChangePriority_invalidPriorityParam_shouldReturnExceptionDto(String invalidPriority) throws Exception {
        final ChangeMessagePriorityDtoRequest request = new ChangeMessagePriorityDtoRequest(invalidPriority);

        when(mockMessageService
                .changeMessagePriority(
                        anyString(), anyInt(), any(ChangeMessagePriorityDtoRequest.class)
                )
        )
                .thenReturn(new EmptyDtoResponse());

        mvc.perform(
                put("/api/messages/{id}/priority", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.INVALID_REQUEST_DATA.name()))
                .andExpect(jsonPath("$.errors[0].field").value(ValidatedRequestFieldName.MESSAGE_PRIORITY.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());

        verifyZeroInteractions(mockMessageService);
    }

    static Stream<Arguments> changePriorityServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.NO_USER_SESSION, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.USER_BANNED, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.MESSAGE_NOT_FOUND, HttpStatus.NOT_FOUND),
                Arguments.arguments(ErrorCode.UNABLE_OPERATION_FOR_COMMENT, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.FORBIDDEN_OPERATION, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.FORUM_READ_ONLY, HttpStatus.BAD_REQUEST)
        );
    }

    @ParameterizedTest
    @MethodSource("changePriorityServiceExceptions")
    void testChangePriority_exceptionsInService_shouldReturnExceptionDto(
            ErrorCode errorCode, HttpStatus httpStatus
    ) throws Exception {
        final ChangeMessagePriorityDtoRequest request = new ChangeMessagePriorityDtoRequest("HIGH");
        when(mockMessageService
                .changeMessagePriority(
                        anyString(), anyInt(),
                        any(ChangeMessagePriorityDtoRequest.class)
                )
        )
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                put("/api/messages/{message}/priority", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().is(httpStatus.value()))
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockMessageService)
                .changeMessagePriority(
                        anyString(), anyInt(),
                        any(ChangeMessagePriorityDtoRequest.class)
                );
    }

    @Test
    void testMadeNewBranch() throws Exception {
        final MadeBranchFromCommentDtoRequest request = new MadeBranchFromCommentDtoRequest(
                "Subject", MessagePriority.LOW.name(), Collections.emptyList()
        );
        final MadeBranchFromCommentDtoResponse response = new MadeBranchFromCommentDtoResponse(123);

        when(mockMessageService
                .newBranchFromComment(anyString(), anyInt(), any(MadeBranchFromCommentDtoRequest.class))
        )
                .thenReturn(response);

        mvc.perform(
                put("/api/messages/{id}/up", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.getId()));

        verify(mockMessageService)
                .newBranchFromComment(anyString(), anyInt(), any(MadeBranchFromCommentDtoRequest.class));
    }

    static Stream<Arguments> newBranchInvalidParams() {
        return Stream.of(
                Arguments.arguments("", MessagePriority.NORMAL.name(), ValidatedRequestFieldName.MESSAGE_SUBJECT),
                Arguments.arguments(null, MessagePriority.NORMAL.name(), ValidatedRequestFieldName.MESSAGE_SUBJECT),
                Arguments.arguments("Subject", "", ValidatedRequestFieldName.MESSAGE_PRIORITY),
                Arguments.arguments("Subject", "EXTRA_HIGH", ValidatedRequestFieldName.MESSAGE_PRIORITY)
        );
    }

    @ParameterizedTest
    @MethodSource("newBranchInvalidParams")
    void testMadeNewBranch_invalidParams_shouldReturnExceptionDto(
            String subject, String priority, ValidatedRequestFieldName errorFieldName
    ) throws Exception {
        final MadeBranchFromCommentDtoRequest request = new MadeBranchFromCommentDtoRequest(
                subject, priority, Collections.emptyList()
        );
        final MadeBranchFromCommentDtoResponse response = new MadeBranchFromCommentDtoResponse(123);

        when(mockMessageService
                .newBranchFromComment(anyString(), anyInt(), any(MadeBranchFromCommentDtoRequest.class))
        )
                .thenReturn(response);

        mvc.perform(
                put("/api/messages/{id}/up", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.INVALID_REQUEST_DATA.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorFieldName.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());

        verifyZeroInteractions(mockMessageService);
    }

    @Test
    void testMadeNewBranch_emptyTagNames_shouldReturnExceptionDto() throws Exception {
        final MadeBranchFromCommentDtoRequest request = new MadeBranchFromCommentDtoRequest(
                "Subject", MessagePriority.LOW.name(), Arrays.asList("", "normal")
        );
        final MadeBranchFromCommentDtoResponse response = new MadeBranchFromCommentDtoResponse(123);

        when(mockMessageService
                .newBranchFromComment(anyString(), anyInt(), any(MadeBranchFromCommentDtoRequest.class))
        )
                .thenReturn(response);

        mvc.perform(
                put("/api/messages/{id}/up", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.INVALID_REQUEST_DATA.name()))
                .andExpect(jsonPath("$.errors[0].field").value("tags[0]"))
                .andExpect(jsonPath("$.errors[0].message").exists());

        verify(mockMessageService, never())
                .newBranchFromComment(anyString(), anyInt(), any(MadeBranchFromCommentDtoRequest.class));
    }

    static Stream<Arguments> newBranchServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.NO_USER_SESSION, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.USER_BANNED, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.MESSAGE_NOT_FOUND, HttpStatus.NOT_FOUND),
                Arguments.arguments(ErrorCode.MESSAGE_ALREADY_BRANCH, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.FORUM_READ_ONLY, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.FORBIDDEN_OPERATION, HttpStatus.BAD_REQUEST)
        );
    }

    @ParameterizedTest
    @MethodSource("newBranchServiceExceptions")
    void testMadeNewBranch_exceptionsInService_shouldReturnExceptionDto(
            ErrorCode errorCode, HttpStatus httpStatus
    ) throws Exception {
        final MadeBranchFromCommentDtoRequest request = new MadeBranchFromCommentDtoRequest(
                "Subject", MessagePriority.LOW.name(), Collections.emptyList()
        );
        when(mockMessageService
                .newBranchFromComment(anyString(), anyInt(), any(MadeBranchFromCommentDtoRequest.class))
        )
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                put("/api/messages/{id}/up", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().is(httpStatus.value()))
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockMessageService)
                .newBranchFromComment(anyString(), anyInt(), any(MadeBranchFromCommentDtoRequest.class));
    }

    @Test
    void testPublishMessage() throws Exception {
        final PublicationDecisionDtoRequest request = new PublicationDecisionDtoRequest(
                PublicationDecision.YES.name()
        );
        when(mockMessageService.publish(anyString(), anyInt(), any(PublicationDecisionDtoRequest.class)))
                .thenReturn(new EmptyDtoResponse());

        mvc.perform(
                put("/api/messages/{id}/publish", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{}"));

        verify(mockMessageService)
                .publish(anyString(), anyInt(), any(PublicationDecisionDtoRequest.class));
    }

    static Stream<Arguments> publicationInvalidParams() {
        return Stream.of(
                null,
                Arguments.arguments(""),
                Arguments.arguments("MAYBE TOMORROW")
        );
    }

    @ParameterizedTest
    @MethodSource("publicationInvalidParams")
    void testPublishMessage_invalidParams_shouldReturnExceptionDto(String invalidDecision) throws Exception {
        final PublicationDecisionDtoRequest request = new PublicationDecisionDtoRequest(invalidDecision);
        when(mockMessageService.publish(anyString(), anyInt(), any(PublicationDecisionDtoRequest.class)))
                .thenReturn(new EmptyDtoResponse());

        mvc.perform(
                put("/api/messages/{id}/publish", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode")
                        .value(ErrorCode.INVALID_REQUEST_DATA.name())
                )
                .andExpect(jsonPath("$.errors[0].field")
                        .value(ValidatedRequestFieldName.PUBLICATION_DECISION.getName())
                )
                .andExpect(jsonPath("$.errors[0].message").exists());

        verifyZeroInteractions(mockMessageService);
    }

    static Stream<Arguments> publicationServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.NO_USER_SESSION, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.USER_PERMANENTLY_BANNED, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.MESSAGE_NOT_FOUND, HttpStatus.NOT_FOUND),
                Arguments.arguments(ErrorCode.FORUM_READ_ONLY, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.FORBIDDEN_OPERATION, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.MESSAGE_ALREADY_PUBLISHED, HttpStatus.BAD_REQUEST)
        );
    }

    @ParameterizedTest
    @MethodSource("publicationServiceExceptions")
    void testPublishMessage_exceptionsInService_shouldReturnExceptionDto(
            ErrorCode errorCode, HttpStatus httpStatus
    ) throws Exception {
        final PublicationDecisionDtoRequest request = new PublicationDecisionDtoRequest(
                PublicationDecision.YES.name()
        );
        when(mockMessageService.publish(anyString(), anyInt(), any(PublicationDecisionDtoRequest.class)))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                put("/api/messages/{id}/publish", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().is(httpStatus.value()))
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockMessageService)
                .publish(anyString(), anyInt(), any(PublicationDecisionDtoRequest.class));
    }

    @Test
    void testRateMessage() throws Exception {
        final RateMessageDtoRequest request = new RateMessageDtoRequest(5);
        when(mockMessageService.rate(anyString(), anyInt(), any(RateMessageDtoRequest.class)))
                .thenReturn(new EmptyDtoResponse());

        mvc.perform(
                post("/api/messages/{id}/rating", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{}"));

        verify(mockMessageService)
                .rate(anyString(), anyInt(), any(RateMessageDtoRequest.class));
    }

    static Stream<Arguments> rateMessageServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.NO_USER_SESSION, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.USER_PERMANENTLY_BANNED, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.MESSAGE_NOT_PUBLISHED, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.MESSAGE_CREATOR_RATES_HIS_MESSAGE, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.MESSAGE_NOT_FOUND, HttpStatus.NOT_FOUND)
        );
    }

    @ParameterizedTest
    @MethodSource("rateMessageServiceExceptions")
    void testRateMessage_exceptionsInService_shouldReturnExceptionDto(
            ErrorCode errorCode, HttpStatus httpStatus
    ) throws Exception {
        final RateMessageDtoRequest request = new RateMessageDtoRequest(5);
        when(mockMessageService.rate(anyString(), anyInt(), any(RateMessageDtoRequest.class)))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                post("/api/messages/{id}/rating", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().is(httpStatus.value()))
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockMessageService)
                .rate(anyString(), anyInt(), any(RateMessageDtoRequest.class));
    }

    @Test
    void testGetMessage() throws Exception {
        final MessageInfoDtoResponse response = new MessageInfoDtoResponse(
                1,
                "Creator",
                "Subject",
                Arrays.asList("Hello", "Hi", "Privyet"),
                MessagePriority.HIGH.name(),
                Arrays.asList("Greetings", "Meet"),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                4,
                2,
                Collections.emptyList()
        );

        when(mockMessageService
                .getMessage(anyString(), anyInt(), anyBoolean(), anyBoolean(), anyBoolean(), anyString())
        )
                .thenReturn(response);

        MvcResult result = mvc.perform(
                get("/api/messages/{id}", 123)
                        .param("allversions", "true")
                        .param("nocomments", "true")
                        .param("unpublished", "false")
                        .param("order", "ASC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        final MessageInfoDtoResponse actualResponse = mapper.readValue(
                result.getResponse().getContentAsString(),
                MessageInfoDtoResponse.class
        );
        assertEquals(response, actualResponse);

        verify(mockMessageService)
                .getMessage(anyString(), anyInt(), anyBoolean(), anyBoolean(), anyBoolean(), anyString());
    }

    @Test
    void testGetMessage_notExistsSomeQueryParams_shouldApplyDefaultSettingsInService() throws Exception {
        final MessageInfoDtoResponse response = new MessageInfoDtoResponse(
                1,
                "Creator",
                "Subject",
                Arrays.asList("Hello", "Hi", "Privyet"),
                MessagePriority.HIGH.name(),
                Arrays.asList("Greetings", "Meet"),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                4,
                2,
                Collections.emptyList()
        );

        when(mockMessageService
                .getMessage(anyString(), anyInt(), anyBoolean(), eq(null), anyBoolean(), eq(null))
        )
                .thenReturn(response);

        MvcResult result = mvc.perform(
                get("/api/messages/{id}", 123)
                        .param("allversions", "true")
                        .param("unpublished", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        final MessageInfoDtoResponse actualResponse = mapper.readValue(
                result.getResponse().getContentAsString(),
                MessageInfoDtoResponse.class
        );
        assertEquals(response, actualResponse);

        verify(mockMessageService)
                .getMessage(anyString(), anyInt(), anyBoolean(), eq(null), anyBoolean(), eq(null));
    }

    @Test
    void testGetMessage_notExistsAllQueryParams_shouldApplyDefaultSettingsInService() throws Exception {
        final MessageInfoDtoResponse response = new MessageInfoDtoResponse(
                1,
                "Creator",
                "Subject",
                Arrays.asList("Hello", "Hi", "Privyet"),
                MessagePriority.HIGH.name(),
                Arrays.asList("Greetings", "Meet"),
                LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                4,
                2,
                Collections.emptyList()
        );

        when(mockMessageService
                .getMessage(anyString(), anyInt(), eq(null), eq(null), eq(null), eq(null))
        )
                .thenReturn(response);

        MvcResult result = mvc.perform(
                get("/api/messages/{id}", 123)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        final MessageInfoDtoResponse actualResponse = mapper.readValue(
                result.getResponse().getContentAsString(),
                MessageInfoDtoResponse.class
        );
        assertEquals(response, actualResponse);

        verify(mockMessageService)
                .getMessage(anyString(), anyInt(), eq(null), eq(null), eq(null), eq(null));
    }

    @Test
    void testGetMessage_invalidMessageOrder_shouldReturnExceptionDto() throws Exception {
        final String invalidOrder = "KAK_NADO";

        mvc.perform(
                get("/api/messages/{id}", 123)
                        .param("order", invalidOrder)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode")
                        .value(ErrorCode.INVALID_REQUEST_DATA.name())
                )
                .andExpect(jsonPath("$.errors[0].field").value("order"))
                .andExpect(jsonPath("$.errors[0].message").exists());

        verifyZeroInteractions(mockMessageService);
    }

    static Stream<Arguments> getMessageServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.NO_USER_SESSION, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.MESSAGE_NOT_FOUND, HttpStatus.NOT_FOUND),
                Arguments.arguments(ErrorCode.UNABLE_OPERATION_FOR_COMMENT, HttpStatus.BAD_REQUEST)
        );
    }

    @ParameterizedTest
    @MethodSource("getMessageServiceExceptions")
    void testGetMessage_exceptionsInService_shouldReturnExceptionDto(
            ErrorCode errorCode, HttpStatus httpStatus
    ) throws Exception {
        when(mockMessageService
                .getMessage(anyString(), anyInt(), anyBoolean(), anyBoolean(), anyBoolean(), anyString())
        )
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                get("/api/messages/{id}", 123)
                        .param("allversions", "true")
                        .param("nocomments", "true")
                        .param("unpublished", "false")
                        .param("order", "DESC")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
        )
                .andExpect(status().is(httpStatus.value()))
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockMessageService)
                .getMessage(anyString(), anyInt(), anyBoolean(), anyBoolean(), anyBoolean(), anyString());
    }
}
