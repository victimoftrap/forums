package net.thumbtack.forums.integration.webClient;

import net.thumbtack.forums.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.forums.exception.ErrorCode;
import net.thumbtack.forums.exception.ValidatedRequestFieldName;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WebClientUserControllerIntegrationTest {
    @Autowired
    private WebTestClient webTestClient;
    private static final String SERVER_URL = "http://localhost:8080/api";

    @BeforeEach
    public void clear() {
        webTestClient
                .post()
                .uri(SERVER_URL + "/debug/clear")
                .exchange();
    }

    @Test
    void testRegisterUser() {
        RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "Chase", "r.chase@test.com", "w3ryStr0nGPa55wD"
        );

        webTestClient
                .post()
                .uri(SERVER_URL + "/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectHeader().exists(HttpHeaders.SET_COOKIE)
                .expectBody()
                .jsonPath("$.id").isNotEmpty()
                .jsonPath("$.name").isNotEmpty()
                .jsonPath("$.name").isEqualTo(request.getName())
                .jsonPath("$.email").isNotEmpty()
                .jsonPath("$.email").isEqualTo(request.getEmail());
    }

    @Test
    void testRegisterUser_invalidRequestData_shouldReturnBadRequest() {
        RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "", "test-email@email.com", "w3ryStr0nGPa55wD"
        );

        webTestClient
                .post()
                .uri(SERVER_URL + "/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectHeader().doesNotExist(HttpHeaders.SET_COOKIE)
                .expectBody()
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0].errorCode").isEqualTo(ErrorCode.INVALID_REQUEST_DATA.name())
                .jsonPath("$.errors[0].field").isEqualTo(ValidatedRequestFieldName.USERNAME.getName())
                .jsonPath("$.errors[0].message").isNotEmpty();
    }

    @Test
    void testRegisterUser_registerExistingUser_shouldReturnBadRequest() {
        RegisterUserDtoRequest request = new RegisterUserDtoRequest(
                "TestUser", "test-email@email.com", "w3ryStr0nGPa55wD"
        );
        webTestClient
                .post()
                .uri(SERVER_URL + "/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange();

        webTestClient
                .post()
                .uri(SERVER_URL + "/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectHeader().doesNotExist(HttpHeaders.SET_COOKIE)
                .expectBody()
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0].errorCode").isEqualTo(ErrorCode.USER_NAME_ALREADY_USED.name())
                .jsonPath("$.errors[0].field").isEqualTo(ErrorCode.USER_NAME_ALREADY_USED.getErrorCauseField())
                .jsonPath("$.errors[0].message").isEqualTo(ErrorCode.USER_NAME_ALREADY_USED.getMessage());
    }
}
