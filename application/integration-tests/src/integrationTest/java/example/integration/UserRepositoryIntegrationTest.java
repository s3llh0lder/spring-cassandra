package example.integration;

import example.domain.model.User;
import example.domain.model.UserByEmail;
import example.domain.model.UserStats;
import example.domain.repository.UserByEmailRepository;
import example.domain.repository.UserRepository;
import example.domain.repository.UserStatsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserRepositoryIntegrationTest extends BaseCassandraIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserByEmailRepository userByEmailRepository;

    @Autowired
    private UserStatsRepository userStatsRepository;

    private User testUser;
    private UserByEmail testUserByEmail;
    private UserStats testUserStats;

    @BeforeEach
    void set() {
        userRepository.deleteAll();
        userByEmailRepository.deleteAll();
        userStatsRepository.deleteAll();

        testUser = new User("John Doe", "john.doe@example.com");
        testUserByEmail = UserByEmail.fromUser(testUser);
        testUserStats = new UserStats(testUser.getId());
    }

    @Test
    void saveAndFindUser_Success() {
        // When
        User savedUser = userRepository.save(testUser);
        Optional<User> foundUser = userRepository.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("John Doe");
        assertThat(foundUser.get().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(foundUser.get().getId()).isEqualTo(savedUser.getId());
    }

    @Test
    void saveAndFindUserByEmail_Success() {
        // When
        userRepository.save(testUser);
        UserByEmail savedUserByEmail = userByEmailRepository.save(testUserByEmail);
        Optional<UserByEmail> foundUserByEmail = userByEmailRepository.findById("john.doe@example.com");

        // Then
        assertThat(foundUserByEmail).isPresent();
        assertThat(foundUserByEmail.get().getEmail()).isEqualTo("john.doe@example.com");
        assertThat(foundUserByEmail.get().getUserId()).isEqualTo(testUser.getId());
        assertThat(foundUserByEmail.get().getName()).isEqualTo("John Doe");
    }

    @Test
    void updateUser_Success() {
        // Given
        User savedUser = userRepository.save(testUser);

        // When
        savedUser.setName("Jane Doe");
        savedUser.setEmail("jane.doe@example.com");
        User updatedUser = userRepository.save(savedUser);

        // Then
        Optional<User> foundUser = userRepository.findById(updatedUser.getId());
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Jane Doe");
        assertThat(foundUser.get().getEmail()).isEqualTo("jane.doe@example.com");
        assertThat(foundUser.get().getUpdatedAt()).isAfter(foundUser.get().getCreatedAt());
    }

    @Test
    void deleteUser_Success() {
        // Given
        User savedUser = userRepository.save(testUser);

        // When
        userRepository.deleteById(savedUser.getId());

        // Then
        Optional<User> foundUser = userRepository.findById(savedUser.getId());
        assertThat(foundUser).isEmpty();
    }

    @Test
    void saveAndFindUserStats_Success() {
        // When
        UserStats savedStats = userStatsRepository.save(testUserStats);
        Optional<UserStats> foundStats = userStatsRepository.findById(testUser.getId());

        // Then
        assertThat(foundStats).isPresent();
        assertThat(foundStats.get().getUserId()).isEqualTo(testUser.getId());
        assertThat(foundStats.get().getTotalPosts()).isEqualTo(0);
        assertThat(foundStats.get().getPublishedPosts()).isEqualTo(0);
        assertThat(foundStats.get().getDraftPosts()).isEqualTo(0);
    }

    @Test
    void updateUserStats_Success() {
        // Given
        UserStats savedStats = userStatsRepository.save(testUserStats);

        // When
        savedStats.incrementPost("PUBLISHED");
        savedStats.incrementPost("DRAFT");
        UserStats updatedStats = userStatsRepository.save(savedStats);

        // Then
        Optional<UserStats> foundStats = userStatsRepository.findById(testUser.getId());
        assertThat(foundStats).isPresent();
        assertThat(foundStats.get().getTotalPosts()).isEqualTo(2);
        assertThat(foundStats.get().getPublishedPosts()).isEqualTo(1);
        assertThat(foundStats.get().getDraftPosts()).isEqualTo(1);
    }


}