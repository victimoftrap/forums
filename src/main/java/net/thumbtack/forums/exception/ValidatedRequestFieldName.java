package net.thumbtack.forums.exception;

public enum ValidatedRequestFieldName {
    USERNAME("name"),
    PASSWORD("password"),
    OLD_PASSWORD("oldPassword"),
    EMAIL("email"),
    FORUM_NAME("name"),
    FORUM_TYPE("type"),
    MESSAGE_SUBJECT("subject"),
    MESSAGE_BODY("body"),
    MESSAGE_PRIORITY("priority"),
    PUBLICATION_DECISION("decision");

    private String name;

    ValidatedRequestFieldName() {
    }

    ValidatedRequestFieldName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
