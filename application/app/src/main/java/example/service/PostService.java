package example.service;

import example.exception.PostNotFoundException;
import example.exception.UserNotFoundException;
import example.model.*;
import example.repository.*;
import example.request.CreatePostRequest;
import example.request.UpdatePostRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PostService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostByUserRepository postByUserRepository;

    @Autowired
    private PostByIdRepository postByIdRepository;

    @Autowired
    private PostByUserStatusRepository postByUserStatusRepository;

    @Autowired
    private UserStatsRepository userStatsRepository;

    @Transactional
    public PostByUser createPost(UUID userId, CreatePostRequest request) {
        // Verify user exists
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            throw new UserNotFoundException("User not found: " + userId);
        }

        // Create post
        PostByUser postByUser = new PostByUser(userId, request.getTitle(), request.getContent());
        if (request.getTags() != null) {
            postByUser.setTags(new HashSet<>(request.getTags()));
        }
        if (request.getStatus() != null) {
            postByUser.setStatus(request.getStatus());
        }

        // Save to posts_by_user table
        PostByUser savedPost = postByUserRepository.save(postByUser);

        // Save to posts_by_id table for direct lookups
        PostById postById = PostById.fromPostByUser(savedPost);
        postByIdRepository.save(postById);

        // Save to posts_by_user_status table for efficient status queries
        PostByUserStatus postByStatus = PostByUserStatus.fromPostByUser(savedPost);
        postByUserStatusRepository.save(postByStatus);

        updateUserStats(userId, savedPost.getStatus(), true);

        return savedPost;
    }

    @Transactional
    public PostByUser updatePost(UUID userId, UUID postId, UpdatePostRequest request) {
        // Find the post by scanning posts_by_user (not ideal, but necessary for updates)
        List<PostByUser> userPosts = postByUserRepository.findByUserId(userId);
        PostByUser existingPost = userPosts.stream()
                .filter(p -> p.getPostId().equals(postId))
                .findFirst()
                .orElseThrow(() -> new PostNotFoundException("Post not found: " + postId));

        String oldStatus = existingPost.getStatus();

        // Update fields
        if (request.getTitle() != null) {
            existingPost.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            existingPost.setContent(request.getContent());
        }
        if (request.getStatus() != null) {
            existingPost.setStatus(request.getStatus());
        }
        if (request.getTags() != null) {
            existingPost.setTags(new HashSet<>(request.getTags()));
        }

        // Save to posts_by_user table
        PostByUser updatedPost = postByUserRepository.save(existingPost);

        // Save to posts_by_id table
        PostById postById = PostById.fromPostByUser(updatedPost);
        postByIdRepository.save(postById);

        // Handle status table updates
        if (!oldStatus.equals(updatedPost.getStatus())) {
            // Delete old status entry
            PostByUserStatusKey oldStatusKey = new PostByUserStatusKey(
                    userId, oldStatus, existingPost.getCreatedAt(), postId);
            postByUserStatusRepository.deleteById(oldStatusKey);

            // Create new status entry
            PostByUserStatus newStatusPost = PostByUserStatus.fromPostByUser(updatedPost);
            postByUserStatusRepository.save(newStatusPost);

            // Update stats
            updateUserStats(userId, oldStatus, false);
            updateUserStats(userId, updatedPost.getStatus(), true);


        } else {
            // Status didn't change, just update existing entry
            PostByUserStatus statusPost = PostByUserStatus.fromPostByUser(updatedPost);
            postByUserStatusRepository.save(statusPost);
        }

        return updatedPost;
    }

    @Transactional
    public void deletePost(UUID userId, UUID postId) {
        // Find the post to get creation time for composite key
        List<PostByUser> userPosts = postByUserRepository.findByUserId(userId);
        PostByUser postToDelete = userPosts.stream()
                .filter(p -> p.getPostId().equals(postId))
                .findFirst()
                .orElseThrow(() -> new PostNotFoundException("Post not found: " + postId));

        // Delete from posts_by_user table
        postByUserRepository.deleteById(postToDelete.getKey());

        // Delete from posts_by_id table
        postByIdRepository.deleteById(postId);

        // Delete from posts_by_user_status table using correct key
        PostByUserStatusKey statusKey = new PostByUserStatusKey(
                userId, postToDelete.getStatus(), postToDelete.getCreatedAt(), postId);
        postByUserStatusRepository.deleteById(statusKey);

        // Update user stats
        updateUserStats(userId, postToDelete.getStatus(), false);

    }

    public List<PostByUser> getUserPosts(UUID userId, int limit) {
        if (limit <= 0) {
            return postByUserRepository.findByUserId(userId);
        }
        return postByUserRepository.findByUserIdWithLimit(userId, limit);
    }

    public List<PostByUser> getUserPostsByStatus(UUID userId, String status) {
        // Get posts from status table and convert to PostByUser objects
        List<PostByUserStatus> statusPosts = postByUserStatusRepository.findByUserIdAndStatus(userId, status);

        return statusPosts.stream()
                .map(this::convertToPostByUser)
                .collect(Collectors.toList());
    }

    public Optional<PostById> getPostById(UUID postId) {
        return postByIdRepository.findById(postId);
    }

    @Transactional
    public PostByUser publishPost(UUID userId, UUID postId) {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setStatus("PUBLISHED");
        return updatePost(userId, postId, request);
    }

    // TODO: get rid of synchronized method annotation
    private synchronized void updateUserStats(UUID userId, String status, boolean increment) {
        UserStats stats = userStatsRepository.findById(userId)
                .orElse(new UserStats(userId));

        if (increment) {
            stats.incrementPost(status);
        } else {
            stats.decrementPost(status);
        }

        userStatsRepository.save(stats);
    }

    private PostByUser convertToPostByUser(PostByUserStatus statusPost) {
        PostByUser postByUser = new PostByUser();

        // Create PostByUserKey from PostByUserStatusKey
        PostByUserKey key = new PostByUserKey(
                statusPost.getUserId(),
                statusPost.getCreatedAt(),
                statusPost.getPostId()
        );

        postByUser.setKey(key);
        postByUser.setTitle(statusPost.getTitle());
        postByUser.setContent(statusPost.getContent());
        postByUser.setTags(statusPost.getTags());
        postByUser.setStatus(statusPost.getStatus());
        postByUser.setCreatedAt(statusPost.getCreatedAt());
        postByUser.setUpdatedAt(statusPost.getUpdatedAt());

        return postByUser;
    }
}