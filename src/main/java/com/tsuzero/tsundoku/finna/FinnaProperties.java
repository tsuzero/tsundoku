package com.tsuzero.tsundoku.finna;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "finna")
public class FinnaProperties {

    private String baseUrl = "https://api.finna.fi/v1";
    private String helmetBuildingFilter = "0/Helmet/";
    private int timeoutSeconds = 10;

    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }

    public String getHelmetBuildingFilter() { return helmetBuildingFilter; }
    public void setHelmetBuildingFilter(String helmetBuildingFilter) {
        this.helmetBuildingFilter = helmetBuildingFilter;
    }

    public int getTimeoutSeconds() { return timeoutSeconds; }
    public void setTimeoutSeconds(int timeoutSeconds) { this.timeoutSeconds = timeoutSeconds; }
}
