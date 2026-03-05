package com.blogapp.dto;

import com.blogapp.model.Post.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

public class PostDto {

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class Request {
        @NotBlank(message = "Title is required")
        @Size(min = 3, max = 200)
        private String title;

        @NotBlank(message = "Content is required")
        private String content;

        @Size(max = 500)
        private String summary;

        private PostStatus status;
        private Long categoryId;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String content;
        private String summary;
        private String author;
        private PostStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private String categoryName;
        private int commentCount;
    }

    @Getter @Setter
    @NoArgsConstructor @AllArgsConstructor
    @Builder
    public static class Summary {
        private Long id;
        private String title;
        private String summary;
        private String author;
        private PostStatus status;
        private LocalDateTime createdAt;
        private String categoryName;
        private int commentCount;
    }
}
