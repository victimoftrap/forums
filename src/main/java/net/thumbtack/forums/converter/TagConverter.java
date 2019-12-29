package net.thumbtack.forums.converter;

import net.thumbtack.forums.model.Tag;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class TagConverter {
    public static List<Tag> tagNamesToTagList(final List<String> tagNames) {
        if (tagNames == null) {
            return Collections.emptyList();
        }
        final List<Tag> tags = new ArrayList<>();
        tagNames.forEach(tag -> tags.add(new Tag(tag)));
        return tags;
    }

    public static List<String> tagListToTagNamesList(final List<Tag> tags) {
        if (tags == null) {
            return Collections.emptyList();
        }
        final List<String> tagNames = new ArrayList<>();
        tags.forEach(tag -> tagNames.add(tag.getName()));
        return tagNames;
    }
}
