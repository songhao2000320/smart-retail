package com.retail.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 统一 API 响应格式 {code, message, data}
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;

    private ApiResponse() {}

    private ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    // ==================== 成功响应 ====================

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "success", null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(200, message, data);
    }

    // ==================== 错误响应 ====================

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(400, message, null);
    }

    // ==================== 分页响应 ====================

    public static <T> ApiResponse<PageData<T>> page(java.util.List<T> records, long total, int page, int size) {
        PageData<T> pageData = new PageData<>();
        pageData.setRecords(records);
        pageData.setTotal(total);
        pageData.setPage(page);
        pageData.setSize(size);
        return success(pageData);
    }

    // ==================== Getter/Setter ====================

    public int getCode() { return code; }
    public void setCode(int code) { this.code = code; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    /**
     * 分页数据包装类
     */
    public static class PageData<T> {
        private java.util.List<T> records;
        private long total;
        private int page;
        private int size;

        public java.util.List<T> getRecords() { return records; }
        public void setRecords(java.util.List<T> records) { this.records = records; }

        public long getTotal() { return total; }
        public void setTotal(long total) { this.total = total; }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }

        public int getSize() { return size; }
        public void setSize(int size) { this.size = size; }
    }
}
