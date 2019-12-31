package net.thumbtack.forums.controller;

import net.thumbtack.forums.dto.requests.message.ChangeMessagePriorityDtoRequest;
import net.thumbtack.forums.dto.requests.message.EditMessageOrCommentDtoRequest;
import net.thumbtack.forums.dto.responses.message.EditMessageOrCommentDtoResponse;
import net.thumbtack.forums.service.MessageService;
import net.thumbtack.forums.dto.requests.message.CreateCommentDtoRequest;
import net.thumbtack.forums.dto.responses.message.MessageDtoResponse;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.exception.ServerException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/messages")
public class MessageController {
    private final MessageService messageService;
    private final String COOKIE_NAME = "JAVASESSIONID";

    @Autowired
    public MessageController(final MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MessageDtoResponse> createComment(
            @CookieValue(value = COOKIE_NAME) String token,
            @PathVariable("id") int messageId,
            @RequestBody @Valid CreateCommentDtoRequest request
    ) throws ServerException {
        return ResponseEntity.ok(messageService.addComment(token, messageId, request));
    }

    @DeleteMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EmptyDtoResponse> deleteMessageOrComment(
            @CookieValue(value = COOKIE_NAME) String token,
            @PathVariable("id") int id
    ) throws ServerException {
        return ResponseEntity.ok(messageService.deleteMessage(token, id));
    }

    @PutMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EditMessageOrCommentDtoResponse> edit(
            @CookieValue(value = COOKIE_NAME) String token,
            @PathVariable("id") int id,
            @RequestBody @Valid EditMessageOrCommentDtoRequest request
    ) throws ServerException {
        return ResponseEntity.ok(messageService.editMessage(token, id, request));
    }

    @PutMapping(
            value = "/{id}/priority",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EmptyDtoResponse> changePriority(
            @CookieValue(value = COOKIE_NAME) String token,
            @PathVariable("id") int id,
            @RequestBody @Valid ChangeMessagePriorityDtoRequest request
    ) throws ServerException {
        return ResponseEntity.ok(messageService.changeMessagePriority(token, id, request));
    }
}
