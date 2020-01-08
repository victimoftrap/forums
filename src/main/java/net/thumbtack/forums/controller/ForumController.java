package net.thumbtack.forums.controller;

import net.thumbtack.forums.dto.responses.message.ListMessageInfoDtoResponse;
import net.thumbtack.forums.service.ForumService;
import net.thumbtack.forums.service.MessageService;
import net.thumbtack.forums.dto.requests.forum.CreateForumDtoRequest;
import net.thumbtack.forums.dto.requests.message.CreateMessageDtoRequest;
import net.thumbtack.forums.dto.responses.EmptyDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumInfoDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumInfoListDtoResponse;
import net.thumbtack.forums.dto.responses.message.MessageDtoResponse;
import net.thumbtack.forums.exception.ServerException;

import net.thumbtack.forums.validator.message.AvailableOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

@RestController
@RequestMapping("/api/forums")
public class ForumController {
    private final ForumService forumService;
    private final MessageService messageService;
    private final String COOKIE_NAME = "JAVASESSIONID";

    @Autowired
    public ForumController(final ForumService forumService,
                           final MessageService messageService) {
        this.forumService = forumService;
        this.messageService = messageService;
    }

    @PostMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ForumDtoResponse> createForum(
            @CookieValue(value = COOKIE_NAME) String token,
            @RequestBody @Valid CreateForumDtoRequest request) throws ServerException {
        return ResponseEntity.ok(forumService.createForum(token, request));
    }

    @DeleteMapping(
            value = "/{forum_id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<EmptyDtoResponse> deleteForum(
            @CookieValue(value = COOKIE_NAME) String token,
            @PathVariable("forum_id") int forumId) throws ServerException {
        return ResponseEntity.ok(forumService.deleteForum(token, forumId));
    }

    @GetMapping(
            value = "/{forum_id}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ForumInfoDtoResponse> getForum(
            @CookieValue(value = COOKIE_NAME) String token,
            @PathVariable("forum_id") int forumId) throws ServerException {
        return ResponseEntity.ok(forumService.getForum(token, forumId));
    }

    @GetMapping(
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ForumInfoListDtoResponse> getAll(
            @CookieValue(value = COOKIE_NAME) String token) throws ServerException {
        return ResponseEntity.ok(forumService.getForums(token));
    }

    @PostMapping(
            value = "/{forum_id}/messages",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<MessageDtoResponse> createMessage(
            @CookieValue(value = COOKIE_NAME) String token,
            @PathVariable("forum_id") int forumId,
            @RequestBody @Valid CreateMessageDtoRequest request) throws ServerException {
        return ResponseEntity.ok(messageService.addMessage(token, forumId, request));
    }

    @GetMapping(
            value = "/{forum_id}/messages",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ListMessageInfoDtoResponse> getMessages(
            @CookieValue(value = COOKIE_NAME) String token,
            @PathVariable("forum_id") int forumId,
            @RequestParam(value = "allversions", required = false) Boolean allVersions,
            @RequestParam(value = "nocomments", required = false) Boolean noComments,
            @RequestParam(value = "unpublished", required = false) Boolean unpublished,
            @RequestParam(value = "order", required = false) @AvailableOrder String order,
            @RequestParam(value = "tags", required = false) List<@NotBlank String> tags,
            @RequestParam(value = "offset", required = false) Integer offset,
            @RequestParam(value = "limit", required = false) Integer limit
    ) throws ServerException {
        return ResponseEntity.ok(
                messageService.getForumMessageList(
                        token, forumId,
                        allVersions, noComments, unpublished,
                        tags, order, offset, limit
                )
        );
    }
}
