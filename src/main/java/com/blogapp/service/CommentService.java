package com.blogapp.service;

import com.blogapp.dto.CommentDto;
import com.blogapp.exception.ResourceNotFoundException;
import com.blogapp.model.Comment;
import com.blogapp.model.Post;
import com.blogapp.repository.CommentRepository;
import com.blogapp.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentDto.Response addComment(Long postId, CommentDto.Request request) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Post", "id", postId));

        Comment comment = Comment.builder()
            .body(request.getBody())
            .authorName(request.getAuthorName())
            .authorEmail(request.getAuthorEmail())
            .post(post)
            .build();

        return toResponse(commentRepository.save(comment));
    }

    @Transactional(readOnly = true)
    public List<CommentDto.Response> getCommentsByPost(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new ResourceNotFoundException("Post", "id", postId);
        }
        return commentRepository.findByPostId(postId)
            .stream().map(this::toResponse).toList();
    }

    public void deleteComment(Long postId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
            .orElseThrow(() -> new ResourceNotFoundException("Comment", "id", commentId));

        if (!comment.getPost().getId().equals(postId)) {
            throw new IllegalArgumentException("Comment does not belong to post " + postId);
        }

        commentRepository.delete(comment);
    }

    private CommentDto.Response toResponse(Comment comment) {
        return CommentDto.Response.builder()
            .id(comment.getId())
            .body(comment.getBody())
            .authorName(comment.getAuthorName())
            .authorEmail(comment.getAuthorEmail())
            .createdAt(comment.getCreatedAt())
            .postId(comment.getPost().getId())
            .build();
    }
}
