package com.tsuzero.tsundoku.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "features")
public class FeaturesProperties {

    private boolean goodreadsSyncEnabled = true;
    private boolean finnaSyncEnabled = true;

    public boolean isGoodreadsSyncEnabled() { return goodreadsSyncEnabled; }
    public void setGoodreadsSyncEnabled(boolean v) { this.goodreadsSyncEnabled = v; }

    public boolean isFinnaSyncEnabled() { return finnaSyncEnabled; }
    public void setFinnaSyncEnabled(boolean v) { this.finnaSyncEnabled = v; }
}
