package net.thumbtack.forums.exception;

public enum RequestFieldName {
    NONE(null),
    USERNAME("name"),
    PASSWORD("password"),
    OLD_PASSWORD("oldPassword"),
    EMAIL("email"),
    FORUM_NAME("name"),
    FORUM_TYPE("type")
    ;

    private String name;

    RequestFieldName() {
    }

    private RequestFieldName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}