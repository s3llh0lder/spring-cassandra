package example.domain.services;

import example.domain.exceptions.EmailAlreadyExistsException;
import example.domain.exceptions.UserNotFoundException;
import example.domain.model.User;
import example.domain.model.UserByEmail;
import example.domain.model.UserStats;
import example.domain.ports.input.*;
import example.domain.repository.UserByEmailRepository;
import example.domain.repository.UserRepository;
import example.domain.repository.UserStatsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService implements UserPort {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserByEmailRepository userByEmailRepository;

    @Autowired
    private UserStatsRepository userStatsRepository;

    @Transactional
    public User createUser(CreateUserRequest request) throws EmailAlreadyExistsException {
        // Check if email already exists
        Optional<UserByEmail> existingUser = userByEmailRepository.findById(request.getEmail());
        if (existingUser.isPresent()) {
            throw new EmailAlreadyExistsException("Email already exists: " + request.getEmail());
        }

        // Create and save user
        User user = new User(request.getName(), request.getEmail());
        User savedUser = userRepository.save(user);

        // Create email lookup entry
        UserByEmail userByEmail = UserByEmail.fromUser(savedUser);
        userByEmailRepository.save(userByEmail);

        // Initialize user stats
        UserStats stats = new UserStats(savedUser.getId());
        userStatsRepository.save(stats);

        return savedUser;
    }

    public Optional<User> getUserById(UUID userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> getUserByEmail(String email) {
        Optional<UserByEmail> userByEmail = userByEmailRepository.findById(email);
        return userByEmail.flatMap(byEmail -> userRepository.findById(byEmail.getUserId()));
    }

    @Transactional
    public User updateUser(UUID userId, UpdateUserRequest request) throws UserNotFoundException, EmailAlreadyExistsException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        String oldEmail = user.getEmail();

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(oldEmail)) {
            // Check if new email already exists
            Optional<UserByEmail> existingUser = userByEmailRepository.findById(request.getEmail());
            if (existingUser.isPresent()) {
                throw new EmailAlreadyExistsException("Email already exists: " + request.getEmail());
            }
            user.setEmail(request.getEmail());
        }

        User savedUser = userRepository.save(user);

        // Update email lookup table if email changed
        if (request.getEmail() != null && !request.getEmail().equals(oldEmail)) {
            // Delete old email entry
            userByEmailRepository.deleteById(oldEmail);
            // Create new email entry
            UserByEmail userByEmail = UserByEmail.fromUser(savedUser);
            userByEmailRepository.save(userByEmail);
        } else if (request.getName() != null) {
            // Update name in email lookup table
            Optional<UserByEmail> userByEmail = userByEmailRepository.findById(oldEmail);
            if (userByEmail.isPresent()) {
                userByEmail.get().setName(request.getName());
                userByEmailRepository.save(userByEmail.get());
            }
        }

        return savedUser;
    }


    public UserWithStats getUserWithStats(UUID userId) throws UserNotFoundException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));

        UserStats stats = userStatsRepository.findById(userId)
                .orElse(new UserStats(userId));

        // Convert domain model to port model
        UserStats portStats = new UserStats();
        portStats.setUserId(stats.getUserId());
        portStats.setTotalPosts(stats.getTotalPosts());
//        portStats.setTotalViews(stats.getTotalViews());
//        portStats.setCreatedAt(stats.getCreatedAt());
        portStats.setUpdatedAt(stats.getUpdatedAt());
        
        return new UserWithStats(user, portStats);
    }
}