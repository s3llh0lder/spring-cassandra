package example.controller;

import example.exception.EmailAlreadyExistsException;
import example.exception.UserNotFoundException;
import example.model.User;
import example.request.CreateUserRequest;
import example.request.UpdateUserRequest;
import example.request.UserWithStats;
import example.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody CreateUserRequest request) {
        try {
            User user = userService.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<User> getUser(@PathVariable UUID userId) {
        Optional<User> user = userService.getUserById(userId);
        return user.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/users/{userId}/stats")
    public ResponseEntity<UserWithStats> getUserWithStats(@PathVariable UUID userId) {
        try {
            UserWithStats userWithStats = userService.getUserWithStats(userId);
            return ResponseEntity.ok(userWithStats);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<User> updateUser(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserRequest request) {
        try {
            User user = userService.updateUser(userId, request);
            return ResponseEntity.ok(user);
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}