package net.thumbtack.forums.controller;

import net.thumbtack.forums.dto.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.UpdatePasswordDtoRequest;
import net.thumbtack.forums.dto.UserDtoResponse;
import net.thumbtack.forums.service.UserService;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping(
            value = "/",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDtoResponse> registerUser(
            @RequestBody @Valid RegisterUserDtoRequest request) {
        final UserDtoResponse response = userService.registerUser(request);
        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, "JAVASESSIONID=" + response.getSessionToken())
                .body(response);
    }

    @PutMapping(
            value = "/",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<UserDtoResponse> updatePassword(
            @CookieValue(value = "JAVASESSIONID") String token,
            @RequestBody @Valid UpdatePasswordDtoRequest request) {
        final UserDtoResponse response = userService.updatePassword(token, request);
        return ResponseEntity
                .ok(response);
    }
}
