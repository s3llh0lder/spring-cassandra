package example.config;

import example.domain.model.PostByUser;
import example.domain.model.PostByUserKey;
import example.domain.model.User;
import example.domain.model.UserStats;
import example.domain.ports.input.CreatePostRequest;
import example.domain.ports.input.CreateUserRequest;
import example.domain.ports.input.UpdatePostRequest;

import java.time.OffsetDateTime;
import java.util.*;

public class TestDataBuilder {

    public static User createDefaultUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setName("Test User");
        user.setEmail("test@example.com");
        user.setCreatedAt(OffsetDateTime.now());
        user.setUpdatedAt(OffsetDateTime.now());
        return user;
    }

    public static CreateUserRequest createDefaultUserRequest() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Test User");
        request.setEmail("test" + System.currentTimeMillis() + "@example.com");
        return request;
    }

    public static PostByUser createDefaultPostByUser(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();
        PostByUserKey key = new PostByUserKey(userId, now, UUID.randomUUID());
        PostByUser post = new PostByUser();
        post.setKey(key);
        post.setTitle("Test Post");
        post.setContent("Test content");
        post.setStatus("DRAFT");
        post.setTags(List.of("test"));
        post.setCreatedAt(now);
        post.setUpdatedAt(now);
        return post;
    }

    public static CreatePostRequest createDefaultPostRequest() {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("Test Post " + System.currentTimeMillis());
        request.setContent("This is test content");
        request.setStatus("DRAFT");
        request.setTags(Arrays.asList("test", "example/integration"));
        return request;
    }

    public static UpdatePostRequest createDefaultUpdatePostRequest() {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle("Updated Test Post");
        request.setContent("Updated test content");
        request.setStatus("PUBLISHED");
        request.setTags(Arrays.asList("updated", "test"));
        return request;
    }

    public static UserStats createDefaultUserStats(UUID userId) {
        UserStats stats = new UserStats(userId);
        stats.setTotalPosts(0);
        stats.setPublishedPosts(0);
        stats.setDraftPosts(0);
        return stats;
    }
}