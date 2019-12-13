package net.thumbtack.forums.converter;

import net.thumbtack.forums.model.Forum;
import net.thumbtack.forums.dto.responses.forum.ForumInfoDtoResponse;
import net.thumbtack.forums.dto.responses.forum.ForumInfoListDtoResponse;

import java.util.ArrayList;
import java.util.List;

public class ForumConverter {
    public static ForumInfoDtoResponse forumToResponse(final Forum forum) {
        return new ForumInfoDtoResponse(
                forum.getId(),
                forum.getName(),
                forum.getType(),
                forum.getOwner().getUsername(),
                forum.isReadonly(),
                forum.getMessageCount(),
                forum.getCommentCount()
        );
    }

    public static ForumInfoListDtoResponse forumListToResponse(final List<Forum> forums) {
        final List<ForumInfoDtoResponse> list = new ArrayList<>();
        for (final Forum forum : forums) {
            list.add(forumToResponse(forum));
        }
        return new ForumInfoListDtoResponse(list);
    }
}
