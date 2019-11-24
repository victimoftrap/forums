package net.thumbtack.forums.controller;

import net.thumbtack.forums.dto.EmptyDtoResponse;
import net.thumbtack.forums.dto.LoginUserDtoRequest;
import net.thumbtack.forums.dto.UserDtoResponse;
import net.thumbtack.forums.service.UserService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    private final UserService userService;

    @Autowired
    public SessionController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping(
            value = "/",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDtoResponse> login(@RequestBody @Valid LoginUserDtoRequest request) {
        final UserDtoResponse response = userService.login(request);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, "JAVASESSIONID=" + response.getSessionToken())
                .body(response);
    }

    @DeleteMapping("/")
    public ResponseEntity<EmptyDtoResponse> logout(@CookieValue(value = "JAVASESSIONID") String token) {
        return ResponseEntity
                .ok(userService.logout(token));
    }
}
