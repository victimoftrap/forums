package net.thumbtack.forums.dto.responses.settings;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SettingsDtoResponse {
    private Integer banTime;
    private Integer maxBanCount;
    private Integer maxNameLength;
    private Integer minPasswordLength;

    @JsonCreator
    public SettingsDtoResponse(
            @JsonProperty("banTime") Integer banTime,
            @JsonProperty("maxBanCount") Integer maxBanCount,
            @JsonProperty("maxNameLength") Integer maxNameLength,
            @JsonProperty("minPasswordLength") Integer minPasswordLength) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SettingsDtoResponse)) return false;
        SettingsDtoResponse that = (SettingsDtoResponse) o;
        return Objects.equals(banTime, that.banTime) &&
                Objects.equals(maxBanCount, that.maxBanCount) &&
                Objects.equals(maxNameLength, that.maxNameLength) &&
                Objects.equals(minPasswordLength, that.minPasswordLength);
    }

    @Override
    public int hashCode() {
        return Objects.hash(banTime, maxBanCount, maxNameLength, minPasswordLength);
    }
}
