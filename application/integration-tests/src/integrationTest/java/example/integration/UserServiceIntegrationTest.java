package example.integration;


import example.domain.exceptions.EmailAlreadyExistsException;
import example.domain.model.User;
import example.domain.model.UserByEmail;
import example.domain.model.UserStats;
import example.domain.repository.UserByEmailRepository;
import example.domain.repository.UserRepository;
import example.domain.repository.UserStatsRepository;
import example.domain.ports.input.CreateUserRequest;
import example.domain.ports.input.UpdateUserRequest;
import example.domain.ports.input.UserWithStats;
import example.domain.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;


@SpringBootTest
class UserServiceIntegrationTest extends BaseCassandraIntegrationTest {

    @Autowired
    private UserService userService;

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
    void createUser_FullWorkflow_Success() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setName("John Doe");
        request.setEmail("john.doe@example.com");

        // When
        User createdUser = userService.createUser(request);

        // Then
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getName()).isEqualTo("John Doe");
        assertThat(createdUser.getEmail()).isEqualTo("john.doe@example.com");

        // Verify user is saved in main table
        Optional<User> foundUser = userRepository.findById(createdUser.getId());
        assertThat(foundUser).isPresent();

        // Verify email lookup is created
        Optional<UserByEmail> foundUserByEmail = userByEmailRepository.findById("john.doe@example.com");
        assertThat(foundUserByEmail).isPresent();
        assertThat(foundUserByEmail.get().getUserId()).isEqualTo(createdUser.getId());

        // Verify user stats are initialized
        Optional<UserStats> foundStats = userStatsRepository.findById(createdUser.getId());
        assertThat(foundStats).isPresent();
        assertThat(foundStats.get().getTotalPosts()).isEqualTo(0);
    }

    @Test
    void getUserByEmail_FullWorkflow_Success() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Jane Doe");
        request.setEmail("jane.doe@example.com");
        User createdUser = userService.createUser(request);

        // When
        Optional<User> foundUser = userService.getUserByEmail("jane.doe@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.get().getName()).isEqualTo("Jane Doe");
        assertThat(foundUser.get().getEmail()).isEqualTo("jane.doe@example.com");
    }

    @Test
    void updateUserEmail_FullWorkflow_Success() {
        // Given
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("Bob Smith");
        createRequest.setEmail("bob.smith@example.com");
        User createdUser = userService.createUser(createRequest);

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setEmail("robert.smith@example.com");

        // When
        User updatedUser = userService.updateUser(createdUser.getId(), updateRequest);

        // Then
        assertThat(updatedUser.getEmail()).isEqualTo("robert.smith@example.com");

        // Verify old email lookup is removed
        Optional<UserByEmail> oldEmailLookup = userByEmailRepository.findById("bob.smith@example.com");
        assertThat(oldEmailLookup).isEmpty();

        // Verify new email lookup is created
        Optional<UserByEmail> newEmailLookup = userByEmailRepository.findById("robert.smith@example.com");
        assertThat(newEmailLookup).isPresent();
        assertThat(newEmailLookup.get().getUserId()).isEqualTo(createdUser.getId());

        // Verify user can be found by new email
        Optional<User> foundByEmail = userService.getUserByEmail("robert.smith@example.com");
        assertThat(foundByEmail).isPresent();
        assertThat(foundByEmail.get().getId()).isEqualTo(createdUser.getId());
    }

    @Test
    void createUser_DuplicateEmail_ThrowsException() {
        // Given
        CreateUserRequest request1 = new CreateUserRequest();
        request1.setName("User One");
        request1.setEmail("duplicate@example.com");
        userService.createUser(request1);

        CreateUserRequest request2 = new CreateUserRequest();
        request2.setName("User Two");
        request2.setEmail("duplicate@example.com");

        // When & Then
        assertThatThrownBy(() -> userService.createUser(request2))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists: duplicate@example.com");
    }

    @Test
    void getUserWithStats_FullWorkflow_Success() {
        // Given
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Alice Johnson");
        request.setEmail("alice.johnson@example.com");
        User createdUser = userService.createUser(request);

        // When
        UserWithStats userWithStats = userService.getUserWithStats(createdUser.getId());

        // Then
        assertThat(userWithStats).isNotNull();
        assertThat(userWithStats.getUser().getName()).isEqualTo("Alice Johnson");
        assertThat(userWithStats.getStats().getTotalPosts()).isEqualTo(0);
        assertThat(userWithStats.getStats().getPublishedPosts()).isEqualTo(0);
        assertThat(userWithStats.getStats().getDraftPosts()).isEqualTo(0);
    }
}
