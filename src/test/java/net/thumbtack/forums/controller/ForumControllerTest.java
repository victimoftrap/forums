package net.thumbtack.forums.controller;

import net.thumbtack.forums.dto.responses.message.CommentInfoDtoResponse;
import net.thumbtack.forums.dto.responses.message.ListMessageInfoDtoResponse;
import net.thumbtack.forums.dto.responses.message.MessageInfoDtoResponse;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.model.enums.MessagePriority;
import net.thumbtack.forums.model.enums.MessageState;
import net.thumbtack.forums.service.ForumService;
import net.thumbtack.forums.service.MessageService;
import net.thumbtack.forums.dto.requests.forum.CreateForumDtoRequest;
import net.thumbtack.forums.dto.requests.message.CreateMessageDtoRequest;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumInfoDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumInfoListDtoResponse;
import net.thumbtack.forums.dto.responses.message.MessageDtoResponse;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ValidatedRequestFieldName;
import net.thumbtack.forums.exception.ServerException;
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
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = ForumController.class)
@Import(ServerConfigurationProperties.class)
class ForumControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private ForumService mockForumService;

    @MockBean
    private MessageService mockMessageService;

    private final String COOKIE_NAME = "JAVASESSIONID";
    private final String COOKIE_VALUE = UUID.randomUUID().toString();

    @Test
    void testCreateForum() throws Exception {
        final CreateForumDtoRequest request = new CreateForumDtoRequest(
                "testForum", ForumType.UNMODERATED.name()
        );
        final ForumDtoResponse expectedResponse = new ForumDtoResponse(
                123, request.getName(), request.getType()
        );
        when(mockForumService.createForum(anyString(), any(CreateForumDtoRequest.class)))
                .thenReturn(expectedResponse);

        mvc.perform(
                post("/api/forums")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(expectedResponse.getId()))
                .andExpect(jsonPath("$.name").value(expectedResponse.getName()))
                .andExpect(jsonPath("$.type").value(expectedResponse.getType()));

        verify(mockForumService)
                .createForum(anyString(), any(CreateForumDtoRequest.class));
    }

    static Stream<Arguments> createForumParamsInvalidParams() {
        return Stream.of(
                Arguments.arguments(
                        null,
                        ForumType.UNMODERATED.name(),
                        ValidatedRequestFieldName.FORUM_NAME
                ),
                Arguments.arguments(
                        "",
                        ForumType.UNMODERATED.name(),
                        ValidatedRequestFieldName.FORUM_NAME
                ),
                Arguments.arguments(
                        "0123456789_0123456789_0123456789_0123456789_0123456789",
                        ForumType.UNMODERATED.name(),
                        ValidatedRequestFieldName.FORUM_NAME
                ),
                Arguments.arguments(
                        "Norman Name",
                        null,
                        ValidatedRequestFieldName.FORUM_TYPE
                )
        );
    }

    @ParameterizedTest
    @MethodSource("createForumParamsInvalidParams")
    void testCreateForum_wrongForumParams_shouldReturnExceptionDto(
            String forumName, String forumType, ValidatedRequestFieldName requestFieldName
    ) throws Exception {
        final CreateForumDtoRequest request = new CreateForumDtoRequest(
                forumName, forumType
        );
        final ForumDtoResponse expectedResponse = new ForumDtoResponse(
                123, forumName, forumType
        );
        when(mockForumService.createForum(anyString(), any(CreateForumDtoRequest.class)))
                .thenReturn(expectedResponse);

        mvc.perform(
                post("/api/forums")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.INVALID_REQUEST_DATA.name()))
                .andExpect(jsonPath("$.errors[0].field").value(requestFieldName.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());
        verifyZeroInteractions(mockForumService);
    }

    static Stream<Arguments> createForumServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR),
                Arguments.arguments(ErrorCode.WRONG_SESSION_TOKEN),
                Arguments.arguments(ErrorCode.FORUM_NAME_ALREADY_USED),
                Arguments.arguments(ErrorCode.USER_BANNED)
        );
    }

    @ParameterizedTest
    @MethodSource("createForumServiceExceptions")
    void testCreateForum_exceptionsInService_shouldReturnExceptionDto(ErrorCode errorCode) throws Exception {
        final CreateForumDtoRequest request = new CreateForumDtoRequest(
                "testForum", ForumType.UNMODERATED.name()
        );
        when(mockForumService.createForum(anyString(), any(CreateForumDtoRequest.class)))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                post("/api/forums")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockForumService)
                .createForum(anyString(), any(CreateForumDtoRequest.class));
    }

    @Test
    void testDeleteForum() throws Exception {
        final int forumId = 132;
        when(mockForumService.deleteForum(anyString(), anyInt()))
                .thenReturn(new EmptyDtoResponse());

        mvc.perform(
                delete("/api/forums/{forum_id}", forumId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().string("{}"));

        verify(mockForumService).deleteForum(anyString(), anyInt());
    }

    static Stream<Arguments> deleteForumServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR),
                Arguments.arguments(ErrorCode.WRONG_SESSION_TOKEN),
                Arguments.arguments(ErrorCode.USER_PERMANENTLY_BANNED),
                Arguments.arguments(ErrorCode.FORUM_NOT_FOUND),
                Arguments.arguments(ErrorCode.FORBIDDEN_OPERATION)
        );
    }

    @ParameterizedTest
    @MethodSource("deleteForumServiceExceptions")
    void testDeleteForum_exceptionsInService_shouldReturnExceptionDto(ErrorCode errorCode) throws Exception {
        final int forumId = 132;
        when(mockForumService.deleteForum(anyString(), anyInt()))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                delete("/api/forums/{forum_id}", forumId)
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockForumService).deleteForum(anyString(), anyInt());
    }

    @Test
    void testGetForumById() throws Exception {
        final int forumId = 132;
        final ForumInfoDtoResponse response = new ForumInfoDtoResponse(
                1234, "ForumName", "Unmoderated",
                "OwnerName", false, 16, 23
        );
        when(mockForumService.getForum(anyString(), anyInt()))
                .thenReturn(response);

        mvc.perform(
                get("/api/forums/{forum_id}", forumId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.name").value(response.getName()))
                .andExpect(jsonPath("$.type").value(response.getType()))
                .andExpect(jsonPath("$.creator").value(response.getCreatorName()))
                .andExpect(jsonPath("$.readonly").value(response.isReadonly()))
                .andExpect(jsonPath("$.messageCount").value(response.getMessageCount()))
                .andExpect(jsonPath("$.commentCount").value(response.getCommentCount()));

        verify(mockForumService).getForum(anyString(), anyInt());
    }

    static Stream<Arguments> getForumServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR),
                Arguments.arguments(ErrorCode.WRONG_SESSION_TOKEN),
                Arguments.arguments(ErrorCode.FORUM_NOT_FOUND)
        );
    }

    @ParameterizedTest
    @MethodSource("getForumServiceExceptions")
    void testGetForumById_exceptionsInService_shouldReturnExceptionDto(ErrorCode errorCode) throws Exception {
        final int forumId = 132;
        when(mockForumService.getForum(anyString(), anyInt()))
                .thenThrow(new ServerException(errorCode));
        mvc.perform(
                get("/api/forums/{forum_id}", forumId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockForumService).getForum(anyString(), anyInt());
    }

    @Test
    void testGetAllForums() throws Exception {
        final List<ForumInfoDtoResponse> forumList = new ArrayList<>();
        forumList.add(new ForumInfoDtoResponse(
                        123, "Forum1", "UNMODERATED",
                        "Owner1", false, 4, 8
                )
        );
        forumList.add(new ForumInfoDtoResponse(
                        124, "Forum2", "MODERATED",
                        "Owner2", true, 15, 16
                )
        );
        forumList.add(new ForumInfoDtoResponse(
                        125, "Forum3", "UNMODERATED",
                        "Owner3", false, 23, 42
                )
        );
        final ForumInfoListDtoResponse expectedResponse = new ForumInfoListDtoResponse(forumList);

        when(mockForumService.getForums(anyString()))
                .thenReturn(expectedResponse);

        final MvcResult mvcResult = mvc.perform(
                get("/api/forums")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.forums", hasSize(3)))
                .andReturn();

        final ForumInfoListDtoResponse actualResponse = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ForumInfoListDtoResponse.class
        );
        assertEquals(expectedResponse, actualResponse);
        verify(mockForumService).getForums(anyString());
    }

    @Test
    void testGetAllForums_noForumsExists_shouldReturnEmptyList() throws Exception {
        final ForumInfoListDtoResponse response = new ForumInfoListDtoResponse(
                Collections.emptyList()
        );

        when(mockForumService.getForums(anyString()))
                .thenReturn(response);

        mvc.perform(
                get("/api/forums")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.forums", hasSize(0)));

        verify(mockForumService).getForums(anyString());
    }

    @Test
    void testGetAllForums_userNotFound_shouldReturnExceptionDto() throws Exception {
        when(mockForumService.getForums(anyString()))
                .thenThrow(new ServerException(ErrorCode.WRONG_SESSION_TOKEN));

        mvc.perform(
                get("/api/forums")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.WRONG_SESSION_TOKEN.name()))
                .andExpect(jsonPath("$.errors[0].field").value(ErrorCode.WRONG_SESSION_TOKEN.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(ErrorCode.WRONG_SESSION_TOKEN.getMessage()));

        verify(mockForumService).getForums(anyString());
    }

    @Test
    void testCreateMessage() throws Exception {
        final CreateMessageDtoRequest request = new CreateMessageDtoRequest(
                "Subject", "Body", MessagePriority.NORMAL.name(), Collections.emptyList()
        );
        final MessageDtoResponse response = new MessageDtoResponse(
                147258, MessageState.UNPUBLISHED.name()
        );

        when(mockMessageService.addMessage(anyString(), anyInt(), any(CreateMessageDtoRequest.class)))
                .thenReturn(response);

        mvc.perform(
                post("/api/forums/{forum_id}/messages", 123)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.state").value(response.getState()));

        verify(mockMessageService)
                .addMessage(anyString(), anyInt(), any(CreateMessageDtoRequest.class));
    }

    @Test
    void testCreateMessage_noMessagePriorityAndTags_successfullyCreated() throws Exception {
        final CreateMessageDtoRequest request = new CreateMessageDtoRequest(
                "Subject", "Body", null, null
        );
        final MessageDtoResponse response = new MessageDtoResponse(
                147258, MessageState.UNPUBLISHED.name()
        );

        when(mockMessageService.addMessage(anyString(), anyInt(), any(CreateMessageDtoRequest.class)))
                .thenReturn(response);

        mvc.perform(
                post("/api/forums/{forum_id}/messages", 123)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.state").value(response.getState()));

        verify(mockMessageService)
                .addMessage(anyString(), anyInt(), any(CreateMessageDtoRequest.class));
    }

    static Stream<Arguments> createMessageInvalidParams() {
        return Stream.of(
                Arguments.arguments(null, "Body", MessagePriority.NORMAL.name(), ValidatedRequestFieldName.MESSAGE_SUBJECT),
                Arguments.arguments("", "Body", MessagePriority.NORMAL.name(), ValidatedRequestFieldName.MESSAGE_SUBJECT),
                Arguments.arguments("Subject", null, MessagePriority.NORMAL.name(), ValidatedRequestFieldName.MESSAGE_BODY),
                Arguments.arguments("Subject", "", MessagePriority.NORMAL.name(), ValidatedRequestFieldName.MESSAGE_BODY),
                Arguments.arguments("Subject", "Body", "EXTRA-HIGH", ValidatedRequestFieldName.MESSAGE_PRIORITY)
        );
    }

    @ParameterizedTest
    @MethodSource("createMessageInvalidParams")
    void testCreateMessage_invalidRequestParams_shouldReturnExceptionDto(
            String subject, String body, String priority, ValidatedRequestFieldName errorFieldName
    ) throws Exception {
        final CreateMessageDtoRequest request = new CreateMessageDtoRequest(
                subject, body, priority, Collections.emptyList()
        );

        mvc.perform(
                post("/api/forums/{forum_id}/messages", 123)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.INVALID_REQUEST_DATA.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorFieldName.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());

        verify(mockMessageService, never())
                .addMessage(anyString(), anyInt(), any(CreateMessageDtoRequest.class));
    }

    static Stream<Arguments> createMessageServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR),
                Arguments.arguments(ErrorCode.WRONG_SESSION_TOKEN),
                Arguments.arguments(ErrorCode.USER_BANNED),
                Arguments.arguments(ErrorCode.FORUM_NOT_FOUND),
                Arguments.arguments(ErrorCode.FORUM_READ_ONLY)
        );
    }

    @ParameterizedTest
    @MethodSource("createMessageServiceExceptions")
    void testCreateMessage_errorOccurred_shouldReturnExceptionDto(ErrorCode errorCode) throws Exception {
        final CreateMessageDtoRequest request = new CreateMessageDtoRequest(
                "Subject", "Body", MessagePriority.NORMAL.name(), Collections.emptyList()
        );
        when(mockMessageService.addMessage(anyString(), anyInt(), any(CreateMessageDtoRequest.class)))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                post("/api/forums/{forum_id}/messages", 123)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockMessageService)
                .addMessage(anyString(), anyInt(), any(CreateMessageDtoRequest.class));
    }

    @Test
    void testGetMessageList() throws Exception {
        final List<MessageInfoDtoResponse> responses = new ArrayList<>();
        responses.add(
                new MessageInfoDtoResponse(
                        123,
                        "Creator#1",
                        "Subject#1",
                        Arrays.asList("Body#2", "Body#1"),
                        MessagePriority.NORMAL.name(),
                        Arrays.asList("Tag#1", "Tag#2"),
                        LocalDateTime.now()
                                .plus(1, ChronoUnit.WEEKS)
                                .truncatedTo(ChronoUnit.SECONDS),
                        4,
                        3,
                        Collections.singletonList(
                                new CommentInfoDtoResponse(
                                        149, "Creator#3",
                                        Collections.singletonList("C Body"),
                                        LocalDateTime.now()
                                                .plus(6, ChronoUnit.DAYS)
                                                .truncatedTo(ChronoUnit.SECONDS),
                                        5, 1,
                                        Collections.emptyList()
                                )
                        )
                )
        );
        responses.add(
                new MessageInfoDtoResponse(
                        123,
                        "Creator#2",
                        "Subject#2",
                        Arrays.asList("Body#3", "Body#2", "Body#1"),
                        MessagePriority.LOW.name(),
                        Arrays.asList("Tag#2", "Tag#3", "Tag#4"),
                        LocalDateTime.now()
                                .plus(2, ChronoUnit.WEEKS)
                                .truncatedTo(ChronoUnit.SECONDS),
                        3,
                        5,
                        Collections.emptyList()
                )
        );
        final ListMessageInfoDtoResponse expectedResponse = new ListMessageInfoDtoResponse(responses);

        when(mockMessageService
                .getForumMessageList(
                        anyString(), anyInt(),
                        eq(null), eq(null), eq(null),
                        eq(null), eq(null), anyInt(), anyInt()
                )
        )
                .thenReturn(expectedResponse);

        final MvcResult mvcResult = mvc.perform(
                get("/api/forums/{forum_id}/messages", 123)
                        .param("offset", "0")
                        .param("limit", "10")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        final ListMessageInfoDtoResponse actualResponse = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ListMessageInfoDtoResponse.class
        );
        assertEquals(expectedResponse, actualResponse);

        verify(mockMessageService)
                .getForumMessageList(
                        anyString(), anyInt(),
                        eq(null), eq(null), eq(null),
                        eq(null), eq(null), anyInt(), anyInt()
                );
    }

    @Test
    void testGetMessageList_tagsListsInParams() throws Exception {
        final List<MessageInfoDtoResponse> responses = new ArrayList<>();
        responses.add(
                new MessageInfoDtoResponse(
                        123,
                        "Creator#1",
                        "Subject#1",
                        Arrays.asList("Body#2", "Body#1"),
                        MessagePriority.NORMAL.name(),
                        Arrays.asList("Tag#1", "Tag#2"),
                        LocalDateTime.now()
                                .plus(1, ChronoUnit.WEEKS)
                                .truncatedTo(ChronoUnit.SECONDS),
                        4,
                        3,
                        Collections.singletonList(
                                new CommentInfoDtoResponse(
                                        149, "Creator#3",
                                        Collections.singletonList("C Body"),
                                        LocalDateTime.now()
                                                .plus(6, ChronoUnit.DAYS)
                                                .truncatedTo(ChronoUnit.SECONDS),
                                        5, 1,
                                        Collections.emptyList()
                                )
                        )
                )
        );
        responses.add(
                new MessageInfoDtoResponse(
                        123,
                        "Creator#2",
                        "Subject#2",
                        Arrays.asList("Body#3", "Body#2", "Body#1"),
                        MessagePriority.LOW.name(),
                        Arrays.asList("Tag#2", "Tag#3", "Tag#4"),
                        LocalDateTime.now()
                                .plus(2, ChronoUnit.WEEKS)
                                .truncatedTo(ChronoUnit.SECONDS),
                        3,
                        5,
                        Collections.emptyList()
                )
        );
        final ListMessageInfoDtoResponse expectedResponse = new ListMessageInfoDtoResponse(responses);
        final String tag2 = "Tag#2";
        final String tag3 = "Tag#3";

        when(mockMessageService
                .getForumMessageList(
                        anyString(), anyInt(),
                        eq(null), eq(null), eq(null),
                        eq(Arrays.asList(tag2, tag3)), eq(null),
                        eq(null), eq(null)
                )
        )
                .thenReturn(expectedResponse);

        final MvcResult mvcResult = mvc.perform(
                get("/api/forums/{forum_id}/messages", 123)
                        .param("tags", tag2)
                        .param("tags", tag3)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        final ListMessageInfoDtoResponse actualResponse = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ListMessageInfoDtoResponse.class
        );
        assertEquals(expectedResponse, actualResponse);

        verify(mockMessageService)
                .getForumMessageList(
                        anyString(), anyInt(),
                        eq(null), eq(null), eq(null),
                        eq(Arrays.asList(tag2, tag3)), eq(null),
                        eq(null), eq(null)
                );
    }

    @Test
    void testGetMessageList_notExistsAllQueryParams_shouldApplyDefaultSettingsInService() throws Exception {
        final List<MessageInfoDtoResponse> responses = new ArrayList<>();
        responses.add(
                new MessageInfoDtoResponse(
                        123,
                        "Creator#1",
                        "Subject#1",
                        Arrays.asList("Body#2", "Body#1"),
                        MessagePriority.NORMAL.name(),
                        Arrays.asList("Tag#1", "Tag#2"),
                        LocalDateTime.now()
                                .plus(1, ChronoUnit.WEEKS)
                                .truncatedTo(ChronoUnit.SECONDS),
                        4,
                        3,
                        Collections.singletonList(
                                new CommentInfoDtoResponse(
                                        149, "Creator#3",
                                        Collections.singletonList("C Body"),
                                        LocalDateTime.now()
                                                .plus(6, ChronoUnit.DAYS)
                                                .truncatedTo(ChronoUnit.SECONDS),
                                        5, 1,
                                        Collections.emptyList()
                                )
                        )
                )
        );
        responses.add(
                new MessageInfoDtoResponse(
                        123,
                        "Creator#2",
                        "Subject#2",
                        Arrays.asList("Body#3", "Body#2", "Body#1"),
                        MessagePriority.LOW.name(),
                        Arrays.asList("Tag#2", "Tag#3", "Tag#4"),
                        LocalDateTime.now()
                                .plus(2, ChronoUnit.WEEKS)
                                .truncatedTo(ChronoUnit.SECONDS),
                        3,
                        5,
                        Collections.emptyList()
                )
        );
        final ListMessageInfoDtoResponse expectedResponse = new ListMessageInfoDtoResponse(responses);

        when(mockMessageService
                .getForumMessageList(
                        anyString(), anyInt(),
                        eq(null), eq(null), eq(null),
                        eq(null), eq(null), eq(null), eq(null)
                )
        )
                .thenReturn(expectedResponse);

        final MvcResult mvcResult = mvc.perform(
                get("/api/forums/{forum_id}/messages", 123)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        final ListMessageInfoDtoResponse actualResponse = mapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                ListMessageInfoDtoResponse.class
        );
        assertEquals(expectedResponse, actualResponse);

        verify(mockMessageService)
                .getForumMessageList(
                        anyString(), anyInt(),
                        eq(null), eq(null), eq(null),
                        eq(null), eq(null), eq(null), eq(null)
                );
    }

    static Stream<Arguments> getMessagesServiceException() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR),
                Arguments.arguments(ErrorCode.WRONG_SESSION_TOKEN),
                Arguments.arguments(ErrorCode.FORUM_NOT_FOUND)
        );
    }

    @ParameterizedTest
    @MethodSource("getMessagesServiceException")
    void testGetMessageList_exceptionsInService_shouldReturnExceptionDto(ErrorCode errorCode) throws Exception {
        when(mockMessageService
                .getForumMessageList(
                        anyString(), anyInt(),
                        eq(null), eq(null), eq(null),
                        eq(null), eq(null), anyInt(), anyInt()
                )
        )
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                get("/api/forums/{forum_id}/messages", 123)
                        .param("offset", "0")
                        .param("limit", "10")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockMessageService)
                .getForumMessageList(
                        anyString(), anyInt(),
                        eq(null), eq(null), eq(null),
                        eq(null), eq(null), anyInt(), anyInt()
                );
    }
}