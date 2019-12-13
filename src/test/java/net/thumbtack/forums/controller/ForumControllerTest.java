package net.thumbtack.forums.controller;

import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.dto.responses.forum.ForumDtoResponse;
import net.thumbtack.forums.dto.requests.forum.CreateForumDtoRequest;
import net.thumbtack.forums.service.ForumService;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.RequestFieldName;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import javax.servlet.http.Cookie;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private final String COOKIE_NAME = "JAVASESSIONID";
    private final String COOKIE_VALUE = UUID.randomUUID().toString();

    @Test
    void testCreateForum() throws Exception {
        final CreateForumDtoRequest request = new CreateForumDtoRequest(
                "testForum", ForumType.UNMODERATED
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
                .andExpect(jsonPath("$.type").value(expectedResponse.getType().name()));

        verify(mockForumService).createForum(anyString(), any(CreateForumDtoRequest.class));
    }

    @Test
    void testCreateForum_forumNameAreNull_shouldReturnExceptionDto() throws Exception {
        final CreateForumDtoRequest request = new CreateForumDtoRequest(
                null, ForumType.UNMODERATED
        );
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
                .andExpect(jsonPath("$.errors[0].field").value(RequestFieldName.FORUM_NAME.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());
        verifyZeroInteractions(mockForumService);
    }

    @Test
    void testCreateForum_forumNameAreEmpty_shouldReturnExceptionDto() throws Exception {
        final CreateForumDtoRequest request = new CreateForumDtoRequest(
                "", ForumType.UNMODERATED
        );
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
                .andExpect(jsonPath("$.errors[0].field").value(RequestFieldName.FORUM_NAME.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());
        verifyZeroInteractions(mockForumService);
    }

    @Test
    void testCreateForum_forumNameAreTooLarge_shouldReturnExceptionDto() throws Exception {
        final CreateForumDtoRequest request = new CreateForumDtoRequest(
                "0123456789_0123456789_0123456789_0123456789_0123456789", ForumType.UNMODERATED
        );
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
                .andExpect(jsonPath("$.errors[0].field").value(RequestFieldName.FORUM_NAME.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());
        verifyZeroInteractions(mockForumService);
    }

    @Test
    void testCreateForum_forumTypeAreNull_shouldReturnExceptionDto() throws Exception {
        final CreateForumDtoRequest request = new CreateForumDtoRequest(
                "test", null
        );
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
                .andExpect(jsonPath("$.errors[0].field").value(RequestFieldName.FORUM_TYPE.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());
        verifyZeroInteractions(mockForumService);
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

    @Test
    void testDeleteForum_notOwnerTryingToDeleteForum_shouldReturnExceptionDto() throws Exception {
        when(mockForumService.deleteForum(anyString(), anyInt()))
                .thenThrow(new ServerException(ErrorCode.FORBIDDEN_OPERATION));
        mvc.perform(
                delete("/api/forums/{forum_id}", 456)
                .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                .contentType(MediaType.APPLICATION_JSON_VALUE)
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.FORBIDDEN_OPERATION.name()))
                .andExpect(jsonPath("$.errors[0].field").doesNotExist())
                .andExpect(jsonPath("$.errors[0].message").exists());
    }
}