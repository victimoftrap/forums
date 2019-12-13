package net.thumbtack.forums.dto.responses.settings;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SettingsDtoResponse {
    private Integer banTime;
    private Integer maxBanCount;
    private Integer maxNameLength;
    private Integer minPasswordLength;

    public SettingsDtoResponse(Integer banTime,
                               Integer maxBanCount,
                               Integer maxNameLength,
                               Integer minPasswordLength) {
        this.banTime = banTime;
        this.maxBanCount = maxBanCount;
        this.maxNameLength = maxNameLength;
        this.minPasswordLength = minPasswordLength;
    }

    public Integer getBanTime() {
        return banTime;
    }

    public Integer getMaxBanCount() {
        return maxBanCount;
    }

    public Integer getMaxNameLength() {
        return maxNameLength;
    }

    public Integer getMinPasswordLength() {
        return minPasswordLength;
    }
}
