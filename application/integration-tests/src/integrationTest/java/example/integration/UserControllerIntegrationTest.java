package example.integration;

import example.domain.model.User;
import example.domain.repository.UserByEmailRepository;
import example.domain.repository.UserRepository;
import example.domain.repository.UserStatsRepository;
import example.domain.ports.input.CreateUserRequest;
import example.domain.ports.input.UpdateUserRequest;
import example.domain.ports.input.UserWithStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest extends BaseCassandraIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserByEmailRepository userByEmailRepository;

    @Autowired
    private UserStatsRepository userStatsRepository;

    @BeforeEach
    void set() {
        userRepository.deleteAll();
        userByEmailRepository.deleteAll();
        userStatsRepository.deleteAll();
    }

    @Test
    void createUser_Success() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setName("API Test User");
        request.setEmail("api.test@example.com");

        // When
        ResponseEntity<User> response = restTemplate.postForEntity("/api/users", request, User.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getName()).isEqualTo("API Test User");
        assertThat(response.getBody().getEmail()).isEqualTo("api.test@example.com");
        assertThat(response.getBody().getId()).isNotNull();
    }

    @Test
    void createUser_DuplicateEmail_BadRequest() {
        // Given
        CreateUserRequest request1 = new CreateUserRequest();
        request1.setName("User One");
        request1.setEmail("duplicate@example.com");
        restTemplate.postForEntity("/api/users", request1, User.class);

        CreateUserRequest request2 = new CreateUserRequest();
        request2.setName("User Two");
        request2.setEmail("duplicate@example.com");

        // When
        ResponseEntity<User> response = restTemplate.postForEntity("/api/users", request2, User.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getUser_Success() {
        // Given
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("Get Test User");
        createRequest.setEmail("get.test@example.com");
        ResponseEntity<User> createResponse = restTemplate.postForEntity("/api/users", createRequest, User.class);
        UUID userId = createResponse.getBody().getId();

        // When
        ResponseEntity<User> response = restTemplate.getForEntity("/api/users/" + userId, User.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(userId);
        assertThat(response.getBody().getName()).isEqualTo("Get Test User");
    }

    @Test
    void getUser_NotFound() {
        // Given
        UUID nonExistentId = UUID.randomUUID();

        // When
        ResponseEntity<User> response = restTemplate.getForEntity("/api/users/" + nonExistentId, User.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getUserWithStats_Success() {
        // Given
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("Stats Test User");
        createRequest.setEmail("stats.test@example.com");
        ResponseEntity<User> createResponse = restTemplate.postForEntity("/api/users", createRequest, User.class);
        UUID userId = createResponse.getBody().getId();

        // When
        ResponseEntity<UserWithStats> response = restTemplate.getForEntity("/api/users/" + userId + "/stats", UserWithStats.class);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getUser().getId()).isEqualTo(userId);
        assertThat(response.getBody().getStats().getTotalPosts()).isEqualTo(0);
    }

    @Test
    void updateUser_Success() {
        // Given
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("Update Test User");
        createRequest.setEmail("update.test@example.com");
        ResponseEntity<User> createResponse = restTemplate.postForEntity("/api/users", createRequest, User.class);
        UUID userId = createResponse.getBody().getId();

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("Updated Name");
        updateRequest.setEmail("updated.test@example.com");

        // When
        restTemplate.put("/api/users/" + userId, updateRequest);
        ResponseEntity<User> getResponse = restTemplate.getForEntity("/api/users/" + userId, User.class);

        // Then
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody().getName()).isEqualTo("Updated Name");
        assertThat(getResponse.getBody().getEmail()).isEqualTo("updated.test@example.com");
    }
}