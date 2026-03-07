package com.tsuzero.tsundoku.goodreads;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "goodreads")
public class GoodreadsProperties {

    private String userId;
    private String shelf = "to-read";

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getShelf() { return shelf; }
    public void setShelf(String shelf) { this.shelf = shelf; }
}
