package net.thumbtack.forums.dto.responses.message;

import java.util.List;
import java.time.LocalDateTime;

public class CommentInfoDtoResponse {
    private int id;
    private String creator;
    private List<String> body;
    private LocalDateTime created;
    private double rating;
    private int rated;
    private List<CommentInfoDtoResponse> comments;

    public CommentInfoDtoResponse(int id, String creator, List<String> body, LocalDateTime created,
                                  double rating, int rated, List<CommentInfoDtoResponse> comments) {
        this.id = id;
        this.creator = creator;
        this.body = body;
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

    public List<String> getBody() {
        return body;
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
