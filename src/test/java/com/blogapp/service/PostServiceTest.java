package com.blogapp.service;

import com.blogapp.dto.PostDto;
import com.blogapp.exception.ResourceNotFoundException;
import com.blogapp.model.Post;
import com.blogapp.repository.CategoryRepository;
import com.blogapp.repository.PostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private PostService postService;

    @BeforeEach
    void setUp() {
        // Mock authenticated user
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken("testuser", "password")
        );
    }

    @Test
    void createPost_ShouldReturnSavedPost() {
        PostDto.Request request = PostDto.Request.builder()
            .title("Test Post")
            .content("Test content")
            .build();

        Post savedPost = Post.builder()
            .id(1L)
            .title("Test Post")
            .content("Test content")
            .author("testuser")
            .status(Post.PostStatus.DRAFT)
            .build();

        when(postRepository.save(any(Post.class))).thenReturn(savedPost);

        PostDto.Response response = postService.createPost(request);

        assertThat(response.getTitle()).isEqualTo("Test Post");
        assertThat(response.getAuthor()).isEqualTo("testuser");
        assertThat(response.getStatus()).isEqualTo(Post.PostStatus.DRAFT);
        verify(postRepository, times(1)).save(any(Post.class));
    }

    @Test
    void getPostById_ShouldReturnPost_WhenExists() {
        Post post = Post.builder()
            .id(1L)
            .title("Existing Post")
            .content("Content")
            .author("author")
            .status(Post.PostStatus.PUBLISHED)
            .build();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        PostDto.Response response = postService.getPostById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Existing Post");
    }

    @Test
    void getPostById_ShouldThrowException_WhenNotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.getPostById(99L))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessageContaining("Post");
    }

    @Test
    void deletePost_ShouldCallRepository_WhenPostExists() {
        Post post = Post.builder().id(1L).title("To Delete").author("testuser").build();
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));

        postService.deletePost(1L);

        verify(postRepository, times(1)).delete(post);
    }

    @Test
    void getAllPosts_ShouldReturnPagedResults() {
        Post post = Post.builder()
            .id(1L).title("Post 1").author("author").status(Post.PostStatus.PUBLISHED).build();

        when(postRepository.findAll(any(Pageable.class)))
            .thenReturn(new PageImpl<>(List.of(post)));

        Page<PostDto.Summary> results = postService.getAllPosts(0, 10, "createdAt");

        assertThat(results.getTotalElements()).isEqualTo(1);
        assertThat(results.getContent().get(0).getTitle()).isEqualTo("Post 1");
    }
}
