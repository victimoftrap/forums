package net.thumbtack.forums.validator;

import net.thumbtack.forums.configuration.ServerConfigurationProperties;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component("passwordValidator")
public class PasswordValidator {
    private final ServerConfigurationProperties properties;

    @Autowired
    public PasswordValidator(ServerConfigurationProperties properties) {
        this.properties = properties;
    }

    public boolean validate(final String password) {
        return password.length() >= properties.getMinPasswordLength();
    }
}
