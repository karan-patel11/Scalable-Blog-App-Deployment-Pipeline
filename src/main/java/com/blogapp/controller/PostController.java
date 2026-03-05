package com.blogapp.controller;

import com.blogapp.dto.PostDto;
import com.blogapp.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDto.Response> createPost(@Valid @RequestBody PostDto.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPost(request));
    }

    @GetMapping
    public ResponseEntity<Page<PostDto.Summary>> getAllPosts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "createdAt") String sortBy
    ) {
        return ResponseEntity.ok(postService.getAllPosts(page, size, sortBy));
    }

    @GetMapping("/published")
    public ResponseEntity<Page<PostDto.Summary>> getPublishedPosts(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(postService.getPublishedPosts(page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto.Response> getPostById(@PathVariable Long id) {
        return ResponseEntity.ok(postService.getPostById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PostDto.Response> updatePost(
        @PathVariable Long id,
        @Valid @RequestBody PostDto.Request request
    ) {
        return ResponseEntity.ok(postService.updatePost(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PostDto.Summary>> searchPosts(
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(postService.searchPosts(keyword, page, size));
    }

    @GetMapping("/recent")
    public ResponseEntity<List<PostDto.Summary>> getRecentPosts() {
        return ResponseEntity.ok(postService.getRecentPosts());
    }
}
