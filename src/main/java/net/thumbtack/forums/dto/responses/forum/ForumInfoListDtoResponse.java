package net.thumbtack.forums.dto.responses.forum;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class ForumInfoListDtoResponse {
    private List<ForumInfoDtoResponse> forums;

    public ForumInfoListDtoResponse() {
        forums = new ArrayList<>();
    }

    public ForumInfoListDtoResponse(final List<ForumInfoDtoResponse> forums) {
        this.forums = forums;
    }

    public List<ForumInfoDtoResponse> getForums() {
        return forums;
    }

    public void setForums(final List<ForumInfoDtoResponse> forums) {
        this.forums = forums;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ForumInfoListDtoResponse)) return false;
        ForumInfoListDtoResponse that = (ForumInfoListDtoResponse) o;
        return Objects.equals(getForums(), that.getForums());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getForums());
    }
}
