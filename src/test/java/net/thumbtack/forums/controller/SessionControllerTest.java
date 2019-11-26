package net.thumbtack.forums.controller;

import net.thumbtack.forums.service.UserService;
import net.thumbtack.forums.dto.EmptyDtoResponse;
import net.thumbtack.forums.dto.UserDtoResponse;
import net.thumbtack.forums.dto.LoginUserDtoRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.UUID;
import javax.servlet.http.Cookie;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SessionController.class)
class SessionControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

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

        when(userService.login(any(LoginUserDtoRequest.class)))
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

        verify(userService).login(request);
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

        when(userService.login(any(LoginUserDtoRequest.class)))
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

        verify(userService).login(request);
    }

    @Test
    void testLogout_shouldDeleteSession() throws Exception {
        when(userService.logout(anyString()))
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

        verify(userService).logout(anyString());
    }
}