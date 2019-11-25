package net.thumbtack.forums.validator;

import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("usernameLengthValidator")
public class UsernameLengthValidator {
    private final ServerConfigurationProperties properties;

    @Autowired
    public UsernameLengthValidator(final ServerConfigurationProperties properties) {
        this.properties = properties;
    }

    public boolean isValid(final String username) {
        return username.length() < properties.getMaxNameLength();
    }
}
