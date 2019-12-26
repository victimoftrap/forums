package net.thumbtack.forums.dto.responses.message;

import java.time.LocalDateTime;
import java.util.List;

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

    public MessageInfoDtoResponse(int id, String creator, String subject, List<String> body,
                                  String priority, List<String> tags, LocalDateTime created,
                                  double rating, int rated, List<CommentInfoDtoResponse> comments) {
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
}
