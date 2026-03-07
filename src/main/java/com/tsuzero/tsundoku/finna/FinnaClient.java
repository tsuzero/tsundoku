package com.tsuzero.tsundoku.finna;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Optional;

@Component
public class FinnaClient {

    private static final Logger log = LoggerFactory.getLogger(FinnaClient.class);

    private final WebClient webClient;
    private final FinnaProperties properties;

    public FinnaClient(WebClient finnaWebClient, FinnaProperties properties) {
        this.webClient = finnaWebClient;
        this.properties = properties;
    }

    /**
     * Search by ISBN. Returns first matching record in Helmet.
     */
    public Optional<FinnaSearchResponse.FinnaRecord> searchByIsbn(String isbn) {
        if (isbn == null || isbn.isBlank()) return Optional.empty();

        FinnaSearchResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("lookfor", "isbn:" + isbn)
                        .queryParam("filter[]", "building:\"" + properties.getHelmetBuildingFilter() + "\"")
                        .queryParam("field[]", "id")
                        .queryParam("field[]", "title")
                        .queryParam("field[]", "buildings")
                        .queryParam("limit", "1")
                        .build())
                .retrieve()
                .bodyToMono(FinnaSearchResponse.class)
                .block();

        if (response == null || response.resultCount() == 0 || response.records() == null
                || response.records().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(response.records().get(0));
    }

    /**
     * Search by title + author. Returns first matching record in Helmet.
     */
    public Optional<FinnaSearchResponse.FinnaRecord> searchByTitleAuthor(String title, String author) {
        if (title == null || title.isBlank()) return Optional.empty();

        String query = buildTitleAuthorQuery(title, author);
        log.info("Finna title+author query: [{}]", query);

        FinnaSearchResponse response = webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/search")
                        .queryParam("lookfor", query)
                        .queryParam("filter[]", "building:\"" + properties.getHelmetBuildingFilter() + "\"")
                        .queryParam("field[]", "id")
                        .queryParam("field[]", "title")
                        .queryParam("field[]", "buildings")
                        .queryParam("limit", "1")
                        .build())
                .retrieve()
                .bodyToMono(FinnaSearchResponse.class)
                .block();

        if (response == null || response.resultCount() == 0 || response.records() == null
                || response.records().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(response.records().get(0));
    }

    private String buildTitleAuthorQuery(String title, String author) {
        // Strip trailing series info like " (Wheel of Time, #1)" — the parens and # break Lucene
        String cleanTitle = title.replaceAll("\\s*\\([^)]*#\\d+[^)]*\\)$", "").trim();
        if (cleanTitle.isBlank()) cleanTitle = title;

        if (author == null || author.isBlank()) {
            return "title:\"" + escapePhrase(cleanTitle) + "\"";
        }
        // Finna stores authors surname-first ("Watts, Peter"), so match only the surname token
        String surname = extractSurname(author);
        return "title:\"" + escapePhrase(cleanTitle) + "\" AND author:" + surname;
    }

    /** Extract the last meaningful word from an author name, skipping common suffixes. */
    private static String extractSurname(String author) {
        String[] parts = author.trim().split("\\s+");
        String last = parts[parts.length - 1].replaceAll("[.,]$", "");
        // Skip "Jr", "Sr", "II", "III", "IV" etc.
        if (last.matches("(?i)jr|sr|i{1,3}|iv|v") && parts.length > 1) {
            last = parts[parts.length - 2].replaceAll("[.,]$", "");
        }
        return last;
    }

    private static String escapePhrase(String s) {
        // Strip characters that break Lucene phrase queries (inside double quotes)
        return s.replace("\"", "").replace("\\", "");
    }

}
