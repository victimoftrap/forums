package net.thumbtack.forums.configuration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertEquals;

@RunWith(SpringRunner.class)
@PropertySource("classpath:application.properties")
@ContextConfiguration(
        classes = TestPropertiesConfiguration.class,
        initializers = ConfigFileApplicationContextInitializer.class
)
public class ServerConfigurationPropertiesTest {
    @Autowired
    private ServerConfigurationProperties properties;

    @Test
    public void testValueFromProperties() {
        assertEquals(8888, properties.getRestHttpPort());
        assertEquals(10, properties.getBanTime());
        assertEquals(5, properties.getMaxBanCount());
        assertEquals(50, properties.getMaxNameLength());
        assertEquals(10, properties.getMinPasswordLength());
    }
}
