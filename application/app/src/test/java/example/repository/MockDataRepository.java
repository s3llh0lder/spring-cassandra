package example.repository;

import example.domain.model.*;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Profile("test")
public class MockDataRepository {

    private final Map<UUID, User> users = new ConcurrentHashMap<>();
    private final Map<String, UserByEmail> usersByEmail = new ConcurrentHashMap<>();
    private final Map<UUID, UserStats> userStats = new ConcurrentHashMap<>();
    private final Map<PostByUserKey, PostByUser> postsByUser = new ConcurrentHashMap<>();
    private final Map<UUID, PostById> postsById = new ConcurrentHashMap<>();

    public void clear() {
        users.clear();
        usersByEmail.clear();
        userStats.clear();
        postsByUser.clear();
        postsById.clear();
    }

    public User saveUser(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }
        if (user.getCreatedAt() == null) {
            user.setCreatedAt(OffsetDateTime.now());
        }
        user.setUpdatedAt(OffsetDateTime.now());
        users.put(user.getId(), user);
        return user;
    }

    public UserByEmail saveUserByEmail(UserByEmail userByEmail) {
        usersByEmail.put(userByEmail.getEmail(), userByEmail);
        return userByEmail;
    }

    public PostByUser savePostByUser(PostByUser post) {
        if (post.getKey().getPostId() == null) {
            post.getKey().setPostId(UUID.randomUUID());
        }
        if (post.getCreatedAt() == null) {
            post.setCreatedAt(OffsetDateTime.now());
        }
        post.setUpdatedAt(OffsetDateTime.now());
        postsByUser.put(post.getKey(), post);
        return post;
    }

    public PostById savePostById(PostById post) {
        if (post.getPostId() == null) {
            post.setPostId(UUID.randomUUID());
        }
        if (post.getCreatedAt() == null) {
            post.setCreatedAt(OffsetDateTime.now());
        }
        post.setUpdatedAt(OffsetDateTime.now());
        postsById.put(post.getPostId(), post);
        return post;
    }

    public Optional<User> findUserById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }

    public Optional<UserByEmail> findUserByEmail(String email) {
        return Optional.ofNullable(usersByEmail.get(email));
    }

    public List<PostByUser> findPostsByUserId(UUID userId) {
        return postsByUser.values().stream()
                .filter(post -> post.getUserId().equals(userId))
                .sorted((p1, p2) -> p2.getCreatedAt().compareTo(p1.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public void deleteUser(UUID userId) {
        User user = users.remove(userId);
        if (user != null) {
            usersByEmail.remove(user.getEmail());
        }
        userStats.remove(userId);
    }

    public void deletePost(PostByUserKey key) {
        PostByUser post = postsByUser.remove(key);
        if (post != null) {
            postsById.remove(post.getPostId());
        }
    }
}
