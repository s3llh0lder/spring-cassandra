package example.integration;

import example.domain.model.User;
import example.domain.ports.input.CreateUserRequest;
import example.domain.repository.*;
import example.spring_cassandra.api.model.CreatePostRequestDto;
import example.spring_cassandra.api.model.PostByIdDto;
import example.spring_cassandra.api.model.PostByUserDto;
import example.spring_cassandra.api.model.UpdatePostRequestDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PostAdapterIntegrationTest extends BaseCassandraIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserByEmailRepository userByEmailRepository;

    @Autowired
    private UserStatsRepository userStatsRepository;

    @Autowired
    private PostByUserRepository postByUserRepository;

    @Autowired
    private PostByIdRepository postByIdRepository;

    @Autowired
    private PostByUserStatusRepository postByUserStatusRepository;

    private User testUser;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        // Clean all repositories
        userRepository.deleteAll();
        userByEmailRepository.deleteAll();
        userStatsRepository.deleteAll();
        postByUserRepository.deleteAll();
        postByIdRepository.deleteAll();
        postByUserStatusRepository.deleteAll();

        // Create a test user first
        CreateUserRequest userRequest = new CreateUserRequest();
        userRequest.setName("Post Test User");
        userRequest.setEmail("post.test@example.com");
        ResponseEntity<User> userResponse = restTemplate.postForEntity("/api/v1/users", userRequest, User.class);
        testUser = userResponse.getBody();
        testUserId = testUser.getId();
    }

    @Test
    void createPost_Success() {
        // Given
        CreatePostRequestDto request = new CreatePostRequestDto();
        request.setTitle("Integration Test Post");
        request.setContent("This is a test post content");

        // When
        ResponseEntity<PostByUserDto> response = restTemplate.postForEntity(
                "/api/v1/users/{userId}/posts",
                request,
                PostByUserDto.class,
                testUserId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getTitle()).isEqualTo("Integration Test Post");
        assertThat(response.getBody().getContent()).isEqualTo("This is a test post content");
        assertThat(response.getBody().getUserId()).isEqualTo(testUserId);
        assertThat(response.getBody().getStatus().toString()).isEqualTo("DRAFT");
        assertThat(response.getBody().getId()).isNotNull();
        assertThat(response.getBody().getCreatedAt()).isNotNull();
    }

    @Test
    void createPost_UserNotFound_NotFound() {
        // Given
        UUID nonExistentUserId = UUID.randomUUID();
        CreatePostRequestDto request = new CreatePostRequestDto();
        request.setTitle("Test Post");
        request.setContent("Test content");

        // When
        ResponseEntity<PostByUserDto> response = restTemplate.postForEntity(
                "/api/v1/users/{userId}/posts",
                request,
                PostByUserDto.class,
                nonExistentUserId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getPost_Success() {
        // Given - Create a post first
        CreatePostRequestDto createRequest = new CreatePostRequestDto();
        createRequest.setTitle("Get Test Post");
        createRequest.setContent("Get test content");
        ResponseEntity<PostByUserDto> createResponse = restTemplate.postForEntity(
                "/api/v1/users/{userId}/posts",
                createRequest,
                PostByUserDto.class,
                testUserId
        );
        UUID postId = createResponse.getBody().getId();

        // When
        ResponseEntity<PostByIdDto> response = restTemplate.getForEntity(
                "/api/v1/posts/{postId}",
                PostByIdDto.class,
                postId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(postId);
        assertThat(response.getBody().getTitle()).isEqualTo("Get Test Post");
        assertThat(response.getBody().getContent()).isEqualTo("Get test content");
    }

    @Test
    void getPost_NotFound() {
        // Given
        UUID nonExistentPostId = UUID.randomUUID();

        // When
        ResponseEntity<PostByIdDto> response = restTemplate.getForEntity(
                "/api/v1/posts/{postId}",
                PostByIdDto.class,
                nonExistentPostId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getUserPosts_Success() {
        // Given - Create some posts first
        CreatePostRequestDto request1 = new CreatePostRequestDto();
        request1.setTitle("User Post 1");
        request1.setContent("Content 1");
        restTemplate.postForEntity("/api/v1/users/{userId}/posts", request1, PostByUserDto.class, testUserId);

        CreatePostRequestDto request2 = new CreatePostRequestDto();
        request2.setTitle("User Post 2");
        request2.setContent("Content 2");
        restTemplate.postForEntity("/api/v1/users/{userId}/posts", request2, PostByUserDto.class, testUserId);

        // When
        ResponseEntity<List<PostByUserDto>> response = restTemplate.exchange(
                "/api/v1/users/{userId}/posts",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PostByUserDto>>() {
                },
                testUserId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getUserId()).isEqualTo(testUserId);
        assertThat(response.getBody().get(1).getUserId()).isEqualTo(testUserId);
    }

    @Test
    void getUserPosts_WithLimit_Success() {
        // Given - Create multiple posts
        for (int i = 0; i < 5; i++) {
            CreatePostRequestDto request = new CreatePostRequestDto();
            request.setTitle("Limited Post " + i);
            request.setContent("Content " + i);
            restTemplate.postForEntity("/api/v1/users/{userId}/posts", request, PostByUserDto.class, testUserId);
        }

        // When
        ResponseEntity<List<PostByUserDto>> response = restTemplate.exchange(
                "/api/v1/users/{userId}/posts?limit=3",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PostByUserDto>>() {
                },
                testUserId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSizeLessThanOrEqualTo(3);
    }

    @Test
    void getUserPosts_WithStatus_Success() {
        // Given - Create posts with different statuses
        CreatePostRequestDto draftRequest = new CreatePostRequestDto();
        draftRequest.setTitle("Draft Post");
        draftRequest.setContent("Draft content");
        ResponseEntity<PostByUserDto> draftResponse = restTemplate.postForEntity(
                "/api/v1/users/{userId}/posts", draftRequest, PostByUserDto.class, testUserId);

        // Publish one post
        UUID draftPostId = draftResponse.getBody().getId();
        restTemplate.put("/api/v1/users/{userId}/posts/{postId}/publish", null, testUserId, draftPostId);

        // When - Get published posts
        ResponseEntity<List<PostByUserDto>> response = restTemplate.exchange(
                "/api/v1/users/{userId}/posts?status=PUBLISHED",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PostByUserDto>>() {
                },
                testUserId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).hasSize(1);
        assertThat(response.getBody().get(0).getStatus().toString()).isEqualTo("PUBLISHED");
    }

    @Test
    void updatePost_Success() {
        // Given - Create a post first
        CreatePostRequestDto createRequest = new CreatePostRequestDto();
        createRequest.setTitle("Original Title");
        createRequest.setContent("Original content");
        ResponseEntity<PostByUserDto> createResponse = restTemplate.postForEntity(
                "/api/v1/users/{userId}/posts",
                createRequest,
                PostByUserDto.class,
                testUserId
        );
        UUID postId = createResponse.getBody().getId();

        UpdatePostRequestDto updateRequest = new UpdatePostRequestDto();
        updateRequest.setTitle("Updated Title");
        updateRequest.setContent("Updated content");

        // When
        restTemplate.put("/api/v1/users/{userId}/posts/{postId}", updateRequest, testUserId, postId);
        ResponseEntity<PostByIdDto> getResponse = restTemplate.getForEntity(
                "/api/v1/posts/{postId}",
                PostByIdDto.class,
                postId
        );

        // Then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getTitle()).isEqualTo("Updated Title");
        assertThat(getResponse.getBody().getContent()).isEqualTo("Updated content");
    }

    @Test
    void updatePost_PostNotFound_NotFound() {
        // Given
        UUID nonExistentPostId = UUID.randomUUID();
        UpdatePostRequestDto updateRequest = new UpdatePostRequestDto();
        updateRequest.setTitle("Updated Title");
        updateRequest.setContent("Updated content");

        // When
        HttpEntity<UpdatePostRequestDto> httpEntity = new HttpEntity<>(updateRequest);

        ResponseEntity<PostByUserDto> response = restTemplate.exchange(
                "/api/v1/users/{userId}/posts/{postId}",
                HttpMethod.PUT,
                httpEntity,  // Use the manually created HttpEntity
                PostByUserDto.class,
                testUserId,
                nonExistentPostId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void publishPost_Success() {
        // Given - Create a draft post
        CreatePostRequestDto createRequest = new CreatePostRequestDto();
        createRequest.setTitle("Draft Post");
        createRequest.setContent("Draft content");
        ResponseEntity<PostByUserDto> createResponse = restTemplate.postForEntity(
                "/api/v1/users/{userId}/posts",
                createRequest,
                PostByUserDto.class,
                testUserId
        );
        UUID postId = createResponse.getBody().getId();

        // When
        ResponseEntity<PostByUserDto> response = restTemplate.exchange(
                "/api/v1/users/{userId}/posts/{postId}/publish",
                HttpMethod.PUT,
                null,
                PostByUserDto.class,
                testUserId,
                postId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getStatus().toString()).isEqualTo("PUBLISHED");
    }

    @Test
    void publishPost_PostNotFound_NotFound() {
        // Given
        UUID nonExistentPostId = UUID.randomUUID();

        // When
        ResponseEntity<PostByUserDto> response = restTemplate.exchange(
                "/api/v1/users/{userId}/posts/{postId}/publish",
                HttpMethod.PUT,
                null,
                PostByUserDto.class,
                testUserId,
                nonExistentPostId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deletePost_Success() {
        // Given - Create a post first
        CreatePostRequestDto createRequest = new CreatePostRequestDto();
        createRequest.setTitle("Post to Delete");
        createRequest.setContent("This post will be deleted");
        ResponseEntity<PostByUserDto> createResponse = restTemplate.postForEntity(
                "/api/v1/users/{userId}/posts",
                createRequest,
                PostByUserDto.class,
                testUserId
        );
        UUID postId = createResponse.getBody().getId();

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/users/{userId}/posts/{postId}",
                HttpMethod.DELETE,
                null,
                Void.class,
                testUserId,
                postId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // Verify post is deleted
        ResponseEntity<PostByIdDto> getResponse = restTemplate.getForEntity(
                "/api/v1/posts/{postId}",
                PostByIdDto.class,
                postId
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deletePost_PostNotFound_NotFound() {
        // Given
        UUID nonExistentPostId = UUID.randomUUID();

        // When
        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/v1/users/{userId}/posts/{postId}",
                HttpMethod.DELETE,
                null,
                Void.class,
                testUserId,
                nonExistentPostId
        );

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void fullPostWorkflow_Success() {
        // Given
        CreatePostRequestDto createRequest = new CreatePostRequestDto();
        createRequest.setTitle("Workflow Test Post");
        createRequest.setContent("Testing full workflow");

        // When & Then - Create post
        ResponseEntity<PostByUserDto> createResponse = restTemplate.postForEntity(
                "/api/v1/users/{userId}/posts",
                createRequest,
                PostByUserDto.class,
                testUserId
        );
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        UUID postId = createResponse.getBody().getId();
        assertThat(createResponse.getBody().getStatus().toString()).isEqualTo("DRAFT");

        // Update post
        UpdatePostRequestDto updateRequest = new UpdatePostRequestDto();
        updateRequest.setTitle("Updated Workflow Test Post");
        updateRequest.setContent("Updated workflow content");
        restTemplate.put("/api/v1/users/{userId}/posts/{postId}", updateRequest, testUserId, postId);

        // Publish post
        ResponseEntity<PostByUserDto> publishResponse = restTemplate.exchange(
                "/api/v1/users/{userId}/posts/{postId}/publish",
                HttpMethod.PUT,
                null,
                PostByUserDto.class,
                testUserId,
                postId
        );
        assertThat(publishResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(publishResponse.getBody().getStatus().toString()).isEqualTo("PUBLISHED");

        // Verify by getting post
        ResponseEntity<PostByIdDto> getResponse = restTemplate.getForEntity(
                "/api/v1/posts/{postId}",
                PostByIdDto.class,
                postId
        );
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getTitle()).isEqualTo("Updated Workflow Test Post");
        assertThat(getResponse.getBody().getStatus().toString()).isEqualTo("PUBLISHED");

        // Delete post
        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                "/api/v1/users/{userId}/posts/{postId}",
                HttpMethod.DELETE,
                null,
                Void.class,
                testUserId,
                postId
        );
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }
}