package example.domain.ports.input;

import example.domain.model.User;
import example.domain.exceptions.EmailAlreadyExistsException;
import example.domain.exceptions.UserNotFoundException;
import example.domain.model.UserStats;

import java.util.Optional;
import java.util.UUID;

public interface UserPort {
    
    /**
     * Create a new user
     * @param request User creation request
     * @return Created user
     * @throws EmailAlreadyExistsException if email already exists
     */
    User createUser(CreateUserRequest request) throws EmailAlreadyExistsException;
    
    /**
     * Get user by ID
     * @param userId User ID
     * @return Optional user
     */
    Optional<User> getUserById(UUID userId);
    
    /**
     * Get user by email
     * @param email User email
     * @return Optional user
     */
    Optional<User> getUserByEmail(String email);
    
    /**
     * Update user
     * @param userId User ID
     * @param request Update request
     * @return Updated user
     * @throws UserNotFoundException if user not found
     * @throws EmailAlreadyExistsException if email already exists
     */
    User updateUser(UUID userId, UpdateUserRequest request) throws UserNotFoundException, EmailAlreadyExistsException;
    
    /**
     * Get user with statistics
     * @param userId User ID
     * @return User with stats
     * @throws UserNotFoundException if user not found
     */
    UserWithStats getUserWithStats(UUID userId) throws UserNotFoundException;
    





}
