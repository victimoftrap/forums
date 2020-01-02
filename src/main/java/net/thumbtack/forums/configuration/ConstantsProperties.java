package net.thumbtack.forums.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Configuration
@ConfigurationProperties
@PropertySource("classpath:constants.properties")
public class ConstantsProperties {
    @Value("${datetime.pattern}")
    private String datetimePattern;

    @Value("${datetime.permanent_ban}")
    private String permanentBanDatetime;

    @Value("${session_id_key}")
    private String sessionIdKey;

    public String getDatetimePattern() {
        return datetimePattern;
    }

    public String getPermanentBanDatetime() {
        return permanentBanDatetime;
    }

    public String getSessionIdKey() {
        return sessionIdKey;
    }
}
