package net.thumbtack.forums.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "configuration")
public class ServerConfigurationProperties {
    private int restHttpPort;
    private int banTime;
    private int maxBanCount;
    private int maxNameLength;
    private int minPasswordLength;

    public int getRestHttpPort() {
        return restHttpPort;
    }

    public void setRestHttpPort(int restHttpPort) {
        this.restHttpPort = restHttpPort;
    }

    public int getBanTime() {
        return banTime;
    }

    public void setBanTime(int banTime) {
        this.banTime = banTime;
    }

    public int getMaxBanCount() {
        return maxBanCount;
    }

    public void setMaxBanCount(int maxBanCount) {
        this.maxBanCount = maxBanCount;
    }

    public int getMaxNameLength() {
        return maxNameLength;
    }

    public void setMaxNameLength(int maxNameLength) {
        this.maxNameLength = maxNameLength;
    }

    public int getMinPasswordLength() {
        return minPasswordLength;
    }

    public void setMinPasswordLength(int minPasswordLength) {
        this.minPasswordLength = minPasswordLength;
    }
}
