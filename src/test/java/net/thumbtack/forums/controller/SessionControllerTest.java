package net.thumbtack.forums.controller;

import net.thumbtack.forums.service.UserService;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.dto.responses.user.UserDtoResponse;
import net.thumbtack.forums.dto.requests.user.LoginUserDtoRequest;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.RequestFieldName;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;
import javax.servlet.http.Cookie;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SessionController.class)
@Import(ServerConfigurationProperties.class)
class SessionControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService mockUserService;

    private final String COOKIE_NAME = "JAVASESSIONID";

    @Test
    void testLogin_shouldLoginUser() throws Exception {
        final LoginUserDtoRequest request = new LoginUserDtoRequest(
                "dumbledore", "strong_password"
        );
        final UserDtoResponse response = new UserDtoResponse(
                12,
                request.getName(),
                "albus@hogwarts.uk",
                UUID.randomUUID().toString()
        );

        when(mockUserService.login(any(LoginUserDtoRequest.class)))
                .thenReturn(response);

        mvc.perform(
                post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().value(COOKIE_NAME, response.getSessionToken()))
                .andExpect(cookie().httpOnly(COOKIE_NAME, true))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.name").value(response.getName()))
                .andExpect(jsonPath("$.email").value(response.getEmail()))
                .andExpect(jsonPath("$.sessionToken").doesNotExist());

        verify(mockUserService).login(request);
    }

    @Test
    void testLogin_requestWithCaseInsensitiveName_shouldLoginUser() throws Exception {
        final LoginUserDtoRequest request = new LoginUserDtoRequest(
                "duMBledOrE", "strong_password"
        );
        final UserDtoResponse response = new UserDtoResponse(
                1998,
                "dumbledore",
                "albus@hogwarts.uk",
                UUID.randomUUID().toString()
        );

        when(mockUserService.login(any(LoginUserDtoRequest.class)))
                .thenReturn(response);

        mvc.perform(
                post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().value(COOKIE_NAME, response.getSessionToken()))
                .andExpect(cookie().httpOnly(COOKIE_NAME, true))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.name").value(response.getName()))
                .andExpect(jsonPath("$.email").value(response.getEmail()))
                .andExpect(jsonPath("$.sessionToken").doesNotExist());

        verify(mockUserService).login(request);
    }

    @Test
    void testLogin_userNotFoundByName_shouldReturnExceptionDto() throws Exception {
        final LoginUserDtoRequest request = new LoginUserDtoRequest(
                "dumbledore", "strong_password"
        );
        when(mockUserService.login(any(LoginUserDtoRequest.class)))
                .thenThrow(new ServerException(ErrorCode.INVALID_REQUEST_DATA, RequestFieldName.USERNAME));

        mvc.perform(
                post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.INVALID_REQUEST_DATA.name()))
                .andExpect(jsonPath("$.errors[0].field").value(RequestFieldName.USERNAME.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());

        verify(mockUserService).login(request);
    }

    @Test
    void testLogin_usernameAreEmpty_shouldReturnExceptionDto() throws Exception {
        final LoginUserDtoRequest request = new LoginUserDtoRequest("", "strong_password");
        mvc.perform(
                post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.INVALID_REQUEST_DATA.name()))
                .andExpect(jsonPath("$.errors[0].field").value(RequestFieldName.USERNAME.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());

        verifyZeroInteractions(mockUserService);
    }

    @Test
    void testLogin_usernameAreNull_shouldReturnExceptionDto() throws Exception {
        final LoginUserDtoRequest request = new LoginUserDtoRequest(null, "strong_password");
        mvc.perform(
                post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.INVALID_REQUEST_DATA.name()))
                .andExpect(jsonPath("$.errors[0].field").value(RequestFieldName.USERNAME.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());
        verifyZeroInteractions(mockUserService);
    }

    @Test
    void testLogin_usernameAreTooLarge_shouldReturnExceptionDto() throws Exception {
        final LoginUserDtoRequest request = new LoginUserDtoRequest(
                "0123456789_0123456789_0123456789_0123456789_0123456789",
                "strong_password"
        );
        mvc.perform(
                post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.INVALID_REQUEST_DATA.name()))
                .andExpect(jsonPath("$.errors[0].field").value(RequestFieldName.USERNAME.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());
        verifyZeroInteractions(mockUserService);
    }

    @Test
    void testLogin_passwordNotMatches_shouldReturnExceptionDto() throws Exception {
        final LoginUserDtoRequest request = new LoginUserDtoRequest(
                "dumbledore", "not_matches"
        );
        when(mockUserService.login(any(LoginUserDtoRequest.class)))
                .thenThrow(new ServerException(ErrorCode.INVALID_REQUEST_DATA, RequestFieldName.PASSWORD));

        mvc.perform(
                post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.INVALID_REQUEST_DATA.name()))
                .andExpect(jsonPath("$.errors[0].field").value(RequestFieldName.PASSWORD.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());

        verify(mockUserService).login(request);
    }

    @Test
    void testLogin_passwordAreNull_shouldReturnExceptionDto() throws Exception {
        final LoginUserDtoRequest request = new LoginUserDtoRequest("dumbledore", null);
        mvc.perform(
                post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.INVALID_REQUEST_DATA.name()))
                .andExpect(jsonPath("$.errors[0].field").value(RequestFieldName.PASSWORD.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());

        verifyZeroInteractions(mockUserService);
    }

    @Test
    void testLogin_passwordAreEmpty_shouldReturnExceptionDto() throws Exception {
        final LoginUserDtoRequest request = new LoginUserDtoRequest("dumbledore", "");
        mvc.perform(
                post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.INVALID_REQUEST_DATA.name()))
                .andExpect(jsonPath("$.errors[0].field").value(RequestFieldName.PASSWORD.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());

        verifyZeroInteractions(mockUserService);
    }

    @Test
    void testLogout_shouldDeleteSession() throws Exception {
        when(mockUserService.logout(anyString()))
                .thenReturn(new EmptyDtoResponse());

        mvc.perform(
                delete("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, UUID.randomUUID().toString()))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{}"));

        verify(mockUserService).logout(anyString());
    }

    @Test
    void testLogout_wrongSessionToken_returnExceptionDto() throws Exception {
        when(mockUserService.logout(anyString()))
                .thenThrow(new ServerException(ErrorCode.WRONG_SESSION_TOKEN));
        mvc.perform(
                delete("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .cookie(new Cookie(COOKIE_NAME, ""))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.WRONG_SESSION_TOKEN.name()))
                .andExpect(jsonPath("$.errors[0].field").doesNotExist())
                .andExpect(jsonPath("$.errors[0].message").exists());

        verify(mockUserService).logout(anyString());
    }
}