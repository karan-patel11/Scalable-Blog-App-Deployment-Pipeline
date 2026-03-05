package com.blogapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.time.LocalDateTime;

public class CommentDto {

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "Comment body is required")
        private String body;

        @NotBlank(message = "Author name is required")
        private String authorName;

        @NotBlank(message = "Author email is required")
        @Email(message = "Invalid email format")
        private String authorEmail;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String body;
        private String authorName;
        private String authorEmail;
        private LocalDateTime createdAt;
        private Long postId;
    }
}
