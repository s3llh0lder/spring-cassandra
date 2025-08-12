package example.web;

import example.domain.exceptions.PostNotFoundException;
import example.domain.exceptions.UserNotFoundException;
import example.domain.model.PostById;
import example.domain.model.PostByUser;
import example.domain.ports.input.CreatePostRequest;
import example.domain.ports.input.PostPort;
import example.domain.ports.input.UpdatePostRequest;
import example.spring_cassandra.api.controller.PostsApi;
import example.spring_cassandra.api.model.CreatePostRequestDto;
import example.spring_cassandra.api.model.PostByIdDto;
import example.spring_cassandra.api.model.PostByUserDto;
import example.spring_cassandra.api.model.UpdatePostRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/v1")
public class PostAdapter implements PostsApi {

    @Autowired
    private PostPort postPort;

    @Override
    public ResponseEntity<PostByUserDto> createPost(UUID userId, CreatePostRequestDto createPostRequestDto) {
        try {
            CreatePostRequest request = convertToCreateRequest(createPostRequestDto);
            PostByUser post = postPort.createPost(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(convertToPostByUserDto(post));
        } catch (UserNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<Void> deletePost(UUID userId, UUID postId) {
        try {
            postPort.deletePost(userId, postId);
            return ResponseEntity.noContent().build();
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<PostByIdDto> getPost(UUID postId) {
        try {
            Optional<PostById> post = postPort.getPostById(postId);
            return post.map(p -> ResponseEntity.ok(convertToPostByIdDto(p)))
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<List<PostByUserDto>> getUserPosts(UUID userId, Integer limit, String status) {
        try {
            List<PostByUser> posts;
            if (status != null) {
                posts = postPort.getUserPostsByStatus(userId, status);
            } else {
                int actualLimit = (limit != null) ? limit : 20;
                posts = postPort.getUserPosts(userId, actualLimit);
            }
            List<PostByUserDto> postDtos = posts.stream()
                    .map(this::convertToPostByUserDto)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(postDtos);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<PostByUserDto> publishPost(UUID userId, UUID postId) {
        try {
            PostByUser post = postPort.publishPost(userId, postId);
            return ResponseEntity.ok(convertToPostByUserDto(post));
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @Override
    public ResponseEntity<PostByUserDto> updatePost(UUID userId, UUID postId, UpdatePostRequestDto updatePostRequestDto) {
        try {
            UpdatePostRequest request = convertToUpdateRequest(updatePostRequestDto);
            PostByUser post = postPort.updatePost(userId, postId, request);
            return ResponseEntity.ok(convertToPostByUserDto(post));
        } catch (PostNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private CreatePostRequest convertToCreateRequest(CreatePostRequestDto dto) {
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle(dto.getTitle());
        request.setContent(dto.getContent());
        return request;
    }

    private UpdatePostRequest convertToUpdateRequest(UpdatePostRequestDto dto) {
        UpdatePostRequest request = new UpdatePostRequest();
        request.setTitle(dto.getTitle());
        request.setContent(dto.getContent());
        return request;
    }

    private PostByUserDto convertToPostByUserDto(PostByUser post) {
        PostByUserDto dto = new PostByUserDto();
        dto.setId(post.getPostId());
        dto.setUserId(post.getKey().getUserId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setStatus(PostByUserDto.StatusEnum.fromValue(post.getStatus()));
        if (post.getCreatedAt() != null) {
            dto.setCreatedAt(post.getCreatedAt());
        }
        if (post.getUpdatedAt() != null) {
            dto.setUpdatedAt(post.getUpdatedAt());
        }
        return dto;
    }

    private PostByIdDto convertToPostByIdDto(PostById post) {
        PostByIdDto dto = new PostByIdDto();
        dto.setId(post.getPostId());
        dto.setTitle(post.getTitle());
        dto.setContent(post.getContent());
        dto.setStatus(PostByIdDto.StatusEnum.fromValue(post.getStatus()));
        if (post.getCreatedAt() != null) {
            dto.setCreatedAt(post.getCreatedAt());
        }
        if (post.getUpdatedAt() != null) {
            dto.setUpdatedAt(post.getUpdatedAt());
        }
        return dto;
    }

//    private UserDto convertToUserDto(User user) {
//        UserDto dto = new UserDto();
//        dto.setId(user.getId());
//        dto.setName(user.getName());
//        dto.setEmail(user.getEmail());
//        dto.setCreatedAt(user.getCreatedAt());
//        dto.setUpdatedAt(user.getUpdatedAt());
//        return dto;
//    }
}