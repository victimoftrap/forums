package net.thumbtack.forums.integration;

import net.thumbtack.forums.utils.MyBatisConnectionUtils;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.web.client.RestTemplate;

public class BaseIntegrationEnvironment {
    protected static final String SERVER_URL = "http://localhost:8080/api";
    protected RestTemplate restTemplate = new RestTemplate();

    @BeforeAll
    public static void init() {
        Assumptions.assumeTrue(MyBatisConnectionUtils.initSqlSessionFactory());
    }

    @BeforeEach
    public void clear() {
        restTemplate.postForObject(
                SERVER_URL + "/debug/clear", null, Void.class
        );
    }
}
