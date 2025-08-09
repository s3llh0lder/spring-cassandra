package example.integration;

import example.model.PostById;
import example.model.PostByUser;
import example.model.User;
import example.request.CreatePostRequest;
import example.request.CreateUserRequest;
import example.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PostControllerIntegrationTest extends BaseCassandraIntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(PostControllerIntegrationTest.class);

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserService userService;

    private User testUser;

    @BeforeEach
    void set() {
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setName("Post Test User");
        userRequest.setEmail("post.test." + System.currentTimeMillis() + "@example.com");
        testUser = userService.createUser(userRequest);
    }

    @Test
    void createPost_Success() {
        // Given
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle("API Test Post");
        request.setContent("This is an API test post");
        request.setStatus("DRAFT");
        request.setTags(Arrays.asList("api", "test"));

        // When
        ResponseEntity<PostByUser> response = restTemplate.postForEntity(
                "/api/users/" + testUser.getId() + "/posts", request, PostByUser.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("API Test Post");
        assertThat(response.getBody().getContent()).isEqualTo("This is an API test post");
        assertThat(response.getBody().getStatus()).isEqualTo("DRAFT");
    }

    @Test
    void getUserPosts_Success() {
        // Given
        CreatePostRequest request1 = new CreatePostRequest();
        request1.setTitle("Post 1");
        request1.setContent("Content 1");

        CreatePostRequest request2 = new CreatePostRequest();
        request2.setTitle("Post 2");
        request2.setContent("Content 2");

        restTemplate.postForEntity("/api/users/" + testUser.getId() + "/posts", request1, PostByUser.class);
        restTemplate.postForEntity("/api/users/" + testUser.getId() + "/posts", request2, PostByUser.class);

        // When
        ResponseEntity<PostByUser[]> response = restTemplate.getForEntity(
                "/api/users/" + testUser.getId() + "/posts", PostByUser[].class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
    }

    @Test
    void getPost_Success() {
        // Given
        CreatePostRequest createRequest = new CreatePostRequest();
        createRequest.setTitle("Get Test Post");
        createRequest.setContent("Content");
        ResponseEntity<PostByUser> createResponse = restTemplate.postForEntity(
                "/api/users/" + testUser.getId() + "/posts", createRequest, PostByUser.class);
        UUID postId = createResponse.getBody().getPostId();

        // When
        ResponseEntity<PostById> response = restTemplate.getForEntity("/api/posts/" + postId, PostById.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPostId()).isEqualTo(postId);
        assertThat(response.getBody().getTitle()).isEqualTo("Get Test Post");
    }

    @Test
    void publishPost_Success() {
        // Given
        CreatePostRequest createRequest = new CreatePostRequest();
        createRequest.setTitle("Draft Post");
        createRequest.setContent("Content");
        createRequest.setStatus("DRAFT");
        ResponseEntity<PostByUser> createResponse = restTemplate.postForEntity(
                "/api/users/" + testUser.getId() + "/posts", createRequest, PostByUser.class);
        UUID postId = createResponse.getBody().getPostId();


        logger.info("Created post with ID: {} and status: {}", postId, createResponse.getBody().getStatus());

        // When
        restTemplate.put("/api/users/" + testUser.getId() + "/posts/" + postId + "/publish", null);
        ResponseEntity<PostById> getResponse = restTemplate.getForEntity("/api/posts/" + postId, PostById.class);

        // Then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getStatus()).isEqualTo("PUBLISHED");
    }

    @Test
    void deletePost_Success() {
        // Given
        CreatePostRequest createRequest = new CreatePostRequest();
        createRequest.setTitle("Delete Test Post");
        createRequest.setContent("Content");
        ResponseEntity<PostByUser> createResponse = restTemplate.postForEntity(
                "/api/users/" + testUser.getId() + "/posts", createRequest, PostByUser.class);
        UUID postId = createResponse.getBody().getPostId();

        // When
        restTemplate.delete("/api/users/" + testUser.getId() + "/posts/" + postId);
        ResponseEntity<PostById> getResponse = restTemplate.getForEntity("/api/posts/" + postId, PostById.class);

        // Then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}