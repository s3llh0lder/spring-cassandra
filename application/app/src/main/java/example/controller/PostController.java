package example.controller;

import example.exception.PostNotFoundException;
import example.exception.UserNotFoundException;
import example.model.PostById;
import example.model.PostByUser;
import example.request.CreatePostRequest;
import example.request.UpdatePostRequest;
import example.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Validated
public class PostController {

    @Autowired
    private PostService postService;

    @PostMapping("/users/{userId}/posts")
    public ResponseEntity<PostByUser> createPost(
            @PathVariable UUID userId,
            @Valid @RequestBody CreatePostRequest request) {
        try {
            PostByUser post = postService.createPost(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(post);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<List<PostByUser>> getUserPosts(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "20") int limit,
            @RequestParam(required = false) String status) {

        List<PostByUser> posts;
        if (status != null) {
            posts = postService.getUserPostsByStatus(userId, status);
        } else {
            posts = postService.getUserPosts(userId, limit);
        }
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostById> getPost(@PathVariable UUID postId) {
        Optional<PostById> post = postService.getPostById(postId);
        return post.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/users/{userId}/posts/{postId}")
    public ResponseEntity<PostByUser> updatePost(
            @PathVariable UUID userId,
            @PathVariable UUID postId,
            @Valid @RequestBody UpdatePostRequest request) {
        try {
            PostByUser post = postService.updatePost(userId, postId, request);
            return ResponseEntity.ok(post);
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/users/{userId}/posts/{postId}/publish")
    public ResponseEntity<PostByUser> publishPost(
            @PathVariable UUID userId,
            @PathVariable UUID postId) {
        try {
            PostByUser post = postService.publishPost(userId, postId);
            return ResponseEntity.ok(post);
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/users/{userId}/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable UUID userId,
            @PathVariable UUID postId) {
        try {
            postService.deletePost(userId, postId);
            return ResponseEntity.noContent().build();
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}