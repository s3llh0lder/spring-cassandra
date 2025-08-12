package example.web;

import example.domain.exceptions.EmailAlreadyExistsException;
import example.domain.exceptions.UserNotFoundException;
import example.domain.model.User;
import example.domain.ports.input.CreateUserRequest;
import example.domain.ports.input.UpdateUserRequest;
import example.domain.ports.input.UserPort;
import example.domain.ports.input.UserWithStats;
import example.spring_cassandra.api.controller.UsersApi;
import example.spring_cassandra.api.model.CreateUserRequestDto;
import example.spring_cassandra.api.model.UpdateUserRequestDto;
import example.spring_cassandra.api.model.UserDto;
import example.spring_cassandra.api.model.UserWithStatsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/api/v1")
public class UserAdapter implements UsersApi {

    @Autowired
    private UserPort userPort;

    @Override
    public ResponseEntity<UserDto> createUser(CreateUserRequestDto createUserRequestDto) {
        try {
            CreateUserRequest request = convertToCreateRequest(createUserRequestDto);
            User user = userPort.createUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToUserDto(user));
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<UserDto> getUser(UUID userId) {
        try {
            Optional<User> user = userPort.getUserById(userId);
            return user.map(u -> ResponseEntity.ok(convertToUserDto(u)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<UserWithStatsDto> getUserWithStats(UUID userId) {
        try {
            UserWithStats userWithStats = userPort.getUserWithStats(userId);
            return ResponseEntity.ok(convertToUserWithStatsDto(userWithStats));
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<UserDto> updateUser(UUID userId, UpdateUserRequestDto updateUserRequestDto) {
        try {
            UpdateUserRequest request = convertToUpdateRequest(updateUserRequestDto);
            User user = userPort.updateUser(userId, request);
            return ResponseEntity.ok(convertToUserDto(user));
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private CreateUserRequest convertToCreateRequest(CreateUserRequestDto dto) {
        CreateUserRequest request = new CreateUserRequest();
        request.setName(dto.getName());
        request.setEmail(dto.getEmail());
        return request;
    }

    private UpdateUserRequest convertToUpdateRequest(UpdateUserRequestDto dto) {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName(dto.getName());
        request.setEmail(dto.getEmail());
        return request;
    }

    private UserDto convertToUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        if (user.getCreatedAt() != null) {
            dto.setCreatedAt(user.getCreatedAt());
        }
        if (user.getUpdatedAt() != null) {
            dto.setUpdatedAt(user.getUpdatedAt());
        }
        return dto;
    }

    private UserWithStatsDto convertToUserWithStatsDto(UserWithStats userWithStats) {
        UserWithStatsDto dto = new UserWithStatsDto();

        // Set the nested user object
        UserDto userDto = convertToUserDto(userWithStats.getUser());
        dto.setUser(userDto);

        // Set the stats object
        if (userWithStats.getStats() != null) {
            Map<String, Object> statsMap = new HashMap<>();
            statsMap.put("totalPosts", userWithStats.getStats().getTotalPosts());
            statsMap.put("publishedPosts", userWithStats.getStats().getPublishedPosts());
            statsMap.put("draftPosts", userWithStats.getStats().getDraftPosts());

            if (userWithStats.getStats().getLastPostDate() != null) {
                statsMap.put("lastPostDate", userWithStats.getStats().getLastPostDate());
            }
            if (userWithStats.getStats().getUpdatedAt() != null) {
                statsMap.put("updatedAt", userWithStats.getStats().getUpdatedAt());
            }

            dto.setStats(statsMap);
        }

        return dto;
    }
}