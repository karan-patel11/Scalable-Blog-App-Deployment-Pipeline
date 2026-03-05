package com.blogapp.service;

import com.blogapp.dto.PostDto;
import com.blogapp.exception.ResourceNotFoundException;
import com.blogapp.model.Category;
import com.blogapp.model.Post;
import com.blogapp.model.Post.PostStatus;
import com.blogapp.repository.CategoryRepository;
import com.blogapp.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final CategoryRepository categoryRepository;

    public PostDto.Response createPost(PostDto.Request request) {
        String author = SecurityContextHolder.getContext().getAuthentication().getName();

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
        }

        Post post = Post.builder()
            .title(request.getTitle())
            .content(request.getContent())
            .summary(request.getSummary())
            .author(author)
            .status(request.getStatus() != null ? request.getStatus() : PostStatus.DRAFT)
            .category(category)
            .build();

        Post saved = postRepository.save(post);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<PostDto.Summary> getAllPosts(int page, int size, String sortBy) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        return postRepository.findAll(pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public Page<PostDto.Summary> getPublishedPosts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.findByStatus(PostStatus.PUBLISHED, pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public PostDto.Response getPostById(Long id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        return toResponse(post);
    }

    public PostDto.Response updatePost(Long id, PostDto.Request request) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setSummary(request.getSummary());
        if (request.getStatus() != null) post.setStatus(request.getStatus());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "id", request.getCategoryId()));
            post.setCategory(category);
        }

        return toResponse(postRepository.save(post));
    }

    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", id));
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public Page<PostDto.Summary> searchPosts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return postRepository.searchByKeyword(keyword, pageable).map(this::toSummary);
    }

    @Transactional(readOnly = true)
    public List<PostDto.Summary> getRecentPosts() {
        return postRepository.findTop5ByStatusOrderByCreatedAtDesc(PostStatus.PUBLISHED)
            .stream().map(this::toSummary).toList();
    }

    // --- Mapping helpers ---
    private PostDto.Response toResponse(Post post) {
        return PostDto.Response.builder()
            .id(post.getId())
            .title(post.getTitle())
            .content(post.getContent())
            .summary(post.getSummary())
            .author(post.getAuthor())
            .status(post.getStatus())
            .createdAt(post.getCreatedAt())
            .updatedAt(post.getUpdatedAt())
            .categoryName(post.getCategory() != null ? post.getCategory().getName() : null)
            .commentCount(post.getComments() != null ? post.getComments().size() : 0)
            .build();
    }

    private PostDto.Summary toSummary(Post post) {
        return PostDto.Summary.builder()
            .id(post.getId())
            .title(post.getTitle())
            .summary(post.getSummary())
            .author(post.getAuthor())
            .status(post.getStatus())
            .createdAt(post.getCreatedAt())
            .categoryName(post.getCategory() != null ? post.getCategory().getName() : null)
            .commentCount(post.getComments() != null ? post.getComments().size() : 0)
            .build();
    }
}
