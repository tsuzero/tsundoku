package com.tsuzero.tsundoku.web;

import com.tsuzero.tsundoku.book.BookDto;

import java.math.BigDecimal;

record BookViewModel(BookDto book, String stars) {

    static BookViewModel of(BookDto book) {
        return new BookViewModel(book, computeStars(book.averageRating()));
    }

    private static String computeStars(BigDecimal r) {
        if (r == null) return "";
        int full = r.intValue();
        boolean half = r.doubleValue() - full >= 0.5;
        return "★".repeat(full) + (half ? "½" : "") + "☆".repeat(5 - full - (half ? 1 : 0));
    }
}
