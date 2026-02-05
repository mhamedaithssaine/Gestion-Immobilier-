package com.example.gestionimmobilier.dto.api.reponse;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiRetour<T>(
        boolean status,
        String message,
        LocalDateTime timestamp,
        T data
) {
    public static <T> ApiRetour<T> success(String message, T data) {
        return new ApiRetour<>(true, message, LocalDateTime.now(), data);
    }

    public static <T> ApiRetour<T> success(String message) {
        return new ApiRetour<>(true, message, LocalDateTime.now(), null);
    }
}
