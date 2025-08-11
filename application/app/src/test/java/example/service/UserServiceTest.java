package example.service;

import example.domain.exceptions.EmailAlreadyExistsException;
import example.domain.exceptions.UserNotFoundException;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserByEmailRepository userByEmailRepository;

    @Mock
    private UserStatsRepository userStatsRepository;

    @InjectMocks
    private UserService userService;

    private CreateUserRequest createUserRequest;
    private UpdateUserRequest updateUserRequest;
    private User testUser;
    private UserByEmail testUserByEmail;
    private UserStats testUserStats;

    @BeforeEach
    void setUp() {
        createUserRequest = new CreateUserRequest();
        createUserRequest.setName("John Doe");
        createUserRequest.setEmail("john.doe@example.com");

        updateUserRequest = new UpdateUserRequest();
        updateUserRequest.setName("John Smith");
        updateUserRequest.setEmail("john.smith@example.com");

        testUser = new User("John Doe", "john.doe@example.com");
        testUser.setId(UUID.randomUUID());

        testUserByEmail = UserByEmail.fromUser(testUser);
        testUserStats = new UserStats(testUser.getId());
    }

    @Test
    void createUser_Success() {
        // Given
        when(userByEmailRepository.findById("john.doe@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userByEmailRepository.save(any(UserByEmail.class))).thenReturn(testUserByEmail);
        when(userStatsRepository.save(any(UserStats.class))).thenReturn(testUserStats);

        // When
        User result = userService.createUser(createUserRequest);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");

        verify(userByEmailRepository).findById("john.doe@example.com");
        verify(userRepository).save(any(User.class));
        verify(userByEmailRepository).save(any(UserByEmail.class));
        verify(userStatsRepository).save(any(UserStats.class));
    }

    @Test
    void createUser_EmailAlreadyExists_ThrowsException() {
        // Given
        when(userByEmailRepository.findById("john.doe@example.com")).thenReturn(Optional.of(testUserByEmail));

        // When & Then
        assertThatThrownBy(() -> userService.createUser(createUserRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists: john.doe@example.com");

        verify(userByEmailRepository).findById("john.doe@example.com");
        verify(userRepository, never()).save(any());
        verify(userStatsRepository, never()).save(any());
    }

    @Test
    void getUserById_UserExists_ReturnsUser() {
        // Given
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserById(testUser.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(testUser.getId());
        assertThat(result.get().getName()).isEqualTo("John Doe");

        verify(userRepository).findById(testUser.getId());
    }

    @Test
    void getUserById_UserNotExists_ReturnsEmpty() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserById(nonExistentId);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findById(nonExistentId);
    }

    @Test
    void getUserByEmail_UserExists_ReturnsUser() {
        // Given
        when(userByEmailRepository.findById("john.doe@example.com")).thenReturn(Optional.of(testUserByEmail));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));

        // When
        Optional<User> result = userService.getUserByEmail("john.doe@example.com");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");

        verify(userByEmailRepository).findById("john.doe@example.com");
        verify(userRepository).findById(testUser.getId());
    }

    @Test
    void getUserByEmail_UserNotExists_ReturnsEmpty() {
        // Given
        when(userByEmailRepository.findById("nonexistent@example.com")).thenReturn(Optional.empty());

        // When
        Optional<User> result = userService.getUserByEmail("nonexistent@example.com");

        // Then
        assertThat(result).isEmpty();
        verify(userByEmailRepository).findById("nonexistent@example.com");
        verify(userRepository, never()).findById(any());
    }

    @Test
    void updateUser_UpdateNameOnly_Success() {
        // Given
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("John Smith");

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userByEmailRepository.findById(testUser.getEmail())).thenReturn(Optional.of(testUserByEmail));
        when(userByEmailRepository.save(any(UserByEmail.class))).thenReturn(testUserByEmail);

        // When
        User result = userService.updateUser(testUser.getId(), request);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findById(testUser.getId());
        verify(userRepository).save(any(User.class));
        verify(userByEmailRepository).findById(testUser.getEmail());
        verify(userByEmailRepository).save(any(UserByEmail.class));
    }

    @Test
    void updateUser_UpdateEmail_Success() {
        // Given
        String newEmail = "john.smith@example.com";
        updateUserRequest.setEmail(newEmail);
        updateUserRequest.setName(null);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userByEmailRepository.findById(newEmail)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        // When
        User result = userService.updateUser(testUser.getId(), updateUserRequest);

        // Then
        assertThat(result).isNotNull();
        verify(userRepository).findById(testUser.getId());
        verify(userByEmailRepository).findById(newEmail);
        verify(userRepository).save(any(User.class));
//        verify(userByEmailRepository).deleteById(testUser.getEmail());
        verify(userByEmailRepository).save(any(UserByEmail.class));
    }

    @Test
    void updateUser_EmailAlreadyExists_ThrowsException() {
        // Given
        String existingEmail = "existing@example.com";
        UserByEmail existingUserByEmail = new UserByEmail();
        existingUserByEmail.setEmail(existingEmail);
        existingUserByEmail.setUserId(UUID.randomUUID());

        updateUserRequest.setEmail(existingEmail);

        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userByEmailRepository.findById(existingEmail)).thenReturn(Optional.of(existingUserByEmail));

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(testUser.getId(), updateUserRequest))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessage("Email already exists: " + existingEmail);

        verify(userRepository).findById(testUser.getId());
        verify(userByEmailRepository).findById(existingEmail);
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_UserNotFound_ThrowsException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUser(nonExistentId, updateUserRequest))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + nonExistentId);

        verify(userRepository).findById(nonExistentId);
        verify(userByEmailRepository, never()).findById(any());
    }

    @Test
    void getUserWithStats_Success() {
        // Given
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(userStatsRepository.findById(testUser.getId())).thenReturn(Optional.of(testUserStats));

        // When
        UserWithStats result = userService.getUserWithStats(testUser.getId());

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUser()).isEqualTo(testUser);
        assertThat(result.getStats()).isEqualTo(testUserStats);

        verify(userRepository).findById(testUser.getId());
        verify(userStatsRepository).findById(testUser.getId());
    }

    @Test
    void getUserWithStats_UserNotFound_ThrowsException() {
        // Given
        UUID nonExistentId = UUID.randomUUID();
        when(userRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserWithStats(nonExistentId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found: " + nonExistentId);

        verify(userRepository).findById(nonExistentId);
        verify(userStatsRepository, never()).findById(any());
    }
}