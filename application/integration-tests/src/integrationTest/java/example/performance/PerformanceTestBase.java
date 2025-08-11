package example.performance;

import example.integration.BaseCassandraIntegrationTest;
import example.domain.model.PostByUser;
import example.domain.model.User;
import example.domain.ports.input.CreatePostRequest;
import example.domain.ports.input.CreateUserRequest;
import example.domain.services.PostService;
import example.domain.services.UserService;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
abstract class PerformanceTestBase extends BaseCassandraIntegrationTest {

    protected static final int SMALL_DATASET = 100;
    protected static final int MEDIUM_DATASET = 1000;
    protected static final int LARGE_DATASET = 10000;

    @Autowired
    protected UserService userService;

    @Autowired
    protected PostService postService;

    protected void measureExecutionTime(String operationName, Runnable operation) {
        long startTime = System.currentTimeMillis();
        operation.run();
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        System.out.printf("%s completed in %d ms%n", operationName, duration);

        // Assert reasonable performance thresholds
        switch (operationName) {
            case "Create User":
                assertThat(duration).isLessThan(1000); // Should complete within 1 second
                break;
            case "Create Post":
                assertThat(duration).isLessThan(2000); // Should complete within 2 seconds
                break;
            case "Get User Posts":
                assertThat(duration).isLessThan(3000); // Should complete within 3 seconds
                break;
        }
    }

    protected List<User> createTestUsers(int count) {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CreateUserRequest request = new CreateUserRequest();
            request.setName("User " + i);
            request.setEmail("user" + i + "@example.com");
            users.add(userService.createUser(request));
        }
        return users;
    }

    protected List<PostByUser> createTestPosts(UUID userId, int count) {
        List<PostByUser> posts = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            CreatePostRequest request = new CreatePostRequest();
            request.setTitle("Post " + i);
            request.setContent("Content for post " + i);
            request.setStatus(i % 2 == 0 ? "PUBLISHED" : "DRAFT");
            posts.add(postService.createPost(userId, request));
        }
        return posts;
    }
}