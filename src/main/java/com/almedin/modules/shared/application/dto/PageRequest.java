package com.almedin.modules.shared.application.dto;

public record PageRequest(int page, int size) {

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_SIZE = 10;
    public static final int MAX_SIZE = 50;

    public PageRequest {
        if (page < 0) page = DEFAULT_PAGE;
        if (size <= 0) size = DEFAULT_SIZE;
        if (size > MAX_SIZE) size = MAX_SIZE;
    }

    public int offset() {
        return page * size;
    }
}