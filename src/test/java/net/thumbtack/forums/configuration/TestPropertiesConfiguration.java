package net.thumbtack.forums.configuration;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableAutoConfiguration
@EnableConfigurationProperties(value = {
        ServerConfigurationProperties.class,
        ConstantsProperties.class
})
public class TestPropertiesConfiguration {
}
