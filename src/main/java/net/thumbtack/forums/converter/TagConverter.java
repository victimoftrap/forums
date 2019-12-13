package net.thumbtack.forums.converter;

import net.thumbtack.forums.model.Tag;

import java.util.ArrayList;
import java.util.List;

public class TagConverter {
    public static List<Tag> tagNamesToTagList(final List<String> tagNames) {
        if (tagNames == null) {
        	// REVU а лучше не возвращать null, а вернуть пустой список
        	// тогда всегда будет список, может быть, пустой
        	// а NPE не будет
            return null;
        }
        final List<Tag> tags = new ArrayList<>();
        tagNames.forEach(tag -> tags.add(new Tag(tag)));
        return tags;
    }
}
