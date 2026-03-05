package com.blogapp.controller;

import com.blogapp.dto.CommentDto;
import com.blogapp.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentDto.Response> addComment(
        @PathVariable Long postId,
        @Valid @RequestBody CommentDto.Request request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(commentService.addComment(postId, request));
    }

    @GetMapping
    public ResponseEntity<List<CommentDto.Response>> getComments(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    @DeleteMapping("/{commentId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteComment(
        @PathVariable Long postId,
        @PathVariable Long commentId
    ) {
        commentService.deleteComment(postId, commentId);
        return ResponseEntity.noContent().build();
    }
}
