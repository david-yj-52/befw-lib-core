package com.tsh.starter.befw.lib.core.interfaces;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String  code;
    private final String  message;
    private final T       data;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("200")
                .message("OK")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code("201")
                .message("Created")
                .data(data)
                .build();
    }

    public static ApiResponse<Void> noContent() {
        return ApiResponse.<Void>builder()
                .success(true)
                .code("204")
                .message("No Content")
                .build();
    }

    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .code(code)
                .message(message)
                .build();
    }
}
