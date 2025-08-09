package example.performance;

import example.config.ConcurrentTestUtils;
import example.config.SimpleConcurrentTestUtils;
import example.integration.BaseCassandraIntegrationTest;
import example.model.PostByUser;
import example.model.User;
import example.request.CreatePostRequest;
import example.request.CreateUserRequest;
import example.request.UserWithStats;
import example.service.PostService;
import example.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ConcurrentOperationsIntegrationTest extends BaseCassandraIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private PostService postService;

    @Test
    void concurrentUserCreation_Success() throws InterruptedException {
        // Given
        AtomicInteger counter = new AtomicInteger(0);

        // When
        List<User> createdUsers = ConcurrentTestUtils.runConcurrentOperationsWithResults(
                5, // 5 threads
                10, // 10 operations per thread
                () -> {
                    int id = counter.incrementAndGet();
                    CreateUserRequest request = new CreateUserRequest();
                    request.setName("Concurrent User " + id);
                    request.setEmail("concurrent" + id + "@example.com");
                    return userService.createUser(request);
                }
        );

        // Then
        assertThat(createdUsers).hasSize(50); // 5 threads * 10 operations

        // Verify all users have unique emails
        Set<String> emails = createdUsers.stream()
                .map(User::getEmail)
                .collect(Collectors.toSet());
        assertThat(emails).hasSize(50);
    }

    @Test
    void concurrentPostCreation_Success() throws InterruptedException {
        // Given
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setName("Post Creator");
        userRequest.setEmail("postcreator@example.com");
        User user = userService.createUser(userRequest);

        AtomicInteger counter = new AtomicInteger(0);

        // When
        List<PostByUser> createdPosts = SimpleConcurrentTestUtils.runConcurrentOperationsWithResults(
                3, // 3 threads
                5, // 5 operations per thread
                () -> {
                    int id = counter.incrementAndGet();
                    CreatePostRequest request = new CreatePostRequest();
                    request.setTitle("Concurrent Post " + id);
                    request.setContent("Content " + id);
                    request.setStatus("DRAFT");
                    return postService.createPost(user.getId(), request);
                }
        );

        // Then
        assertEquals(15, createdPosts.size(), "Should have created 15 posts"); // 3 threads * 5 operations

        // Verify all posts belong to the same user
        boolean allPostsBelongToUser = createdPosts.stream()
                .allMatch(post -> post.getUserId().equals(user.getId()));
        assertTrue(allPostsBelongToUser, "All posts should belong to the same user");

        // Allow some time for any remaining async operations to complete
        Thread.sleep(100);

        // Verify user stats are correctly updated
        UserWithStats userWithStats = userService.getUserWithStats(user.getId());
        assertEquals(15, userWithStats.getStats().getTotalPosts(), "User stats should show 15 total posts");
        assertEquals(15, userWithStats.getStats().getDraftPosts(), "User stats should show 15 draft posts");
    }

    @Test
    void concurrentReadOperations_Success() throws InterruptedException {
        // Given
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setName("Read Test User");
        userRequest.setEmail("readtest@example.com");
        User user = userService.createUser(userRequest);

        // Create some posts
        for (int i = 0; i < 10; i++) {
            CreatePostRequest postRequest = new CreatePostRequest();
            postRequest.setTitle("Post " + i);
            postRequest.setContent("Content " + i);
            postService.createPost(user.getId(), postRequest);
        }

        // When - Multiple threads reading user posts concurrently
        ConcurrentTestUtils.runConcurrentOperations(
                10, // 10 threads
                20, // 20 reads per thread
                () -> {
                    List<PostByUser> posts = postService.getUserPosts(user.getId(), 5);
                    assertThat(posts).hasSize(5);

                    Optional<User> foundUser = userService.getUserById(user.getId());
                    assertThat(foundUser).isPresent();
                }
        );

        // Then - No exceptions should be thrown and operations should complete successfully
        // The test passes if no exceptions are thrown during concurrent reads
    }
}