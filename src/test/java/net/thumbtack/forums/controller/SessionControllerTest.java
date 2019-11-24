package net.thumbtack.forums.controller;

import net.thumbtack.forums.service.UserService;
import net.thumbtack.forums.dto.UserDtoResponse;
import net.thumbtack.forums.dto.LoginUserDtoRequest;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MvcResult;

import javax.servlet.http.Cookie;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = SessionController.class)
class SessionControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService userService;

    @Test
    void testLogin() throws Exception {
        final LoginUserDtoRequest request = new LoginUserDtoRequest("till", "hallomann");
        final UserDtoResponse response = new UserDtoResponse(
                12,
                request.getName(),
                "till@ramm.com",
                UUID.randomUUID().toString()
        );

        when(userService.login(any(LoginUserDtoRequest.class)))
                .thenReturn(response);

        final MvcResult result = mvc.perform(
                post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        ).andReturn();

        final UserDtoResponse actualResponse = mapper.readValue(
                result.getResponse().getContentAsString(), UserDtoResponse.class
        );
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
        assertEquals(response.getSessionToken(), result.getResponse().getCookie("JAVASESSIONID").getValue());
        assertEquals(response.getId(), actualResponse.getId());
        assertEquals(response.getName(), actualResponse.getName());
        assertEquals(response.getEmail(), actualResponse.getEmail());
        assertNull(actualResponse.getSessionToken());
    }

    @Test
    void testLogout() throws Exception {
        final MvcResult result = mvc.perform(
                delete("/api/sessions")
                .cookie(new Cookie("JAVASESSIONID", UUID.randomUUID().toString()))
        ).andReturn();

        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }
}