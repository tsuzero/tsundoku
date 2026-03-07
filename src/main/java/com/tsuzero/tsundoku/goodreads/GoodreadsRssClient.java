package com.tsuzero.tsundoku.goodreads;

import com.tsuzero.tsundoku.book.BookService.BookData;
import org.apache.commons.text.StringEscapeUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class GoodreadsRssClient {

    private static final Logger log = LoggerFactory.getLogger(GoodreadsRssClient.class);

    // Matches the book ID embedded in Goodreads image URLs: .../books/<timestamp>l/<bookId>._SY...
    private static final Pattern BOOK_ID_FROM_IMG = Pattern.compile("/(\\d+)\\._S[XY]");

    // RFC 1123 / RSS date format: "Thu, 19 Feb 2026 09:55:04 -0800"
    private static final DateTimeFormatter RSS_DATE =
            DateTimeFormatter.ofPattern("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);

    private final GoodreadsProperties properties;

    public GoodreadsRssClient(GoodreadsProperties properties) {
        this.properties = properties;
    }

    public List<BookData> fetchToReadShelf() throws Exception {
        String feedUrl = "https://www.goodreads.com/review/list_rss/%s?shelf=%s"
                .formatted(properties.getUserId(), properties.getShelf());

        log.info("Fetching Goodreads RSS from {}", feedUrl);

        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(URI.create(feedUrl).toURL());

        Element channel = doc.getRootElement().getChild("channel");
        if (channel == null) throw new IllegalStateException("RSS channel element not found");

        List<Element> items = channel.getChildren("item");
        List<BookData> books = new ArrayList<>();

        for (Element item : items) {
            try {
                books.add(parseItem(item));
            } catch (Exception e) {
                log.warn("Failed to parse RSS item: {}", item.getChildTextTrim("title"), e);
            }
        }

        log.info("Fetched {} books from Goodreads RSS", books.size());
        return books;
    }

    private BookData parseItem(Element item) {
        // book_id is always present as a plain element
        String goodreadsId = text(item, "book_id");

        // fallback: <book id="123"> attribute
        if (goodreadsId == null) {
            Element bookEl = item.getChild("book");
            if (bookEl != null) goodreadsId = bookEl.getAttributeValue("id");
        }

        // fallback: extract from image URL
        if (goodreadsId == null) {
            String img = coalesce(text(item, "book_large_image_url"), text(item, "book_image_url"));
            if (img != null) {
                Matcher m = BOOK_ID_FROM_IMG.matcher(img);
                if (m.find()) goodreadsId = m.group(1);
            }
        }

        String isbn   = sanitizeIsbn(text(item, "isbn"));
        String isbn13 = sanitizeIsbn(text(item, "isbn13"));
        String title  = text(item, "title");
        String authorName = text(item, "author_name");

        // Prefer large image, then medium, then small
        String coverUrl = coalesce(
                text(item, "book_large_image_url"),
                text(item, "book_medium_image_url"),
                text(item, "book_image_url")
        );

        // Use book_description for a cleaner description (strip HTML)
        String rawDesc = coalesce(text(item, "book_description"), text(item, "description"));
        String description = stripHtml(rawDesc);

        BigDecimal avgRating  = parseBigDecimal(text(item, "average_rating"));
        Integer yearPublished = parseInteger(text(item, "book_published"));

        OffsetDateTime dateAdded = parseRssDate(text(item, "user_date_added"));
        if (dateAdded == null) dateAdded = parseRssDate(text(item, "pubDate"));

        return new BookData(goodreadsId, isbn, isbn13, title, authorName,
                coverUrl, description, avgRating, yearPublished, dateAdded);
    }

    /** Get trimmed text of a direct child element, or null if missing/blank. */
    private static String text(Element parent, String childName) {
        Element child = parent.getChild(childName);
        if (child == null) return null;
        String t = child.getTextTrim();
        return t.isBlank() ? null : t;
    }

    @SafeVarargs
    private static <T> T coalesce(T... values) {
        for (T v : values) if (v != null) return v;
        return null;
    }

    private static String sanitizeIsbn(String raw) {
        if (raw == null || raw.equalsIgnoreCase("null")) return null;
        raw = raw.trim();
        return raw.isBlank() ? null : raw;
    }

    private static String stripHtml(String html) {
        if (html == null) return null;
        String unescaped = StringEscapeUtils.unescapeHtml4(html);
        return unescaped.replaceAll("<[^>]+>", "").trim();
    }

    private static BigDecimal parseBigDecimal(String s) {
        if (s == null) return null;
        try { return new BigDecimal(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private static Integer parseInteger(String s) {
        if (s == null) return null;
        try { return Integer.parseInt(s.trim()); } catch (NumberFormatException e) { return null; }
    }

    private static OffsetDateTime parseRssDate(String s) {
        if (s == null) return null;
        try {
            return OffsetDateTime.parse(s.trim(), RSS_DATE).withOffsetSameInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
