package com.greentrack.web;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public final class PageRequestUtil {
    private static final int MAX_SIZE = 100;
    private PageRequestUtil() {}

    public static PageRequest of(int page, int size) {
        return PageRequest.of(clampPage(page), clampSize(size));
    }
    public static PageRequest of(int page, int size, Sort sort) {
        return PageRequest.of(clampPage(page), clampSize(size), sort);
    }
    private static int clampPage(int page) { return Math.max(page, 0); }
    private static int clampSize(int size) { return Math.min(Math.max(size, 1), MAX_SIZE); }
}
