package net.thumbtack.forums.controller;

import net.thumbtack.forums.service.UserService;
import net.thumbtack.forums.dto.UserDtoResponse;
import net.thumbtack.forums.dto.EmptyDtoResponse;
import net.thumbtack.forums.dto.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.UpdatePasswordDtoRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import javax.servlet.http.Cookie;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    private final String COOKIE_NAME = "JAVASESSIONID";

    @Test
    void testRegisterUser() throws Exception {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "username", "ahoi@savemail.com", "strong_pass_454"
        );
        final UserDtoResponse response = new UserDtoResponse(
                12345, request.getName(), request.getEmail(), UUID.randomUUID().toString()
        );
        when(userService.registerUser(any(RegisterUserDtoRequest.class)))
                .thenReturn(response);

        mvc.perform(
                post("/api/users")
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

        verify(userService).registerUser(request);
    }

    @Test
    void testDeleteUser() throws Exception {
        final String token = "token";
        when(userService.deleteUser(anyString()))
                .thenReturn(new EmptyDtoResponse());

        mvc.perform(
                delete("/api/users")
                        .cookie(new Cookie(COOKIE_NAME, token))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{}"));

        verify(userService).deleteUser(token);
    }

    @Test
    void testUpdateUserPassword() throws Exception {
        final String token = "token";
        final UpdatePasswordDtoRequest request = new UpdatePasswordDtoRequest(
                "greg_house", "password123", "game_boy_advance"
        );
        final UserDtoResponse response = new UserDtoResponse(
                5, request.getName(), "house@med.com", token
        );
        when(userService.updatePassword(anyString(), any(UpdatePasswordDtoRequest.class)))
                .thenReturn(response);

        mvc.perform(
                put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(request))
                        .cookie(new Cookie(COOKIE_NAME, token))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().value(COOKIE_NAME, token))
                .andExpect(cookie().httpOnly(COOKIE_NAME, true))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.name").value(response.getName()))
                .andExpect(jsonPath("$.email").value(response.getEmail()))
                .andExpect(jsonPath("$.sessionToken").doesNotExist());

        verify(userService).updatePassword(anyString(), any(UpdatePasswordDtoRequest.class));
    }

    @Test
    void testMadeSuperuser() throws Exception {
        final String token = "token";
        when(userService.madeSuperuser(anyString(), anyInt()))
                .thenReturn(new EmptyDtoResponse());

        mvc.perform(
                put("/api/users/{user}/super", 123)
                        .cookie(new Cookie(COOKIE_NAME, token))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{}"));

        verify(userService).madeSuperuser(anyString(), anyInt());
    }

    @Test
    void testBanUser() throws Exception {
        final String token = "token";
        when(userService.banUser(anyString(), anyInt()))
                .thenReturn(new EmptyDtoResponse());

        mvc.perform(
                post("/api/users/{user}/restrict", 123)
                        .cookie(new Cookie(COOKIE_NAME, token))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{}"));

        verify(userService).banUser(anyString(), anyInt());
    }
}