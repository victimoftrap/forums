package net.thumbtack.forums.integration;

import net.thumbtack.forums.dto.requests.forum.CreateForumDtoRequest;
import net.thumbtack.forums.dto.requests.message.CreateCommentDtoRequest;
import net.thumbtack.forums.dto.requests.message.CreateMessageDtoRequest;
import net.thumbtack.forums.dto.requests.message.RateMessageDtoRequest;
import net.thumbtack.forums.dto.requests.user.RegisterUserDtoRequest;
import net.thumbtack.forums.dto.responses.statistic.*;
import net.thumbtack.forums.model.enums.ForumType;
import net.thumbtack.forums.model.enums.MessagePriority;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class StatisticsIntegrationTest extends BaseIntegrationEnvironment {
    @Test
    void testGetStatisticsInForum() {
        final RegisterUserDtoRequest registerUser1 = new RegisterUserDtoRequest(
                "user1", "user1@email.com", "utubfm43mWEa"
        );
        final String userToken1 = getSessionTokenFromHeaders(registerUser(registerUser1));
        final RegisterUserDtoRequest registerUser2 = new RegisterUserDtoRequest(
                "user2", "user2@email.com", "utubfm43mWEa"
        );
        final String userToken2 = getSessionTokenFromHeaders(registerUser(registerUser2));
        final RegisterUserDtoRequest registerUser3 = new RegisterUserDtoRequest(
                "user3", "user3@email.com", "utubfm43mWEa"
        );
        final String userToken3 = getSessionTokenFromHeaders(registerUser(registerUser3));

        final RegisterUserDtoRequest registerForumOwner1 = new RegisterUserDtoRequest(
                "ForumOwner1", "fo1@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken1 = getSessionTokenFromHeaders(registerUser(registerForumOwner1));
        final CreateForumDtoRequest createForumRequest1 = new CreateForumDtoRequest(
                "FirstForum", ForumType.UNMODERATED.name()
        );
        final int forumId1 = createForum(createForumRequest1, forumOwnerToken1).getBody().getId();

        final CreateMessageDtoRequest createMessage1 = new CreateMessageDtoRequest(
                "Subject #1", "Body #1",
                MessagePriority.HIGH.name(), Arrays.asList("Tag1")
        );
        final int messageId1 = createMessage(userToken1, forumId1, createMessage1).getBody().getId();
        final int commentId1 = createComment(userToken2, messageId1,
                new CreateCommentDtoRequest("Comment #1")).getBody().getId();
        final int commentId2 = createComment(forumOwnerToken1, messageId1,
                new CreateCommentDtoRequest("Comment #2")).getBody().getId();
        final int commentId3 = createComment(userToken1, commentId1,
                new CreateCommentDtoRequest("Comment #3")).getBody().getId();

        final CreateMessageDtoRequest createMessage2 = new CreateMessageDtoRequest(
                "Subject #2", "Body #2",
                null, Arrays.asList("Tag2", "Tag1")
        );
        final int messageId2 = createMessage(userToken2, forumId1, createMessage2).getBody().getId();
        final int commentId4 = createComment(userToken3, messageId2,
                new CreateCommentDtoRequest("Comment #4")).getBody().getId();
        final int commentId5 = createComment(forumOwnerToken1, commentId4,
                new CreateCommentDtoRequest("Comment #5")).getBody().getId();

        rateMessage(userToken2, messageId1, new RateMessageDtoRequest(5));
        rateMessage(userToken3, messageId1, new RateMessageDtoRequest(5));

        rateMessage(userToken3, commentId3, new RateMessageDtoRequest(2));

        rateMessage(forumOwnerToken1, commentId1, new RateMessageDtoRequest(3));
        rateMessage(userToken1, commentId1, new RateMessageDtoRequest(5));

        final ResponseEntity<MessagesCountDtoResponse> messagesCountResponse = getMessagesCount(userToken1, forumId1);
        assertEquals(HttpStatus.OK, messagesCountResponse.getStatusCode());
        assertNotNull(messagesCountResponse.getBody());
        assertEquals(2, messagesCountResponse.getBody().getMessagesCount());

        final ResponseEntity<CommentsCountDtoResponse> commentsCountResponse = getCommentsCount(userToken1, forumId1);
        assertEquals(HttpStatus.OK, commentsCountResponse.getStatusCode());
        assertNotNull(commentsCountResponse.getBody());
        assertEquals(5, commentsCountResponse.getBody().getCommentsCount());

        final ResponseEntity<MessageRatingListDtoResponse> messagesRatings = getMessagesRatings(
                userToken1, forumId1, 300, 0
        );
        assertEquals(HttpStatus.OK, messagesRatings.getStatusCode());
        assertNotNull(messagesRatings.getBody());

        final List<MessageRatingDtoResponse> messages = messagesRatings.getBody().getMessages();
        assertEquals(7, messages.size());
        assertEquals(messageId1, messages.get(0).getMessageId());
        assertEquals(5, messages.get(0).getRating());
        assertEquals(2, messages.get(0).getRated());

        assertEquals(commentId1, messages.get(1).getMessageId());
        assertEquals(4, messages.get(1).getRating());
        assertEquals(2, messages.get(1).getRated());

        assertEquals(commentId3, messages.get(2).getMessageId());
        assertEquals(2, messages.get(2).getRating());
        assertEquals(1, messages.get(2).getRated());

        assertEquals(commentId5, messages.get(6).getMessageId());
        assertEquals(0, messages.get(6).getRating());
        assertEquals(0, messages.get(6).getRated());

        final ResponseEntity<UserRatingListDtoResponse> usersRatings = getUsersRatings(
                userToken1, forumId1, 300, 0
        );
        assertEquals(HttpStatus.OK, usersRatings.getStatusCode());
        assertNotNull(usersRatings.getBody());

        final List<UserRatingDtoResponse> users = usersRatings.getBody().getUsers();
        assertEquals(4, users.size());
        assertEquals(registerUser1.getName(), users.get(0).getName());
        assertEquals(4, users.get(0).getRating());
        assertEquals(3, users.get(0).getRated());

        assertEquals(registerUser2.getName(), users.get(1).getName());
        assertEquals(4, users.get(1).getRating());
        assertEquals(2, users.get(1).getRated());

        assertEquals(registerForumOwner1.getName(), users.get(3).getName());
        assertEquals(0, users.get(3).getRating());
        assertEquals(0, users.get(3).getRated());
    }
    @Test
    void testGetStatisticsInServer() {
        final RegisterUserDtoRequest registerUser1 = new RegisterUserDtoRequest(
                "user1", "user1@email.com", "utubfm43mWEa"
        );
        final String userToken1 = getSessionTokenFromHeaders(registerUser(registerUser1));
        final RegisterUserDtoRequest registerUser2 = new RegisterUserDtoRequest(
                "user2", "user2@email.com", "utubfm43mWEa"
        );
        final String userToken2 = getSessionTokenFromHeaders(registerUser(registerUser2));
        final RegisterUserDtoRequest registerUser3 = new RegisterUserDtoRequest(
                "user3", "user3@email.com", "utubfm43mWEa"
        );
        final String userToken3 = getSessionTokenFromHeaders(registerUser(registerUser3));

        final RegisterUserDtoRequest registerForumOwner1 = new RegisterUserDtoRequest(
                "ForumOwner1", "fo1@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken1 = getSessionTokenFromHeaders(registerUser(registerForumOwner1));
        final CreateForumDtoRequest createForumRequest1 = new CreateForumDtoRequest(
                "FirstForum", ForumType.UNMODERATED.name()
        );
        final int forumId1 = createForum(createForumRequest1, forumOwnerToken1).getBody().getId();

        final RegisterUserDtoRequest registerForumOwner2 = new RegisterUserDtoRequest(
                "ForumOwner2", "fo2@email.com", "w3ryStr0nGPa55wD"
        );
        final String forumOwnerToken2 = getSessionTokenFromHeaders(registerUser(registerForumOwner2));
        final CreateForumDtoRequest createForumRequest2 = new CreateForumDtoRequest(
                "SecondForum", ForumType.UNMODERATED.name()
        );
        final int forumId2 = createForum(createForumRequest2, forumOwnerToken2).getBody().getId();

        final CreateMessageDtoRequest createMessage1 = new CreateMessageDtoRequest(
                "Subject #1", "Body #1",
                MessagePriority.HIGH.name(), Arrays.asList("Tag1")
        );
        final int messageId1 = createMessage(userToken1, forumId1, createMessage1).getBody().getId();
        final int commentId1 = createComment(userToken2, messageId1,
                new CreateCommentDtoRequest("Comment #1")).getBody().getId();
        final int commentId2 = createComment(forumOwnerToken1, messageId1,
                new CreateCommentDtoRequest("Comment #2")).getBody().getId();
        final int commentId3 = createComment(userToken1, commentId1,
                new CreateCommentDtoRequest("Comment #3")).getBody().getId();

        final CreateMessageDtoRequest createMessage2 = new CreateMessageDtoRequest(
                "Subject #2", "Body #2",
                null, Arrays.asList("Tag2", "Tag1")
        );
        final int messageId2 = createMessage(userToken2, forumId2, createMessage2).getBody().getId();
        final int commentId4 = createComment(userToken3, messageId2,
                new CreateCommentDtoRequest("Comment #4")).getBody().getId();
        final int commentId5 = createComment(forumOwnerToken1, commentId4,
                new CreateCommentDtoRequest("Comment #5")).getBody().getId();

        rateMessage(userToken2, messageId1, new RateMessageDtoRequest(5));
        rateMessage(userToken3, messageId1, new RateMessageDtoRequest(5));

        rateMessage(userToken3, commentId3, new RateMessageDtoRequest(2));

        rateMessage(forumOwnerToken1, commentId1, new RateMessageDtoRequest(3));
        rateMessage(userToken1, commentId1, new RateMessageDtoRequest(5));

        rateMessage(forumOwnerToken1, messageId2, new RateMessageDtoRequest(4));
        rateMessage(userToken1, messageId2, new RateMessageDtoRequest(1));

        rateMessage(forumOwnerToken2, commentId4, new RateMessageDtoRequest(4));
        rateMessage(userToken2, commentId4, new RateMessageDtoRequest(5));

        final ResponseEntity<MessagesCountDtoResponse> messagesCountResponse = getMessagesCount(userToken1, null);
        assertEquals(HttpStatus.OK, messagesCountResponse.getStatusCode());
        assertNotNull(messagesCountResponse.getBody());
        assertEquals(2, messagesCountResponse.getBody().getMessagesCount());

        assertEquals(1, getMessagesCount(userToken1, forumId1).getBody().getMessagesCount());
        assertEquals(1, getMessagesCount(userToken1, forumId2).getBody().getMessagesCount());

        final ResponseEntity<CommentsCountDtoResponse> commentsCountResponse = getCommentsCount(userToken1, null);
        assertEquals(HttpStatus.OK, commentsCountResponse.getStatusCode());
        assertNotNull(commentsCountResponse.getBody());
        assertEquals(5, commentsCountResponse.getBody().getCommentsCount());

        assertEquals(3, getCommentsCount(userToken1, forumId1).getBody().getCommentsCount());
        assertEquals(2, getCommentsCount(userToken1, forumId2).getBody().getCommentsCount());

        final ResponseEntity<MessageRatingListDtoResponse> messagesRatings = getMessagesRatings(
                userToken1, null, 5, 0
        );
        assertEquals(HttpStatus.OK, messagesRatings.getStatusCode());
        assertNotNull(messagesRatings.getBody());

        final List<MessageRatingDtoResponse> messages = messagesRatings.getBody().getMessages();
        assertEquals(5, messages.size());
        assertEquals(messageId1, messages.get(0).getMessageId());
        assertEquals(5, messages.get(0).getRating());
        assertEquals(2, messages.get(0).getRated());

        assertEquals(commentId4, messages.get(1).getMessageId());
        assertEquals(4.5, messages.get(1).getRating());
        assertEquals(2, messages.get(1).getRated());

        assertEquals(commentId1, messages.get(2).getMessageId());
        assertEquals(4, messages.get(2).getRating());
        assertEquals(2, messages.get(2).getRated());

        assertEquals(commentId3, messages.get(4).getMessageId());
        assertEquals(2, messages.get(4).getRating());
        assertEquals(1, messages.get(4).getRated());

        final ResponseEntity<UserRatingListDtoResponse> usersRatings = getUsersRatings(
                userToken1, null, 3, 0
        );
        assertEquals(HttpStatus.OK, usersRatings.getStatusCode());
        assertNotNull(usersRatings.getBody());

        final List<UserRatingDtoResponse> users = usersRatings.getBody().getUsers();
        assertEquals(3, users.size());
        assertEquals(registerUser3.getName(), users.get(0).getName());
        assertEquals(4.5, users.get(0).getRating());
        assertEquals(2, users.get(0).getRated());

        assertEquals(registerUser1.getName(), users.get(1).getName());
        assertEquals(4, users.get(1).getRating());
        assertEquals(3, users.get(1).getRated());

        assertEquals(registerUser2.getName(), users.get(2).getName());
        assertEquals(3.25, users.get(2).getRating());
        assertEquals(4, users.get(2).getRated());

        final ResponseEntity<UserRatingListDtoResponse> usersRatings1 = getUsersRatings(
                userToken1, null, 3, 1
        );
        assertEquals(HttpStatus.OK, usersRatings1.getStatusCode());
        assertNotNull(usersRatings1.getBody());

        final List<UserRatingDtoResponse> users1 = usersRatings1.getBody().getUsers();
        assertEquals(registerUser1.getName(), users1.get(0).getName());
        assertEquals(4, users1.get(0).getRating());
        assertEquals(3, users1.get(0).getRated());

        assertEquals(registerUser2.getName(), users1.get(1).getName());
        assertEquals(3.25, users1.get(1).getRating());
        assertEquals(4, users1.get(1).getRated());

        assertEquals("admin", users1.get(2).getName());
        assertEquals(0, users1.get(2).getRating());
        assertEquals(0, users1.get(2).getRated());
    }
}
