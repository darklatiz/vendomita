package tech.terabyte.labs.vendomita.model;

import java.util.Map;

public record ApiResponse<T>(
  String status,
  String message,
  T data,
  Map<String, ?> meta
) {

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data, null);
    }

    public static <T> ApiResponse<T> success(String message, T data, Map<String, ?> meta) {
        return new ApiResponse<>("success", message, data, meta);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>("error", message, null, null);
    }

    public static <T> ApiResponse<T> error(String message, Map<String, ?> meta) {
        return new ApiResponse<>("error", message, null, meta);
    }



}
