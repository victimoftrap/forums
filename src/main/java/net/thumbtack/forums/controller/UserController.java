package net.thumbtack.forums.controller;

import net.thumbtack.forums.service.UserService;
import net.thumbtack.forums.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.requests.user.UpdatePasswordDtoRequest;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.dto.responses.user.UserDetailsListDtoResponse;
import net.thumbtack.forums.dto.responses.user.UserDtoResponse;
import net.thumbtack.forums.exception.ServerException;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;
    private final String COOKIE_NAME = "JAVASESSIONID";

    @Autowired
    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDtoResponse> registerUser(
            @RequestBody @Valid RegisterUserDtoRequest request,
            HttpServletResponse response) throws ServerException {
        final UserDtoResponse userResponse = userService.registerUser(request);

        final Cookie sessionCookie = new Cookie(COOKIE_NAME, userResponse.getSessionToken());
        sessionCookie.setHttpOnly(true);
        response.addCookie(sessionCookie);
        return ResponseEntity.ok(userResponse);
    }

    @DeleteMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EmptyDtoResponse> deleteUser(
            @CookieValue(value = COOKIE_NAME) String token) throws ServerException {
        return ResponseEntity.ok(userService.deleteUser(token));
    }

    @PutMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDtoResponse> updatePassword(
            @CookieValue(value = COOKIE_NAME) String token,
            @RequestBody @Valid UpdatePasswordDtoRequest request,
            HttpServletResponse response) throws ServerException {
        final UserDtoResponse userResponse = userService.updatePassword(token, request);

        final Cookie sessionCookie = new Cookie(COOKIE_NAME, token);
        sessionCookie.setHttpOnly(true);
        response.addCookie(sessionCookie);
        return ResponseEntity.ok(userResponse);
    }

    @PutMapping(
            value = "/{user}/super",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EmptyDtoResponse> madeSuperuser(
            @CookieValue(value = COOKIE_NAME) String token,
            @PathVariable("user") int userId) throws ServerException {
        return ResponseEntity.ok(userService.madeSuperuser(token, userId));
    }

    @GetMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDetailsListDtoResponse> getUsers(
            @CookieValue(value = COOKIE_NAME) String token) throws ServerException {
        return ResponseEntity.ok(userService.getUsers(token));
    }

    @PostMapping(
            value = "/{user}/restrict",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EmptyDtoResponse> banUser(
            @CookieValue(value = COOKIE_NAME) String token,
            @PathVariable("user") int userId) throws ServerException {
        return ResponseEntity.ok(userService.banUser(token, userId));
    }
}
