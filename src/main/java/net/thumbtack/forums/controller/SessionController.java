package net.thumbtack.forums.controller;

import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.dto.requests.user.LoginUserDtoRequest;
import net.thumbtack.forums.dto.responses.user.UserDtoResponse;
import net.thumbtack.forums.service.UserService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {
    private final UserService userService;
    private final String COOKIE_NAME = "JAVASESSIONID";

    @Autowired
    public SessionController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDtoResponse> login(@RequestBody @Valid LoginUserDtoRequest request,
                                                 HttpServletResponse response) {
        final UserDtoResponse userResponse = userService.login(request);

        final Cookie sessionCookie = new Cookie(COOKIE_NAME, userResponse.getSessionToken());
        sessionCookie.setHttpOnly(true);
        response.addCookie(sessionCookie);
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EmptyDtoResponse> logout(@CookieValue(value = COOKIE_NAME) String token) {
        return ResponseEntity.ok(userService.logout(token));
    }
}
