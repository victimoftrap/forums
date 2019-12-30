package net.thumbtack.forums.controller;

import net.thumbtack.forums.service.UserService;
import net.thumbtack.forums.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.UpdatePasswordDtoRequest;
import net.thumbtack.forums.dto.responses.user.*;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.dto.responses.exception.ExceptionDtoResponse;
import net.thumbtack.forums.dto.responses.exception.ExceptionListDtoResponse;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.RequestFieldName;
import net.thumbtack.forums.exception.ServerException;
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
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.http.Cookie;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(controllers = UserController.class)
@Import(ServerConfigurationProperties.class)
class UserControllerTest {
    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @MockBean
    private UserService mockUserService;

    private final String COOKIE_NAME = "JAVASESSIONID";
    private final String COOKIE_VALUE = UUID.randomUUID().toString();

    @Test
    void testRegisterUser() throws Exception {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "username", "ahoi@savemail.com", "strong_pass_454"
        );
        final UserDtoResponse response = new UserDtoResponse(
                12345, request.getName(), request.getEmail(), UUID.randomUUID().toString()
        );
        when(mockUserService.registerUser(any(RegisterUserDtoRequest.class)))
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

        verify(mockUserService).registerUser(request);
    }

    @Test
    void testRegisterUser_usernameTooLarge_shouldReturnExceptionDto() throws Exception {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "0123456789_0123456789_0123456789_0123456789_0123456789",
                "ahoi@savemail.com", "strong_pass_454"
        );

        mvc.perform(
                post("/api/users")
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
    void testRegisterUser_usernameAreEmpty_shouldReturnExceptionDto() throws Exception {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "", "ahoi@savemail.com", "strong_pass_454"
        );

        final MvcResult result = mvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        final ExceptionListDtoResponse response = mapper.readValue(
                result.getResponse().getContentAsString(),
                ExceptionListDtoResponse.class
        );
        assertEquals(1, response.getErrors().size());
        assertEquals(ErrorCode.INVALID_REQUEST_DATA, response.getErrors().get(0).getErrorCode());
        assertEquals(RequestFieldName.USERNAME.getName(), response.getErrors().get(0).getField());
        assertFalse(response.getErrors().get(0).getMessage().isEmpty());
        verifyZeroInteractions(mockUserService);
    }

    @Test
    void testRegisterUser_usernameAreNull_shouldReturnExceptionDto() throws Exception {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                null, "ahoi@savemail.com", "strong_pass_454"
        );

        final MvcResult result = mvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        final ExceptionListDtoResponse response = mapper.readValue(
                result.getResponse().getContentAsString(),
                ExceptionListDtoResponse.class
        );

        assertEquals(1, response.getErrors().size());
        assertEquals(ErrorCode.INVALID_REQUEST_DATA, response.getErrors().get(0).getErrorCode());
        assertEquals(RequestFieldName.USERNAME.getName(), response.getErrors().get(0).getField());
        assertFalse(response.getErrors().get(0).getMessage().isEmpty());
        verifyZeroInteractions(mockUserService);
    }

    @Test
    void testRegisterUser_passwordTooShort_shouldReturnExceptionDto() throws Exception {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "username", "ahoi@savemail.com", "weak"
        );

        mvc.perform(
                post("/api/users")
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
    void testRegisterUser_passwordAreEmpty_shouldReturnExceptionDto() throws Exception {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "username", "ahoi@savemail.com", ""
        );

        mvc.perform(
                post("/api/users")
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
    void testRegisterUser_passwordAreNull_shouldReturnExceptionDto() throws Exception {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "username", "ahoi@savemail.com", null
        );

        mvc.perform(
                post("/api/users")
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
    void testRegisterUser_allFieldsAreInvalid_shouldReturnExceptionDto() throws Exception {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "", null, "weak"
        );

        final MvcResult result = mvc.perform(
                post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request))
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        final ExceptionListDtoResponse response = mapper.readValue(
                result.getResponse().getContentAsString(),
                ExceptionListDtoResponse.class
        );

        final List<ExceptionDtoResponse> errors = response.getErrors();
        assertEquals(3, errors.size());
        assertEquals(ErrorCode.INVALID_REQUEST_DATA, errors.get(0).getErrorCode());
        assertEquals(RequestFieldName.EMAIL.getName(), errors.get(0).getField());
        assertFalse(errors.get(0).getMessage().isEmpty());
        assertEquals(ErrorCode.INVALID_REQUEST_DATA, errors.get(1).getErrorCode());
        assertEquals(RequestFieldName.USERNAME.getName(), errors.get(1).getField());
        assertFalse(errors.get(1).getMessage().isEmpty());
        assertEquals(ErrorCode.INVALID_REQUEST_DATA, errors.get(2).getErrorCode());
        assertEquals(RequestFieldName.PASSWORD.getName(), errors.get(2).getField());
        assertFalse(errors.get(2).getMessage().isEmpty());
        verifyZeroInteractions(mockUserService);
    }

    @Test
    void testDeleteUser() throws Exception {
        when(mockUserService.deleteUser(anyString()))
                .thenReturn(new EmptyDtoResponse());

        mvc.perform(
                delete("/api/users")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{}"));

        verify(mockUserService).deleteUser(anyString());
    }

    @Test
    void testDeleteUser_userNotFound_shouldReturnExceptionDto() throws Exception {
        when(mockUserService.deleteUser(anyString()))
                .thenThrow(new ServerException(ErrorCode.WRONG_SESSION_TOKEN));

        mvc.perform(
                delete("/api/users")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.WRONG_SESSION_TOKEN.name()))
                .andExpect(jsonPath("$.errors[0].field").doesNotExist())
                .andExpect(jsonPath("$.errors[0].message").exists());

        verify(mockUserService).deleteUser(anyString());
    }

    @Test
    void testDeleteUser_forumNotFound_shouldReturnExceptionDto() throws Exception {
        when(mockUserService.deleteUser(anyString()))
                .thenThrow(new ServerException(ErrorCode.FORUM_NOT_FOUND));

        mvc.perform(
                delete("/api/users")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.FORUM_NOT_FOUND.name()))
                .andExpect(jsonPath("$.errors[0].field").doesNotExist())
                .andExpect(jsonPath("$.errors[0].message").exists());

        verify(mockUserService).deleteUser(anyString());
    }

    @Test
    void testUpdateUserPassword() throws Exception {
        final UpdatePasswordDtoRequest request = new UpdatePasswordDtoRequest(
                "GregHouse", "password123", "game_boy_advance"
        );
        final UserDtoResponse response = new UserDtoResponse(
                5, request.getName(), "house@med.com", COOKIE_VALUE
        );
        when(mockUserService.updatePassword(anyString(), any(UpdatePasswordDtoRequest.class)))
                .thenReturn(response);

        mvc.perform(
                put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(request))
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
        )
                .andExpect(status().isOk())
                .andExpect(cookie().value(COOKIE_NAME, COOKIE_VALUE))
                .andExpect(cookie().httpOnly(COOKIE_NAME, true))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(response.getId()))
                .andExpect(jsonPath("$.name").value(response.getName()))
                .andExpect(jsonPath("$.email").value(response.getEmail()))
                .andExpect(jsonPath("$.sessionToken").doesNotExist());

        verify(mockUserService)
                .updatePassword(anyString(), any(UpdatePasswordDtoRequest.class));
    }

    @Test
    void testUpdatePassword_usernameAreEmpty_shouldReturnExceptionDto() throws Exception {
        final UpdatePasswordDtoRequest request = new UpdatePasswordDtoRequest(
                "0123456789_0123456789_0123456789_0123456789_0123456789",
                "password123", "strong/password/456"
        );
        mvc.perform(
                put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(request))
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
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
    void testUpdatePassword_usernameAreNull_shouldReturnExceptionDto() throws Exception {
        final UpdatePasswordDtoRequest request = new UpdatePasswordDtoRequest(
                "", "password123", "strong/password/456"
        );
        mvc.perform(
                put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(request))
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
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
    void testUpdatePassword_usernameAreTooLarge_shouldReturnExceptionDto() throws Exception {
        final UpdatePasswordDtoRequest request = new UpdatePasswordDtoRequest(
                "", "password123", "strong/password/456"
        );
        mvc.perform(
                put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(request))
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
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
    void testUpdatePassword_newPasswordAreEmpty_shouldReturnExceptionDto() throws Exception {
        final UpdatePasswordDtoRequest request = new UpdatePasswordDtoRequest(
                "GregHouse", "password123", ""
        );
        mvc.perform(
                put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(request))
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
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
    void testUpdatePassword_newPasswordAreNull_shouldReturnExceptionDto() throws Exception {
        final UpdatePasswordDtoRequest request = new UpdatePasswordDtoRequest(
                "GregHouse", "password123", null
        );
        mvc.perform(
                put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(request))
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
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
    void testUpdatePassword_newPasswordAreTooSmall_shouldReturnExceptionDto() throws Exception {
        final UpdatePasswordDtoRequest request = new UpdatePasswordDtoRequest(
                "GregHouse", "password123", "weak"
        );

        mvc.perform(
                put("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsBytes(request))
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
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
    void testMadeSuperuser() throws Exception {
        when(mockUserService.madeSuperuser(anyString(), anyInt()))
                .thenReturn(new EmptyDtoResponse());

        mvc.perform(
                put("/api/users/{user}/super", 123)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{}"));
        verify(mockUserService).madeSuperuser(anyString(), anyInt());
    }

    @Test
    void testMadeSuperuser_requestFromRegularUser_shouldReturnExceptionDto() throws Exception {
        when(mockUserService.madeSuperuser(anyString(), anyInt()))
                .thenThrow(new ServerException(ErrorCode.FORBIDDEN_OPERATION));

        mvc.perform(
                put("/api/users/{user}/super", 123)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.FORBIDDEN_OPERATION.name()))
                .andExpect(jsonPath("$.errors[0].field").doesNotExist())
                .andExpect(jsonPath("$.errors[0].message").exists());

        verify(mockUserService).madeSuperuser(anyString(), anyInt());
    }

    @Test
    void testGetUsers_requestFromRegularUser_shouldReturnNotAllFields() throws Exception {
        final List<UserDetailsDtoResponse> users = new ArrayList<>();
        users.add(new UserDetailsDtoResponse(
                        1, "user1", null,
                        LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                        true, false, null,
                        UserStatus.FULL, null, 0
                )
        );
        users.add(new UserDetailsDtoResponse(
                        2, "user2", null,
                        LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                        false, false, null,
                        UserStatus.FULL, null, 0
                )
        );
        final UserDetailsListDtoResponse expectedUsersResponse = new UserDetailsListDtoResponse(users);
        when(mockUserService.getUsers(anyString()))
                .thenReturn(expectedUsersResponse);

        final MvcResult result = mvc.perform(
                get("/api/users")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.users", hasSize(2)))
                .andReturn();

        final UserDetailsListDtoResponse actualUsersResponse = mapper.readValue(
                result.getResponse().getContentAsString(),
                UserDetailsListDtoResponse.class
        );
        assertEquals(expectedUsersResponse, actualUsersResponse);
    }

    @Test
    void testGetUsers_requestFromSuperuser_shouldReturnAllFields() throws Exception {
        final List<UserDetailsDtoResponse> users = new ArrayList<>();
        users.add(new UserDetailsDtoResponse(
                        1, "user1", "user1@email.com",
                        LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                        true, false, false,
                        UserStatus.FULL, null, 0
                )
        );
        users.add(new UserDetailsDtoResponse(
                        2, "user2", "user2@email.com",
                        LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                        false, false, true,
                        UserStatus.FULL, null, 0
                )
        );
        users.add(new UserDetailsDtoResponse(
                        3, "user3", "user3@email.com",
                        LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                        false, true, false,
                        UserStatus.FULL, null, 0
                )
        );
        final UserDetailsListDtoResponse expectedUsersResponse = new UserDetailsListDtoResponse(users);
        when(mockUserService.getUsers(anyString()))
                .thenReturn(expectedUsersResponse);

        final MvcResult result = mvc.perform(
                get("/api/users")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.users", hasSize(3)))
                .andReturn();

        final UserDetailsListDtoResponse actualUsersResponse = mapper.readValue(
                result.getResponse().getContentAsString(),
                UserDetailsListDtoResponse.class
        );
        assertEquals(expectedUsersResponse, actualUsersResponse);
    }

    @Test
    void testGetUsers_userNotFound_shouldReturnExceptionDto() throws Exception {
        when(mockUserService.getUsers(anyString()))
                .thenThrow(new ServerException(ErrorCode.WRONG_SESSION_TOKEN));

        mvc.perform(
                get("/api/users")
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.WRONG_SESSION_TOKEN.name()))
                .andExpect(jsonPath("$.errors[0].field").doesNotExist())
                .andExpect(jsonPath("$.errors[0].message").exists());

        verify(mockUserService).getUsers(anyString());
    }

    @Test
    void testBanUser() throws Exception {
        when(mockUserService.banUser(anyString(), anyInt()))
                .thenReturn(new EmptyDtoResponse());

        mvc.perform(
                post("/api/users/{user}/restrict", 123)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().string("{}"));

        verify(mockUserService).banUser(anyString(), anyInt());
    }

    @Test
    void testBanUser_userNotFound_shouldReturnExceptionDto() throws Exception {
        when(mockUserService.banUser(anyString(), anyInt()))
                .thenThrow(new ServerException(ErrorCode.WRONG_SESSION_TOKEN));

        mvc.perform(
                post("/api/users/{user}/restrict", 123)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.WRONG_SESSION_TOKEN.name()))
                .andExpect(jsonPath("$.errors[0].field").doesNotExist())
                .andExpect(jsonPath("$.errors[0].message").exists());

        verify(mockUserService).banUser(anyString(), anyInt());
    }

    @Test
    void testBanUser_requestNotFromSuperuser_shouldReturnExceptionDto() throws Exception {
        when(mockUserService.banUser(anyString(), anyInt()))
                .thenThrow(new ServerException(ErrorCode.FORBIDDEN_OPERATION));

        mvc.perform(
                post("/api/users/{user}/restrict", 123)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.FORBIDDEN_OPERATION.name()))
                .andExpect(jsonPath("$.errors[0].field").doesNotExist())
                .andExpect(jsonPath("$.errors[0].message").exists());

        verify(mockUserService).banUser(anyString(), anyInt());
    }

    @Test
    void testBanUser_tryingToBanSuperuser_shouldReturnExceptionDto() throws Exception {
        when(mockUserService.banUser(anyString(), anyInt()))
                .thenThrow(new ServerException(ErrorCode.FORBIDDEN_OPERATION));

        mvc.perform(
                post("/api/users/{user}/restrict", 123)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().isBadRequest())
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(ErrorCode.FORBIDDEN_OPERATION.name()))
                .andExpect(jsonPath("$.errors[0].field").doesNotExist())
                .andExpect(jsonPath("$.errors[0].message").exists());

        verify(mockUserService).banUser(anyString(), anyInt());
    }
}