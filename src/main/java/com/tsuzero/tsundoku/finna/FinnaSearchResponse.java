package com.tsuzero.tsundoku.finna;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FinnaSearchResponse(
        String status,
        @JsonProperty("resultCount") int resultCount,
        List<FinnaRecord> records
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FinnaRecord(
            String id,
            String title,
            List<Building> buildings,
            List<String> authors
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Building(
            String value,
            String translated
    ) {}
}
