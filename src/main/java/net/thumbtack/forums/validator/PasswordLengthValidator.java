package net.thumbtack.forums.validator;

import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component("passwordLengthValidator")
public class PasswordLengthValidator {
    private final ServerConfigurationProperties properties;

    @Autowired
    public PasswordLengthValidator(final ServerConfigurationProperties properties) {
        this.properties = properties;
    }

    public boolean isValid(final String password) {
        return password.length() >= properties.getMinPasswordLength();
    }
}
