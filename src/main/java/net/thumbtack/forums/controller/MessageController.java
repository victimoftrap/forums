package net.thumbtack.forums.controller;

import net.thumbtack.forums.service.MessageService;
import net.thumbtack.forums.dto.requests.message.*;
import net.thumbtack.forums.dto.responses.message.MessageDtoResponse;
import net.thumbtack.forums.dto.responses.message.EditMessageOrCommentDtoResponse;
import net.thumbtack.forums.dto.responses.message.MadeBranchFromCommentDtoResponse;
import net.thumbtack.forums.dto.responses.message.MessageInfoDtoResponse;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.exception.ServerException;
import net.thumbtack.forums.validator.message.AvailableOrder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/messages")
@Validated
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
            value = "/{message}/priority",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EmptyDtoResponse> changePriority(
            @CookieValue(value = COOKIE_NAME) String token,
            @PathVariable("message") int id,
            @RequestBody @Valid ChangeMessagePriorityDtoRequest request
    ) throws ServerException {
        return ResponseEntity.ok(messageService.changeMessagePriority(token, id, request));
    }

    @PutMapping(
            value = "/{comment}/up",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MadeBranchFromCommentDtoResponse> newBranch(
            @CookieValue(value = COOKIE_NAME) String token,
            @PathVariable("comment") int id,
            @RequestBody @Valid MadeBranchFromCommentDtoRequest request
    ) throws ServerException {
        return ResponseEntity
                .ok(messageService.newBranchFromComment(token, id, request));
    }

    @PutMapping(
            value = "/{id}/publish",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EmptyDtoResponse> publish(
            @CookieValue(value = COOKIE_NAME) String token,
            @PathVariable("id") int id,
            @RequestBody @Valid PublicationDecisionDtoRequest request
    ) throws ServerException {
        return ResponseEntity
                .ok(messageService.publish(token, id, request));
    }

    @PostMapping(
            value = "/{id}/rating",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EmptyDtoResponse> rate(
            @CookieValue(value = COOKIE_NAME) String token,
            @PathVariable("id") int id,
            @RequestBody @Valid RateMessageDtoRequest request
    ) throws ServerException {
        return ResponseEntity
                .ok(messageService.rate(token, id, request));
    }

    @GetMapping(
            value = "/{id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MessageInfoDtoResponse> getMessage(
            @CookieValue(value = COOKIE_NAME) String token,
            @PathVariable("id") int id,
            @RequestParam(value = "allversions", required = false) Boolean allVersions,
            @RequestParam(value = "nocomments", required = false) Boolean noComments,
            @RequestParam(value = "unpublished", required = false) Boolean unpublished,
            @RequestParam(value = "order", required = false) @AvailableOrder String order
    ) throws ServerException {
        return ResponseEntity
                .ok(messageService.getMessage(token, id, allVersions, noComments, unpublished, order));
    }
}
