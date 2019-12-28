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
@PropertySource("classpath:constants.properties")
@ContextConfiguration(
        classes = TestPropertiesConfiguration.class,
        initializers = ConfigFileApplicationContextInitializer.class
)
public class ConstantsPropertiesTest {
    @Autowired
    private ConstantsProperties properties;

    @Test
    public void testReadValuesFromProperties() {
        assertEquals("JAVASESSIONID", properties.getSessionIdKey());
        assertEquals("9999-01-01 00:00:00", properties.getPermanentBanDatetime());
        assertEquals("yyyy-MM-dd HH:mm:ss", properties.getDatetimePattern());
    }
}
