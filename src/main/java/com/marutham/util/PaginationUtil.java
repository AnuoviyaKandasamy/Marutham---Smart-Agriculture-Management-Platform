package com.marutham.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaginationUtil {

    public static class PaginationConfig {
        public int page;
        public int size;
        public int offset;
        public int limit;
        public static final int DEFAULT_SIZE = 10;
        public static final int MAX_SIZE = 100;
        public static final int MIN_PAGE = 1;

        public PaginationConfig(int page, int size) {
            this.page = Math.max(page, MIN_PAGE);
            this.size = Math.min(Math.max(size, 1), MAX_SIZE);
            this.offset = (this.page - 1) * this.size;
            this.limit = this.size;
        }

        public PaginationConfig(String pageStr, String sizeStr) {
            int p = MIN_PAGE, s = DEFAULT_SIZE;
            try {
                if (pageStr != null && !pageStr.isEmpty()) {
                    p = Integer.parseInt(pageStr);
                }
                if (sizeStr != null && !sizeStr.isEmpty()) {
                    s = Integer.parseInt(sizeStr);
                }
            } catch (NumberFormatException e) {

            }
            this.page = Math.max(p, MIN_PAGE);
            this.size = Math.min(Math.max(s, 1), MAX_SIZE);
            this.offset = (this.page - 1) * this.size;
            this.limit = this.size;
        }

        public int getOffset() {
            return offset;
        }

        public int getLimit() {
            return limit;
        }
    }


    public static class PageResponse<T> {
        public List<T> data;
        public long total;
        public int page;
        public int size;
        public int totalPages;
        public boolean hasNext;
        public boolean hasPrev;

        public PageResponse(List<T> data, long total, int page, int size) {
            this.data = data;
            this.total = total;
            this.page = page;
            this.size = size;
            this.totalPages = (int) Math.ceil((double) total / size);
            this.hasNext = page < this.totalPages;
            this.hasPrev = page > 1;
        }

        public Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("data", data);
            map.put("total", total);
            map.put("page", page);
            map.put("size", size);
            map.put("totalPages", totalPages);
            map.put("hasNext", hasNext);
            map.put("hasPrev", hasPrev);
            return map;
        }
    }


    public static boolean isValidPage(int page) {
        return page >= PaginationConfig.MIN_PAGE;
    }

    public static boolean isValidSize(int size) {
        return size >= 1 && size <= PaginationConfig.MAX_SIZE;
    }


    public static <T> Map<String, Object> createPageResponse(List<T> data, long total, int page, int size) {
        return new PageResponse<>(data, total, page, size).toMap();
    }
}
