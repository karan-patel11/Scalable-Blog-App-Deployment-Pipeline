package com.blogapp.controller;

import com.blogapp.dto.PostDto;
import com.blogapp.model.Post.PostStatus;
import com.blogapp.service.PostService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void getPublishedPosts_ShouldReturn200() throws Exception {
        PostDto.Summary summary = PostDto.Summary.builder()
            .id(1L).title("Test Post").author("author").status(PostStatus.PUBLISHED).build();

        when(postService.getPublishedPosts(anyInt(), anyInt()))
            .thenReturn(new PageImpl<>(List.of(summary)));

        mockMvc.perform(get("/api/v1/posts/published"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].title").value("Test Post"));
    }

    @Test
    @WithMockUser
    void getPostById_ShouldReturn200() throws Exception {
        PostDto.Response response = PostDto.Response.builder()
            .id(1L).title("My Post").author("author").status(PostStatus.PUBLISHED).build();

        when(postService.getPostById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/posts/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("My Post"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void createPost_ShouldReturn201_WhenAuthenticated() throws Exception {
        PostDto.Request request = PostDto.Request.builder()
            .title("New Post")
            .content("New content here")
            .build();

        PostDto.Response response = PostDto.Response.builder()
            .id(1L).title("New Post").author("testuser").status(PostStatus.DRAFT).build();

        when(postService.createPost(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/posts")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("New Post"));
    }

    @Test
    void createPost_ShouldReturn403_WhenNotAuthenticated() throws Exception {
        PostDto.Request request = PostDto.Request.builder()
            .title("New Post").content("Content").build();

        mockMvc.perform(post("/api/v1/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isForbidden());
    }
}