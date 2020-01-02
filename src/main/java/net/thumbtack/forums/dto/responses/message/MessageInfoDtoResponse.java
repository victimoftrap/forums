package net.thumbtack.forums.dto.responses.message;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class MessageInfoDtoResponse {
    private int id;
    private String creator;
    private String subject;
    private List<String> body;
    private String priority;
    private List<String> tags;
    private LocalDateTime created;
    private double rating;
    private int rated;
    private List<CommentInfoDtoResponse> comments;

    @JsonCreator
    public MessageInfoDtoResponse(
            @JsonProperty("id") int id,
            @JsonProperty("creator") String creator,
            @JsonProperty("subject") String subject,
            @JsonProperty("body") List<String> body,
            @JsonProperty("priority") String priority,
            @JsonProperty("tags") List<String> tags,
            @JsonProperty("created") LocalDateTime created,
            @JsonProperty("rating") double rating,
            @JsonProperty("rated") int rated,
            @JsonProperty("comments") List<CommentInfoDtoResponse> comments
    ) {
        this.id = id;
        this.creator = creator;
        this.subject = subject;
        this.body = body;
        this.priority = priority;
        this.tags = tags;
        this.created = created;
        this.rating = rating;
        this.rated = rated;
        this.comments = comments;
    }

    public int getId() {
        return id;
    }

    public String getCreator() {
        return creator;
    }

    public String getSubject() {
        return subject;
    }

    public List<String> getBody() {
        return body;
    }

    public String getPriority() {
        return priority;
    }

    public List<String> getTags() {
        return tags;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public double getRating() {
        return rating;
    }

    public int getRated() {
        return rated;
    }

    public List<CommentInfoDtoResponse> getComments() {
        return comments;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MessageInfoDtoResponse)) return false;
        MessageInfoDtoResponse response = (MessageInfoDtoResponse) o;
        return id == response.id &&
                Double.compare(response.rating, rating) == 0 &&
                rated == response.rated &&
                Objects.equals(creator, response.creator) &&
                Objects.equals(subject, response.subject) &&
                Objects.equals(body, response.body) &&
                Objects.equals(priority, response.priority) &&
                Objects.equals(tags, response.tags) &&
                Objects.equals(created, response.created) &&
                Objects.equals(comments, response.comments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, creator, subject, body,
                priority, tags, created, rating, rated, comments
        );
    }
}
