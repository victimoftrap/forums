package net.thumbtack.forums.controller;

import net.thumbtack.forums.model.enums.UserStatus;
import net.thumbtack.forums.service.UserService;
import net.thumbtack.forums.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.UpdatePasswordDtoRequest;
import net.thumbtack.forums.dto.responses.user.*;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.dto.responses.exception.ExceptionDtoResponse;
import net.thumbtack.forums.dto.responses.exception.ExceptionListDtoResponse;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Stream;
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

    static Stream<Arguments> registerUserInvalidParams() {
        return Stream.of(
                Arguments.arguments(
                        "0123456789_0123456789_0123456789_0123456789_0123456789",
                        "ahoi@savemail.com", "strong_pass_454", ValidatedRequestFieldName.USERNAME
                ),
                Arguments.arguments("", "ahoi@savemail.com", "strong_pass_454", ValidatedRequestFieldName.USERNAME),
                Arguments.arguments(null, "ahoi@savemail.com", "strong_pass_454", ValidatedRequestFieldName.USERNAME),
                Arguments.arguments("username", "ahoi@savemail.com", "weak", ValidatedRequestFieldName.PASSWORD),
                Arguments.arguments("username", "ahoi@savemail.com", "", ValidatedRequestFieldName.PASSWORD),
                Arguments.arguments("username", "ahoi@savemail.com", null, ValidatedRequestFieldName.PASSWORD)
        );
    }

    @ParameterizedTest
    @MethodSource("registerUserInvalidParams")
    void testRegisterUser_invalidParams_shouldReturnExceptionDto(
            String username, String email, String password, ValidatedRequestFieldName errorFieldName
    ) throws Exception {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(username, email, password);

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
                .andExpect(jsonPath("$.errors[0].field").value(errorFieldName.getName()))
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
        assertEquals(ErrorCode.INVALID_REQUEST_DATA.name(), errors.get(0).getErrorCode());
        assertEquals(ValidatedRequestFieldName.EMAIL.getName(), errors.get(0).getField());
        assertFalse(errors.get(0).getMessage().isEmpty());
        assertEquals(ErrorCode.INVALID_REQUEST_DATA.name(), errors.get(1).getErrorCode());
        assertEquals(ValidatedRequestFieldName.USERNAME.getName(), errors.get(1).getField());
        assertFalse(errors.get(1).getMessage().isEmpty());
        assertEquals(ErrorCode.INVALID_REQUEST_DATA.name(), errors.get(2).getErrorCode());
        assertEquals(ValidatedRequestFieldName.PASSWORD.getName(), errors.get(2).getField());
        assertFalse(errors.get(2).getMessage().isEmpty());
        verifyZeroInteractions(mockUserService);
    }

    static Stream<Arguments> registerServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR),
                Arguments.arguments(ErrorCode.USER_NAME_ALREADY_USED)
        );
    }

    @ParameterizedTest
    @MethodSource("registerServiceExceptions")
    void testRegisterUser_exceptionInService_shouldReturnExceptionDto(ErrorCode errorCode) throws Exception {
        final RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "username", "ahoi@savemail.com", "strong_pass_454"
        );
        when(mockUserService.registerUser(any(RegisterUserDtoRequest.class)))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                post("/api/users")
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

        verify(mockUserService)
                .registerUser(any(RegisterUserDtoRequest.class));
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

    static Stream<Arguments> deleteUserServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR),
                Arguments.arguments(ErrorCode.WRONG_SESSION_TOKEN)
        );
    }

    @ParameterizedTest
    @MethodSource("deleteUserServiceExceptions")
    void testDeleteUser_exceptionInService_shouldReturnExceptionDto(ErrorCode errorCode) throws Exception {
        when(mockUserService.deleteUser(anyString()))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                delete("/api/users")
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

    static Stream<Arguments> updateUserPasswordInvalidParams() {
        return Stream.of(
                Arguments.arguments(
                        "0123456789_0123456789_0123456789_0123456789_0123456789",
                        "password123", "strong/password/456", ValidatedRequestFieldName.USERNAME
                ),
                Arguments.arguments("", "password123", "strong/password/456", ValidatedRequestFieldName.USERNAME),
                Arguments.arguments(null, "password123", "strong/password/456", ValidatedRequestFieldName.USERNAME),
                Arguments.arguments("username", "password123", "weak", ValidatedRequestFieldName.PASSWORD),
                Arguments.arguments("username", "password123", "", ValidatedRequestFieldName.PASSWORD),
                Arguments.arguments("username", "password123", null, ValidatedRequestFieldName.PASSWORD)
        );
    }

    @ParameterizedTest
    @MethodSource("updateUserPasswordInvalidParams")
    void testUpdatePassword_invalidParams_shouldReturnExceptionDto(
            String username, String oldPassword, String newPassword, ValidatedRequestFieldName errorFieldName
    ) throws Exception {
        final UpdatePasswordDtoRequest request = new UpdatePasswordDtoRequest(
                username, oldPassword, newPassword
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
                .andExpect(jsonPath("$.errors[0].field").value(errorFieldName.getName()))
                .andExpect(jsonPath("$.errors[0].message").exists());

        verifyZeroInteractions(mockUserService);
    }

    static Stream<Arguments> updatePasswordServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR),
                Arguments.arguments(ErrorCode.WRONG_SESSION_TOKEN),
                Arguments.arguments(ErrorCode.INVALID_PASSWORD)
        );
    }

    @ParameterizedTest
    @MethodSource("updatePasswordServiceExceptions")
    void testUpdatePassword_exceptionInService_shouldReturnExceptionDto(ErrorCode errorCode) throws Exception {
        final UpdatePasswordDtoRequest request = new UpdatePasswordDtoRequest(
                "GregHouse", "password123", "game_boy_advance"
        );
        when(mockUserService.updatePassword(anyString(), any(UpdatePasswordDtoRequest.class)))
                .thenThrow(new ServerException(errorCode));

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
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockUserService)
                .updatePassword(anyString(), any(UpdatePasswordDtoRequest.class));
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

    static Stream<Arguments> madeSuperuserServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.WRONG_SESSION_TOKEN, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.FORBIDDEN_OPERATION, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND)
        );
    }

    @ParameterizedTest
    @MethodSource("madeSuperuserServiceExceptions")
    void testMadeSuperuser_exceptionInService_shouldReturnExceptionDto(
            ErrorCode errorCode, HttpStatus httpStatus
    ) throws Exception {
        when(mockUserService.madeSuperuser(anyString(), anyInt()))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                put("/api/users/{user}/super", 123)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().is(httpStatus.value()))
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockUserService)
                .madeSuperuser(anyString(), anyInt());
    }

    @Test
    void testGetUsers_requestFromRegularUser_shouldReturnNotAllFields() throws Exception {
        final List<UserDetailsDtoResponse> users = new ArrayList<>();
        users.add(new UserDetailsDtoResponse(
                        1, "user1", null,
                        LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                        true, false, null,
                        UserStatus.FULL.name(), null, 0
                )
        );
        users.add(new UserDetailsDtoResponse(
                        2, "user2", null,
                        LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                        false, false, null,
                        UserStatus.FULL.name(), null, 0
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
                        UserStatus.FULL.name(), null, 0
                )
        );
        users.add(new UserDetailsDtoResponse(
                        2, "user2", "user2@email.com",
                        LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                        false, false, true,
                        UserStatus.FULL.name(), null, 0
                )
        );
        users.add(new UserDetailsDtoResponse(
                        3, "user3", "user3@email.com",
                        LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS),
                        false, true, false,
                        UserStatus.FULL.name(), null, 0
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

    static Stream<Arguments> getUsersServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR),
                Arguments.arguments(ErrorCode.WRONG_SESSION_TOKEN)
        );
    }

    @ParameterizedTest
    @MethodSource("getUsersServiceExceptions")
    void testGetUsers_exceptionInService_shouldReturnExceptionDto(ErrorCode errorCode) throws Exception {
        when(mockUserService.getUsers(anyString()))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                get("/api/users")
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

    static Stream<Arguments> banUserServiceExceptions() {
        return Stream.of(
                Arguments.arguments(ErrorCode.DATABASE_ERROR, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.WRONG_SESSION_TOKEN, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.FORBIDDEN_OPERATION, HttpStatus.BAD_REQUEST),
                Arguments.arguments(ErrorCode.USER_NOT_FOUND, HttpStatus.NOT_FOUND)
        );
    }

    @ParameterizedTest
    @MethodSource("banUserServiceExceptions")
    void testBanUser_exceptionInService_shouldReturnExceptionDto(
            ErrorCode errorCode, HttpStatus httpStatus
    ) throws Exception {
        when(mockUserService.banUser(anyString(), anyInt()))
                .thenThrow(new ServerException(errorCode));

        mvc.perform(
                post("/api/users/{user}/restrict", 123)
                        .cookie(new Cookie(COOKIE_NAME, COOKIE_VALUE))
                        .contentType(MediaType.APPLICATION_JSON)
        )
                .andExpect(status().is(httpStatus.value()))
                .andExpect(cookie().doesNotExist(COOKIE_NAME))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.errors", hasSize(1)))
                .andExpect(jsonPath("$.errors[0].errorCode").value(errorCode.name()))
                .andExpect(jsonPath("$.errors[0].field").value(errorCode.getErrorCauseField()))
                .andExpect(jsonPath("$.errors[0].message").value(errorCode.getMessage()));

        verify(mockUserService).banUser(anyString(), anyInt());
    }
}